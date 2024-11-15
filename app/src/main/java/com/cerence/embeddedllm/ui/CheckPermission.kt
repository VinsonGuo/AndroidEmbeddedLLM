package com.cerence.embeddedllm.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.cerence.embeddedllm.BuildConfig

@Composable
fun CheckPermission() {
    val activity = LocalContext.current as? Activity
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(false) }
    // 权限请求启动器
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grantedMap ->
        val isGranted = grantedMap.values.all { it }
        permissionGranted = isGranted
        if (!isGranted) {
            showPermissionDialog = true
        }
    }

    // 检查权限
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        )
    }
    // 显示权限提示弹窗
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        )
                    )
                }) {
                    Text("Try Again")
                }
            },
            dismissButton = {
                TextButton(onClick = { activity?.finish() }) {
                    Text("Cancel")
                }
            },
            title = { Text("Permission Required") },
            text = { Text("This app requires microphone permission to function properly. Please grant the permission.") }
        )
    }
}