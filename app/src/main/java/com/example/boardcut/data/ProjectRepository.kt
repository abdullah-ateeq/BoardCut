package com.example.boardcut.data

import android.content.Context
import android.content.SharedPreferences
import com.example.boardcut.model.CalculationSettings
import com.example.boardcut.model.CutPiece
import com.example.boardcut.model.CuttingProject
import com.example.boardcut.model.StockBoard
import com.example.boardcut.model.UnitOfMeasurement
import org.json.JSONArray
import org.json.JSONObject

class ProjectRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("boardcut_projects", Context.MODE_PRIVATE)

    fun saveActiveProject(project: CuttingProject) {
        val json = projectToJson(project)
        prefs.edit().putString(KEY_ACTIVE_PROJECT, json.toString()).apply()
    }

    fun loadActiveProject(): CuttingProject {
        val jsonStr = prefs.getString(KEY_ACTIVE_PROJECT, null)
        if (jsonStr != null) {
            try {
                return jsonToProject(JSONObject(jsonStr))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Fallback default project
        return SampleProjects.getSamples().first()
    }

    fun getSavedProjects(): List<CuttingProject> {
        val jsonStr = prefs.getString(KEY_SAVED_PROJECTS, null) ?: return emptyList()
        val list = mutableListOf<CuttingProject>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                list.add(jsonToProject(array.getJSONObject(i)))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveProjectToHistory(project: CuttingProject) {
        val currentList = getSavedProjects().toMutableList()
        // Replace if exists with same ID or add to top
        val existingIndex = currentList.indexOfFirst { it.id == project.id }
        if (existingIndex >= 0) {
            currentList[existingIndex] = project.copy(createdAt = System.currentTimeMillis())
        } else {
            currentList.add(0, project.copy(createdAt = System.currentTimeMillis()))
        }
        // Limit history to 30 items
        val trimmed = currentList.take(30)
        val array = JSONArray()
        for (item in trimmed) {
            array.put(projectToJson(item))
        }
        prefs.edit().putString(KEY_SAVED_PROJECTS, array.toString()).apply()
    }

    fun deleteProject(projectId: String) {
        val currentList = getSavedProjects().filterNot { it.id == projectId }
        val array = JSONArray()
        for (item in currentList) {
            array.put(projectToJson(item))
        }
        prefs.edit().putString(KEY_SAVED_PROJECTS, array.toString()).apply()
    }

    private fun projectToJson(project: CuttingProject): JSONObject {
        val obj = JSONObject()
        obj.put("id", project.id)
        obj.put("name", project.name)
        obj.put("createdAt", project.createdAt)
        obj.put("unit", project.unit.symbol)

        val stockArray = JSONArray()
        for (sb in project.stockBoards) {
            val sObj = JSONObject()
            sObj.put("id", sb.id)
            sObj.put("name", sb.name)
            sObj.put("length", sb.length)
            sb.width?.let { sObj.put("width", it) }
            sb.thickness?.let { sObj.put("thickness", it) }
            sb.quantity?.let { sObj.put("quantity", it) }
            stockArray.put(sObj)
        }
        obj.put("stockBoards", stockArray)

        val piecesArray = JSONArray()
        for (cp in project.requiredPieces) {
            val pObj = JSONObject()
            pObj.put("id", cp.id)
            pObj.put("label", cp.label)
            pObj.put("length", cp.length)
            pObj.put("quantity", cp.quantity)
            piecesArray.put(pObj)
        }
        obj.put("requiredPieces", piecesArray)

        val settingsObj = JSONObject()
        settingsObj.put("kerf", project.settings.kerf)
        settingsObj.put("trimAllowance", project.settings.trimAllowance)
        settingsObj.put("minUsableOffcut", project.settings.minUsableOffcut)
        obj.put("settings", settingsObj)

        return obj
    }

    private fun jsonToProject(obj: JSONObject): CuttingProject {
        val id = obj.optString("id", "")
        val name = obj.optString("name", "Project")
        val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
        val unit = UnitOfMeasurement.fromSymbol(obj.optString("unit", "mm"))

        val stockBoards = mutableListOf<StockBoard>()
        val stockArray = obj.optJSONArray("stockBoards")
        if (stockArray != null) {
            for (i in 0 until stockArray.length()) {
                val sObj = stockArray.getJSONObject(i)
                stockBoards.add(
                    StockBoard(
                        id = sObj.optString("id"),
                        name = sObj.optString("name", "Stock Board"),
                        length = sObj.optDouble("length", 2400.0),
                        width = if (sObj.has("width")) sObj.getDouble("width") else null,
                        thickness = if (sObj.has("thickness")) sObj.getDouble("thickness") else null,
                        quantity = if (sObj.has("quantity")) sObj.getInt("quantity") else null
                    )
                )
            }
        }
        if (stockBoards.isEmpty()) {
            stockBoards.add(StockBoard())
        }

        val pieces = mutableListOf<CutPiece>()
        val piecesArray = obj.optJSONArray("requiredPieces")
        if (piecesArray != null) {
            for (i in 0 until piecesArray.length()) {
                val pObj = piecesArray.getJSONObject(i)
                pieces.add(
                    CutPiece(
                        id = pObj.optString("id"),
                        label = pObj.optString("label", ""),
                        length = pObj.optDouble("length", 0.0),
                        quantity = pObj.optInt("quantity", 1)
                    )
                )
            }
        }

        val settingsObj = obj.optJSONObject("settings")
        val settings = if (settingsObj != null) {
            CalculationSettings(
                kerf = settingsObj.optDouble("kerf", unit.defaultKerf),
                trimAllowance = settingsObj.optDouble("trimAllowance", unit.defaultTrim),
                minUsableOffcut = settingsObj.optDouble("minUsableOffcut", 200.0)
            )
        } else {
            CalculationSettings(
                kerf = unit.defaultKerf,
                trimAllowance = unit.defaultTrim
            )
        }

        return CuttingProject(
            id = id,
            name = name,
            createdAt = createdAt,
            unit = unit,
            stockBoards = stockBoards,
            requiredPieces = pieces,
            settings = settings
        )
    }

    companion object {
        private const val KEY_ACTIVE_PROJECT = "active_project"
        private const val KEY_SAVED_PROJECTS = "saved_projects"
    }
}
