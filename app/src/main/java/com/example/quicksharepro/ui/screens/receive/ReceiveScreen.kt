package com.example.quicksharepro.ui.screens.receive

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.quicksharepro.ui.components.PermissionGate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTransfer: () -> Unit,
    viewModel: ReceiveViewModel = hiltViewModel()
) {
    PermissionGate {
        ReceiveContent(
            onNavigateBack = onNavigateBack,
            onNavigateToTransfer = onNavigateToTransfer,
            viewModel = viewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceiveContent(
    onNavigateBack: () -> Unit,
    onNavigateToTransfer: () -> Unit,
    viewModel: ReceiveViewModel
) {
    val isWaiting by viewModel.isWaiting.collectAsState()
    val isWifiEnabled by viewModel.isWifiEnabled.collectAsState()
    val isLocationEnabled by viewModel.isLocationEnabled.collectAsState()
    val isConnecting by viewModel.isConnecting.collectAsState()
    val connectionError by viewModel.connectionError.collectAsState()
    val isScannerEnabled by viewModel.isScannerEnabled.collectAsState()
    val currentTransfer by viewModel.currentTransfer.collectAsState(initial = null)
    
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(isWifiEnabled, isLocationEnabled) {
        if (isWifiEnabled && isLocationEnabled) {
            viewModel.startListening()
        }
    }

    LaunchedEffect(currentTransfer?.status) {
        val status = currentTransfer?.status
        if (status == com.example.quicksharepro.domain.model.TransferStatus.TRANSFERRING || 
            status == com.example.quicksharepro.domain.model.TransferStatus.COMPLETED ||
            status == com.example.quicksharepro.domain.model.TransferStatus.REQUESTED_CONNECTION ||
            status == com.example.quicksharepro.domain.model.TransferStatus.CONNECTING) {
            onNavigateToTransfer()
        }
    }

    if (isConnecting) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { }) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Linking to Sender...", style = MaterialTheme.typography.titleMedium)
                    Text("Ensure roles are correct.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (connectionError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text("Connection Failed") },
            text = { Text(connectionError ?: "Stale connection detected. Please try scanning again.") },
            confirmButton = {
                Button(onClick = { viewModel.dismissError() }, shape = RoundedCornerShape(12.dp)) {
                    Text("Retry Scan")
                }
            }
        )
    }

    if (currentTransfer?.status == com.example.quicksharepro.domain.model.TransferStatus.AWAITING_AUTHORIZATION) {
        // ... (existing authorization dialog is fine)
        AlertDialog(
            onDismissRequest = { viewModel.rejectConnection() },
            shape = RoundedCornerShape(28.dp),
            title = { Text("Incoming Request", style = MaterialTheme.typography.titleLarge) },
            text = { Text("${currentTransfer?.peerDevice?.deviceName ?: "A device"} wants to send you files.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(onClick = { viewModel.acceptConnection() }, shape = RoundedCornerShape(12.dp)) {
                    Text("Accept")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.rejectConnection() }) {
                    Text("Decline")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Scan to Receive", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HardwareBanners(
                isWifiEnabled = isWifiEnabled,
                isLocationEnabled = isLocationEnabled,
                onRequestWifi = { viewModel.requestWifiEnabled() },
                onRequestLocation = { activity?.let { com.example.quicksharepro.ui.utils.HardwareUtils.promptEnableLocation(it) } }
            )

            if (isWifiEnabled && isLocationEnabled) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (isScannerEnabled) {
                        key(connectionError, isWaiting) {
                            androidx.compose.ui.viewinterop.AndroidView(
                                factory = { ctx ->
                                    com.journeyapps.barcodescanner.CompoundBarcodeView(ctx).apply {
                                        val callback = object : com.journeyapps.barcodescanner.BarcodeCallback {
                                            override fun barcodeResult(result: com.journeyapps.barcodescanner.BarcodeResult?) {
                                                result?.text?.let { data ->
                                                    if (!isConnecting && connectionError == null) {
                                                        pause()
                                                        viewModel.connectFromQR(data) {
                                                            // Handled by LaunchedEffect
                                                        }
                                                    }
                                                }
                                            }
                                            override fun possibleResultPoints(resultPoints: MutableList<com.google.zxing.ResultPoint>?) {}
                                        }
                                        decodeContinuous(callback)
                                        resume()
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        // Show a placeholder when scanner is disabled to allow manual retry
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.WifiOff, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(100.dp))
                        }
                    }

                    // Scanner Overlay UI
                    ScannerOverlay()
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ready to Receive",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Scan the QR code on the sender's device to start the transfer session.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            } else {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Hardware Setup Required",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScannerOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val lineOffset by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lineOffset"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Corner markers (simplified)
        Canvas(modifier = Modifier.fillMaxSize().padding(48.dp)) {
            val strokeWidth = 4.dp.toPx()
            val cornerLen = 40.dp.toPx()
            
            // Top Left
            drawLine(Color.White, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(cornerLen, 0f), strokeWidth)
            drawLine(Color.White, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(0f, cornerLen), strokeWidth)
            
            // Top Right
            drawLine(Color.White, androidx.compose.ui.geometry.Offset(size.width, 0f), androidx.compose.ui.geometry.Offset(size.width - cornerLen, 0f), strokeWidth)
            drawLine(Color.White, androidx.compose.ui.geometry.Offset(size.width, 0f), androidx.compose.ui.geometry.Offset(size.width, cornerLen), strokeWidth)
            
            // Bottom Left
            drawLine(Color.White, androidx.compose.ui.geometry.Offset(0f, size.height), androidx.compose.ui.geometry.Offset(cornerLen, size.height), strokeWidth)
            drawLine(Color.White, androidx.compose.ui.geometry.Offset(0f, size.height), androidx.compose.ui.geometry.Offset(0f, size.height - cornerLen), strokeWidth)
            
            // Bottom Right
            drawLine(Color.White, androidx.compose.ui.geometry.Offset(size.width, size.height), androidx.compose.ui.geometry.Offset(size.width - cornerLen, size.height), strokeWidth)
            drawLine(Color.White, androidx.compose.ui.geometry.Offset(size.width, size.height), androidx.compose.ui.geometry.Offset(size.width, size.height - cornerLen), strokeWidth)
        }

        // Scanning line
        Divider(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .align(Alignment.TopCenter)
                .offset(y = 10.dp) // Placeholder, used with translation
                .graphicsLayer { translationY = lineOffset * 600.dp.toPx() }, // Approximate height
            color = com.example.quicksharepro.ui.theme.ReceiveColor.copy(alpha = 0.5f),
            thickness = 2.dp
        )
    }
}

@Composable
fun HardwareBanners(
    isWifiEnabled: Boolean,
    isLocationEnabled: Boolean,
    onRequestWifi: () -> Unit,
    onRequestLocation: () -> Unit
) {
    Column {
        if (!isWifiEnabled) {
            HardwareBanner(
                title = "Wi-Fi Disabled",
                message = "Enable it to receive files",
                icon = Icons.Default.WifiOff,
                onAction = onRequestWifi
            )
        }
        if (!isLocationEnabled) {
            HardwareBanner(
                title = "Location Required",
                message = "Needed for device discovery",
                icon = Icons.Default.LocationOff,
                onAction = onRequestLocation
            )
        }
    }
}

@Composable
private fun HardwareBanner(
    title: String,
    message: String,
    icon: ImageVector,
    onAction: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onAction) {
                Text("Enable", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}


