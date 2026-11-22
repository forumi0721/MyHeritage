package kr.stonecold.myheritage

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import kr.stonecold.myheritage.notification.NotificationScheduler
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

        // 백그라운드 알림 주기적 작업 등록 (WorkManager)
        NotificationScheduler.scheduleNotificationWork(applicationContext)

        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeModeState.intValue) {
                1 -> false
                2 -> true
                else -> systemDark
            }

            // Android 13 이상 상단바 알림 권한 팝업 요청
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { }
            )

            LaunchedEffect(Unit) {
                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
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
