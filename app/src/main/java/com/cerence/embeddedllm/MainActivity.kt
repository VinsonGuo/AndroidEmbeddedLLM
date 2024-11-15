package com.cerence.embeddedllm

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cerence.embeddedllm.ui.ChatScreen
import com.cerence.embeddedllm.ui.CheckPermission
import com.cerence.embeddedllm.ui.FileSelectionScreen
import com.cerence.embeddedllm.ui.LoadingDialog
import com.cerence.embeddedllm.ui.theme.EmbededLLMTheme

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CheckPermission()
            EmbededLLMTheme {
                AppNavigator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigator(viewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsState()
    Scaffold(
        topBar = { TopAppBar(title = { Text("My App") }) },
    ) { innerPadding ->
        LoadingDialog(isLoading = state.isLoadingModel)
        NavHost(
            navController = navController,
            startDestination = "file_selection",
            Modifier.padding(innerPadding) // Apply padding to prevent overlap with top/bottom bars
        ) {
            composable("file_selection") {
                FileSelectionScreen(
                    onFileSelected = {modelPath, adapterPath ->
                        viewModel.initModel(modelPath, adapterPath)
                        navController.navigate("chat")
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable("chat") {

                ChatScreen(
                    modifier = Modifier.padding(innerPadding),
                    messages = state.messages,
                    onSendMessage = {
                        viewModel.sendMessage(it)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppNavigator()
}