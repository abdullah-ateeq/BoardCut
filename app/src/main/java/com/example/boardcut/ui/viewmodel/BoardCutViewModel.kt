package com.example.boardcut.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.boardcut.data.AppThemeMode
import com.example.boardcut.data.ProjectRepository
import com.example.boardcut.data.SampleProjects
import com.example.boardcut.data.SettingsRepository
import com.example.boardcut.engine.CuttingOptimizer
import com.example.boardcut.engine.PlanExporter
import com.example.boardcut.model.CalculationSettings
import com.example.boardcut.model.CutPiece
import com.example.boardcut.model.CuttingProject
import com.example.boardcut.model.OptimizationResult
import com.example.boardcut.model.StockBoard
import com.example.boardcut.model.UnitOfMeasurement
import com.example.boardcut.ui.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BoardCutViewModel(application: Application) : AndroidViewModel(application) {

    private val projectRepo = ProjectRepository(application)
    private val settingsRepo = SettingsRepository(application)

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Calculator)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _activeProject = MutableStateFlow(projectRepo.loadActiveProject())
    val activeProject: StateFlow<CuttingProject> = _activeProject.asStateFlow()

    private val _optimizationResult = MutableStateFlow<OptimizationResult?>(null)
    val optimizationResult: StateFlow<OptimizationResult?> = _optimizationResult.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isCalculating = MutableStateFlow(false)
    val isCalculating: StateFlow<Boolean> = _isCalculating.asStateFlow()

    private val _historyProjects = MutableStateFlow<List<CuttingProject>>(projectRepo.getSavedProjects())
    val historyProjects: StateFlow<List<CuttingProject>> = _historyProjects.asStateFlow()

    val themeMode: StateFlow<AppThemeMode> = settingsRepo.themeMode

    init {
        // Run initial calculation if the loaded project has pieces
        if (_activeProject.value.requiredPieces.isNotEmpty()) {
            calculate(navigateToResults = false)
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun updateProjectName(name: String) {
        _activeProject.value = _activeProject.value.copy(name = name)
        saveActive()
    }

    fun updateStockBoards(boards: List<StockBoard>) {
        _activeProject.value = _activeProject.value.copy(stockBoards = boards)
        saveActive()
    }

    fun updateSettings(settings: CalculationSettings) {
        _activeProject.value = _activeProject.value.copy(settings = settings)
        saveActive()
    }

    fun addPiece() {
        val current = _activeProject.value.requiredPieces
        val defaultLen = when (_activeProject.value.unit) {
            UnitOfMeasurement.MM -> 600.0
            UnitOfMeasurement.CM -> 60.0
            UnitOfMeasurement.INCH -> 24.0
            UnitOfMeasurement.FEET -> 2.0
        }
        val newPiece = CutPiece(
            label = "Piece #${current.size + 1}",
            length = defaultLen,
            quantity = 1
        )
        _activeProject.value = _activeProject.value.copy(requiredPieces = current + newPiece)
        saveActive()
    }

    fun updatePiece(updated: CutPiece) {
        val current = _activeProject.value.requiredPieces.toMutableList()
        val index = current.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            current[index] = updated
            _activeProject.value = _activeProject.value.copy(requiredPieces = current)
            saveActive()
        }
    }

    fun deletePiece(pieceId: String) {
        val current = _activeProject.value.requiredPieces.filterNot { it.id == pieceId }
        _activeProject.value = _activeProject.value.copy(requiredPieces = current)
        saveActive()
    }

    fun clearPieces() {
        _activeProject.value = _activeProject.value.copy(requiredPieces = emptyList())
        _optimizationResult.value = null
        saveActive()
    }

    fun changeUnit(newUnit: UnitOfMeasurement, convertValues: Boolean = true) {
        val oldUnit = _activeProject.value.unit
        if (oldUnit == newUnit) return

        if (convertValues) {
            // Convert stock boards
            val convertedStock = _activeProject.value.stockBoards.map { stock ->
                val lengthMm = oldUnit.toMm(stock.length)
                stock.copy(
                    length = newUnit.fromMm(lengthMm),
                    width = stock.width?.let { newUnit.fromMm(oldUnit.toMm(it)) },
                    thickness = stock.thickness?.let { newUnit.fromMm(oldUnit.toMm(it)) }
                )
            }
            // Convert pieces
            val convertedPieces = _activeProject.value.requiredPieces.map { piece ->
                val lengthMm = oldUnit.toMm(piece.length)
                piece.copy(length = newUnit.fromMm(lengthMm))
            }
            // Convert settings
            val oldSettings = _activeProject.value.settings
            val convertedSettings = CalculationSettings(
                kerf = newUnit.fromMm(oldUnit.toMm(oldSettings.kerf)),
                trimAllowance = newUnit.fromMm(oldUnit.toMm(oldSettings.trimAllowance)),
                minUsableOffcut = newUnit.fromMm(oldUnit.toMm(oldSettings.minUsableOffcut))
            )

            _activeProject.value = _activeProject.value.copy(
                unit = newUnit,
                stockBoards = convertedStock,
                requiredPieces = convertedPieces,
                settings = convertedSettings
            )
        } else {
            _activeProject.value = _activeProject.value.copy(
                unit = newUnit,
                settings = CalculationSettings(
                    kerf = newUnit.defaultKerf,
                    trimAllowance = newUnit.defaultTrim
                )
            )
        }
        saveActive()
        if (_activeProject.value.requiredPieces.isNotEmpty()) {
            calculate(navigateToResults = false)
        }
    }

    fun calculate(navigateToResults: Boolean = true) {
        viewModelScope.launch {
            _isCalculating.value = true
            _errorMessage.value = null

            val project = _activeProject.value
            val validation = CuttingOptimizer.validateInputs(
                stockBoards = project.stockBoards,
                pieces = project.requiredPieces,
                settings = project.settings,
                unit = project.unit
            )

            if (!validation.isValid) {
                _isCalculating.value = false
                _errorMessage.value = validation.errorMessage
                return@launch
            }

            val result = withContext(Dispatchers.Default) {
                CuttingOptimizer.optimize(
                    stockBoards = project.stockBoards,
                    requiredPieces = project.requiredPieces,
                    settings = project.settings,
                    unit = project.unit
                )
            }

            _optimizationResult.value = result
            _isCalculating.value = false
            saveActive()

            if (navigateToResults) {
                _currentScreen.value = Screen.Results
            }
        }
    }

    fun saveCurrentProjectToHistory(name: String? = null) {
        val current = _activeProject.value
        val toSave = if (!name.isNullOrBlank()) current.copy(name = name) else current
        projectRepo.saveProjectToHistory(toSave)
        _historyProjects.value = projectRepo.getSavedProjects()
        Toast.makeText(getApplication(), "Project '${toSave.name}' saved to history!", Toast.LENGTH_SHORT).show()
    }

    fun loadProject(project: CuttingProject) {
        _activeProject.value = project
        saveActive()
        calculate(navigateToResults = true)
    }

    fun deleteProjectFromHistory(projectId: String) {
        projectRepo.deleteProject(projectId)
        _historyProjects.value = projectRepo.getSavedProjects()
    }

    fun loadPreset(preset: CuttingProject) {
        _activeProject.value = preset.copy(id = java.util.UUID.randomUUID().toString())
        saveActive()
        calculate(navigateToResults = true)
    }

    fun setThemeMode(mode: AppThemeMode) {
        settingsRepo.setThemeMode(mode)
    }

    fun copyCuttingList() {
        val result = _optimizationResult.value ?: return
        val text = PlanExporter.generateSimpleRequirementsText(
            stockLength = _activeProject.value.stockBoards.firstOrNull()?.length ?: 0.0,
            pieces = _activeProject.value.requiredPieces,
            unit = result.unit
        )
        copyToClipboard("BoardCut Requirements", text)
        Toast.makeText(getApplication(), "Cutting list copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun copyCuttingPlan() {
        val result = _optimizationResult.value ?: return
        val text = PlanExporter.generateCuttingListText(result, _activeProject.value.name)
        copyToClipboard("BoardCut Plan", text)
        Toast.makeText(getApplication(), "Complete plan copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun sharePlanText() {
        val result = _optimizationResult.value ?: return
        val text = PlanExporter.generateCuttingListText(result, _activeProject.value.name)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "BoardCut Cutting Plan - ${_activeProject.value.name}")
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Cutting Plan").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        getApplication<Application>().startActivity(shareIntent)
    }

    fun sharePlanCsv() {
        val result = _optimizationResult.value ?: return
        val csv = PlanExporter.generateCsv(result, _activeProject.value.name)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, csv)
            putExtra(Intent.EXTRA_SUBJECT, "BoardCut Plan CSV - ${_activeProject.value.name}")
            type = "text/csv"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Plan CSV").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        getApplication<Application>().startActivity(shareIntent)
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    private fun saveActive() {
        projectRepo.saveActiveProject(_activeProject.value)
    }
}
