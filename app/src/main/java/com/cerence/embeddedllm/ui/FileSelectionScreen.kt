package com.cerence.embeddedllm.ui

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

const val MODEL_FILE_NAME = "model.gguf"
const val ADAPTER_FILE_NAME = "adapter.gguf"
private const val TAG = "FileSelectionScreen"

@Composable
fun FileSelectionScreen(
    modifier: Modifier = Modifier,
    onFileSelected: (String, String?) -> Unit
) {
    var selectedModelFileName by remember { mutableStateOf<String?>(null) }
    var selectedAdapterFileName by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        checkFilePath(context, onFileSelected)
    }
    // Register for activity result
    val modelFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            selectedModelFileName = getFileName(context, uri)
            loading = true
            withContext(Dispatchers.IO) {
                copyFileToAppPrivateDir(context, uri, MODEL_FILE_NAME)
            }
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Model: " + (selectedModelFileName
                ?: "No file selected") + "\nAdapter:" + (selectedAdapterFileName
                ?: "No file selected"),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(enabled = !loading, onClick = { modelFilePickerLauncher.launch("*/*") }) {
            if (loading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Extracting model")
                }
            } else {
                Text("Choose Model File")
            }
        }

        val adapterFilePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri == null) return@rememberLauncherForActivityResult
            scope.launch {
                selectedAdapterFileName = getFileName(context, uri)
                loading = true
                withContext(Dispatchers.IO) {
                    copyFileToAppPrivateDir(context, uri, ADAPTER_FILE_NAME)
                }
                loading = false
            }
        }
        Button(enabled = !loading, onClick = { adapterFilePickerLauncher.launch("*/*") }) {
            if (loading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Extracting Adapter")
                }
            } else {
                Text("Choose Adapter File")
            }
        }
        Button(enabled = File(context.filesDir, MODEL_FILE_NAME).exists(), onClick = {
            checkFilePath(context, onFileSelected)
        }) {
            Text(text = "Next")
        }
    }
}

private fun checkFilePath(
    context: Context,
    onFileSelected: (String, String?) -> Unit
) {
    val adapterFile = File(context.filesDir, ADAPTER_FILE_NAME)
    var adapterPath: String? = null
    if (adapterFile.exists()) {
        adapterPath = adapterFile.absolutePath
    }
    val modelFile = File(context.filesDir, MODEL_FILE_NAME)
    if (modelFile.exists()) {
        onFileSelected(modelFile.absolutePath, adapterPath)
    }
}

// Helper function to get the file name from URI
fun getFileName(context: Context, uri: Uri): String {
    var name = ""
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        name = cursor.getString(nameIndex)
    }
    return name
}

fun copyFileToAppPrivateDir(context: Context, uri: Uri, fileName: String): String? {
    val inputStream = context.contentResolver.openInputStream(uri) ?: return null
    val outputFile = File(
        context.filesDir,
        fileName
    ) // Create a temporary file in the app's privatedirectory
    val outputStream = FileOutputStream(outputFile)

    inputStream.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }

    return outputFile.absolutePath
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
@Preview
fun FileSelectionScreenPreview() {
    Scaffold {
        FileSelectionScreen { _, _ ->
        }
    }

}