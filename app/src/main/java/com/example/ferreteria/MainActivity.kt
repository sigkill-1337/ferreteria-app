package com.example.ferreteria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ferreteria.ui.AppFerreteria
import com.example.ferreteria.ui.theme.FerreteriaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FerreteriaTheme {
                AppFerreteria()
            }
        }
    }
}
