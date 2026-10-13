package com.miyuki.timezones

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.miyuki.timezones.ui.MainScreen
import com.miyuki.timezones.ui.theme.TimezonesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            TimezonesTheme {
                MainScreen()
            }
        }
    }
}