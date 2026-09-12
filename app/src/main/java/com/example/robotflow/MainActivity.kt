package com.example.robotflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.robotflow.ui.AppNavigation
import com.example.robotflow.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: AppViewModel = viewModel()
            AppNavigation(viewModel = viewModel)
        }
    }
}