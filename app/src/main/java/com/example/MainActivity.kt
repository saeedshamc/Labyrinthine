package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.ProgressRepository
import com.example.ui.maze.MazeScreen
import com.example.ui.maze.MazeViewModel
import com.example.ui.maze.MazeViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable premium modern Edge-to-Edge display (mandatory rule)
        enableEdgeToEdge()

        // Initialize local Room database and Progression Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val progressDao = database.progressDao()
        val repository = ProgressRepository(progressDao)

        // Instantiate MazeViewModel using our Custom Factory
        val viewModelFactory = MazeViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[MazeViewModel::class.java]

        setContent {
            MyApplicationTheme(darkTheme = viewModel.isDarkTheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    // Set up the primary screen container within edge-to-edge constraints
                    MazeScreen(viewModel = viewModel)
                }
            }
        }
    }
}
