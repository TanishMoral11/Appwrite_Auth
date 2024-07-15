package com.example.appwritetest

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Appwrite
        Appwrite.init(applicationContext)

        // Get SharedPreferences
        val sharedPref = getSharedPreferences("AppwriteTest", Context.MODE_PRIVATE)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val coroutineScope = rememberCoroutineScope()

                    // State variables
                    var user by remember { mutableStateOf("") }
                    var email by remember { mutableStateOf("") }
                    var password by remember { mutableStateOf("") }
                    var errorMessage by remember { mutableStateOf<String?>(null) }

                    // Check if user is already logged in
                    val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
                    if (isLoggedIn) {
                        user = sharedPref.getString("userEmail", "") ?: ""
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        errorMessage?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        }
                        if (user.isNotEmpty()) {
                            Text(text = "Logged in as $user")
                            Button(onClick = {
                                coroutineScope.launch {
                                    try {
                                        Appwrite.onLogout()
                                        user = ""
                                        errorMessage = null

                                        // Clear SharedPreferences
                                        with(sharedPref.edit()) {
                                            putBoolean("isLoggedIn", false)
                                            putString("userEmail", "")
                                            apply()
                                        }

                                        Log.d("MainActivity", "Logout successful")
                                    } catch (e: Exception) {
                                        Log.e("MainActivity", "Logout failed", e)
                                        errorMessage = "Logout failed: ${e.message}"
                                    }
                                }
                            }) {
                                Text("Logout")
                            }
                        } else {
                            TextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                            TextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(onClick = {
                                    coroutineScope.launch {
                                        try {
                                            Appwrite.onLogin(email, password)
                                            user = email
                                            errorMessage = null

                                            // Save login state to SharedPreferences
                                            with(sharedPref.edit()) {
                                                putBoolean("isLoggedIn", true)
                                                putString("userEmail", user)
                                                apply()
                                            }

                                            Log.d("MainActivity", "Login successful: $user")
                                        } catch (e: Exception) {
                                            Log.e("MainActivity", "Login failed", e)
                                            errorMessage = "Login failed: ${e.message}"
                                        }
                                    }
                                }) {
                                    Text("Login")
                                }
                                Button(onClick = {
                                    coroutineScope.launch {
                                        try {
                                            Appwrite.onRegister(email, password)
                                            errorMessage = null
                                            Log.d("MainActivity", "Registration successful")
                                        } catch (e: Exception) {
                                            Log.e("MainActivity", "Registration failed", e)
                                            errorMessage = "Registration failed: ${e.message}"
                                        }
                                    }
                                }) {
                                    Text("Register")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(), // Or darkColorScheme() if using dark theme
        typography = Typography(),
        content = content
    )
}
