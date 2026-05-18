package com.example.quicksharepro.ui.screens.send

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.quicksharepro.ui.components.PermissionGate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTransfer: () -> Unit,
    viewModel: SendViewModel = hiltViewModel()
) {
    PermissionGate {
        SendContent(
            onNavigateBack = onNavigateBack,
            onNavigateToTransfer = onNavigateToTransfer,
            viewModel = viewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SendContent(
    onNavigateBack: () -> Unit,
    onNavigateToTransfer: () -> Unit,
    viewModel: SendViewModel
) {
    val isWaiting by viewModel.isWaiting.collectAsState()
    val isWifiEnabled by viewModel.isWifiEnabled.collectAsState()
    val isLocationEnabled by viewModel.isLocationEnabled.collectAsState()
    val currentTransfer by viewModel.currentTransfer.collectAsState(initial = null)
    
    val qrCodeData by viewModel.qrCodeData.collectAsState()
    val qrBitmap = rememberQrBitmap(qrCodeData)
    
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(isWifiEnabled, isLocationEnabled) {
        if (isWifiEnabled && isLocationEnabled) {
            viewModel.startHotspot()
        }
    }

    LaunchedEffect(currentTransfer?.status) {
        val status = currentTransfer?.status
        if (status == com.example.quicksharepro.domain.model.TransferStatus.TRANSFERRING || 
            status == com.example.quicksharepro.domain.model.TransferStatus.COMPLETED) {
            onNavigateToTransfer()
        }
    }

    if (currentTransfer?.status == com.example.quicksharepro.domain.model.TransferStatus.AWAITING_AUTHORIZATION) {
        AlertDialog(
            onDismissRequest = { viewModel.rejectConnection() },
            shape = RoundedCornerShape(28.dp),
            title = { Text("Approve Connection", style = MaterialTheme.typography.titleLarge) },
            text = { Text("${currentTransfer?.peerDevice?.deviceName ?: "A device"} wants to receive your files.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(onClick = { viewModel.acceptConnection() }, shape = RoundedCornerShape(12.dp)) {
                    Text("Allow")
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
                title = { Text("Send Files", style = MaterialTheme.typography.titleMedium) },
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

            Spacer(modifier = Modifier.weight(0.1f))

            if (isWaiting && isWifiEnabled && isLocationEnabled) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    if (qrBitmap != null) {
                        Surface(
                            modifier = Modifier.size(300.dp),
                            shape = RoundedCornerShape(40.dp),
                            color = Color.White,
                            tonalElevation = 8.dp,
                            shadowElevation = 16.dp,
                            border = androidx.compose.foundation.BorderStroke(2.dp, com.example.quicksharepro.ui.theme.SendColor.copy(alpha = 0.2f))
                        ) {
                            androidx.compose.foundation.Image(
                                bitmap = qrBitmap,
                                contentDescription = "Scan this QR Code",
                                modifier = Modifier.fillMaxSize().padding(32.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(48.dp))
                        
                        Text(
                            text = "Scan to Connect",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Keep this screen open until the receiver connects.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 12.dp).padding(horizontal = 32.dp)
                        )
                    } else {
                        WaitingAnimation()
                        Spacer(modifier = Modifier.height(40.dp))
                        Text(
                            text = "Initializing...",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Setting up your secure sharing hotspot.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 12.dp).padding(horizontal = 48.dp)
                        )
                    }
                }
            } else if (!isWifiEnabled || !isLocationEnabled) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Setup Required",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(0.1f))
        }
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
                message = "Enable Wi-Fi Direct for sending",
                icon = Icons.Default.WifiOff,
                onAction = onRequestWifi
            )
        }
        if (!isLocationEnabled) {
            HardwareBanner(
                title = "Location Required",
                message = "Needed for device pairing",
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

@Composable
fun WaitingAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "waiting")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
        Surface(
            modifier = Modifier.fillMaxSize().scale(scale),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
            shape = RoundedCornerShape(64.dp)
        ) {}
        Surface(
            modifier = Modifier.size(100.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(40.dp)
        ) {}
        Icon(
            imageVector = Icons.Default.Wifi,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun rememberQrBitmap(data: String?, size: Int = 512): androidx.compose.ui.graphics.ImageBitmap? {
    return remember(data) {
        if (data == null) return@remember null
        try {
            val bitMatrix = com.google.zxing.MultiFormatWriter().encode(data, com.google.zxing.BarcodeFormat.QR_CODE, size, size)
            val bmp = com.journeyapps.barcodescanner.BarcodeEncoder().createBitmap(bitMatrix)
            bmp.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}
