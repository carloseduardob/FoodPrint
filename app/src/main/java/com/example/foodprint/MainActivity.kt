package com.example.foodprint

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.foodprint.ui.screens.LoginScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

@Composable
fun App() {
    val context = LocalContext.current

    MaterialTheme {
        Surface {
            LoginScreen { email, password ->
                // Temporary login action (just shows a toast)
                Toast.makeText(
                    context,
                    "Email: $email\nPassword: $password",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}