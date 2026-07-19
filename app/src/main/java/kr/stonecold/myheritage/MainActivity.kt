package kr.stonecold.myheritage

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import kr.stonecold.myheritage.theme.MyHeritageTheme

class MainActivity : ComponentActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private val themeModeState = mutableIntStateOf(0) // 0: 시스템, 1: 라이트, 2: 다크

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        if (key == "pref_theme_mode") {
            themeModeState.intValue = prefs.getInt("pref_theme_mode", 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("my_heritage_prefs", Context.MODE_PRIVATE)
        themeModeState.intValue = sharedPreferences.getInt("pref_theme_mode", 0)
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeModeState.intValue) {
                1 -> false
                2 -> true
                else -> systemDark
            }
            MyHeritageTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
