package com.example.quicksharepro.ui.screens.transfer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.quicksharepro.domain.model.TransferStatus
import com.example.quicksharepro.ui.screens.filepicker.formatFileSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferProgressScreen(
    onNavigateHome: () -> Unit,
    onRetry: (isReceiver: Boolean) -> Unit,
    viewModel: TransferViewModel = hiltViewModel()
) {
    val session by viewModel.currentTransfer.collectAsState()
    val progress by viewModel.progress.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Transfer Status", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateHome) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
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
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            session?.let { currentSession ->
                val statusText = when (currentSession.status) {
                    TransferStatus.COMPLETED -> "Transfer Complete"
                    TransferStatus.FAILED -> "Transfer Failed"
                    TransferStatus.CANCELLED -> "Transfer Cancelled"
                    else -> "Transferring Files..."
                }

                val isProcessing = currentSession.status != TransferStatus.COMPLETED && 
                                  currentSession.status != TransferStatus.FAILED && 
                                  currentSession.status != TransferStatus.CANCELLED

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TransferProgressBar(
                            progress = if (currentSession.totalSize > 0) {
                                ((progress?.totalBytesTransferred?.toDouble() ?: 0.0) / currentSession.totalSize.toDouble()).coerceIn(0.0, 1.0).toFloat()
                            } else 0f
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (currentSession.files.isNotEmpty()) {
                            Text(
                                text = "${currentSession.files.size} files • ${formatFileSize(currentSession.totalSize)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (currentSession.status == TransferStatus.FAILED && currentSession.errorMessage != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = currentSession.errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    StatCard("Speed", formatSpeed(progress?.currentSpeed ?: 0.0), Icons.Default.Speed, Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(16.dp))
                    StatCard("Remaining", formatDuration(progress?.estimatedTimeRemaining ?: 0), Icons.Default.Timer, Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (isProcessing) {
                    Button(
                        onClick = { viewModel.cancelTransfer(currentSession.sessionId) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel Transfer", style = MaterialTheme.typography.labelLarge)
                    }
                } else if (currentSession.status == TransferStatus.FAILED) {
                    Column {
                        Button(
                            onClick = {
                                val isReceiver = currentSession.isReceiver
                                viewModel.clearState()
                                onRetry(isReceiver)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry Connection", style = MaterialTheme.typography.labelLarge)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.clearState()
                                onNavigateHome()
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Back to Dashboard", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.clearState()
                            onNavigateHome()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Back to Dashboard", style = MaterialTheme.typography.labelLarge)
                    }
                }
            } ?: LoadingState()
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            strokeCap = StrokeCap.Round, 
            strokeWidth = 6.dp, 
            modifier = Modifier.size(72.dp),
            color = com.example.quicksharepro.ui.theme.SendColor
        )
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Initializing...",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Setting up secure connection group",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TransferProgressBar(progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "progress"
    )
    
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = com.example.quicksharepro.ui.theme.SendColor)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(com.example.quicksharepro.ui.theme.SendColor, com.example.quicksharepro.ui.theme.ReceiveColor)
                        )
                    )
            )
        }
    }
}

fun formatSpeed(bytesPerSecond: Double): String {
    return "${formatFileSize(bytesPerSecond.toLong())}/s"
}

fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}
