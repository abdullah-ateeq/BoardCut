package com.example.boardcut.data

import android.content.Context
import android.content.SharedPreferences
import com.example.boardcut.model.UnitOfMeasurement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("boardcut_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(getThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _defaultUnit = MutableStateFlow(getDefaultUnit())
    val defaultUnit: StateFlow<UnitOfMeasurement> = _defaultUnit.asStateFlow()

    fun getThemeMode(): AppThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        return try {
            AppThemeMode.valueOf(name)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun getDefaultUnit(): UnitOfMeasurement {
        val symbol = prefs.getString(KEY_DEFAULT_UNIT, UnitOfMeasurement.MM.symbol) ?: UnitOfMeasurement.MM.symbol
        return UnitOfMeasurement.fromSymbol(symbol)
    }

    fun setDefaultUnit(unit: UnitOfMeasurement) {
        prefs.edit().putString(KEY_DEFAULT_UNIT, unit.symbol).apply()
        _defaultUnit.value = unit
    }

    companion object {
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_DEFAULT_UNIT = "pref_default_unit"
    }
}
