package com.travelplanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelplanner.model.api.ServiceLocation
import com.travelplanner.viewmodel.ServiceDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    uniqueIdentity: String,
    viewModel: ServiceDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val detail by viewModel.serviceDetail.collectAsState()

    LaunchedEffect(uniqueIdentity) {
        viewModel.loadServiceDetails(uniqueIdentity)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Service Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = error ?: "An error occurred",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(onClick = { viewModel.loadServiceDetails(uniqueIdentity) }) {
                        Text("Retry")
                    }
                }
            }
        } else if (detail != null) {
            val service = detail!!
            val meta = service.scheduleMetadata
            val stops = service.locations ?: emptyList()
            val reasons = service.reasons ?: emptyList()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header Information
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = meta?.operator?.name ?: "Unknown Operator",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Identity: ${meta?.identity ?: "TBC"} • Mode: ${meta?.modeType ?: "TRAIN"}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (meta?.uniqueIdentity != null) {
                            Text(
                                text = "Service UID: ${meta.uniqueIdentity}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                // Delay / Cancellation Reasons Banner Sourced from the reasons block
                if (reasons.isNotEmpty()) {
                    val delayReason = reasons.first()
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = delayReason.shortText ?: "Service Disruption",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                delayReason.longText?.let { longText ->
                                    Text(
                                        text = longText,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable Journey Timeline list of calling points
                Text(
                    text = "Route calling points",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                if (stops.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No calling points found.", color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        itemsIndexed(stops) { index, stop ->
                            CallingPointRow(
                                stop = stop,
                                isFirst = index == 0,
                                isLast = index == stops.size - 1
                            )
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No details available.", color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun CallingPointRow(
    stop: ServiceLocation,
    isFirst: Boolean,
    isLast: Boolean
) {
    val name = stop.location?.description ?: "Unknown Stop"
    val temporal = stop.temporalData
    val isCancelled = temporal?.arrival?.isCancelled == true || temporal?.departure?.isCancelled == true

    // Advertised time extraction
    val rawTime = temporal?.arrival?.scheduleAdvertised
        ?: temporal?.departure?.scheduleAdvertised
        ?: ""
    val formattedTime = if (rawTime.length >= 16) rawTime.substring(11, 16) else rawTime

    // Realtime estimated / actual time
    val rtTime = temporal?.arrival?.realtimeForecast
        ?: temporal?.departure?.realtimeForecast
        ?: temporal?.arrival?.realtimeActual
        ?: temporal?.departure?.realtimeActual
        ?: ""
    val formattedRtTime = if (rtTime.length >= 16) rtTime.substring(11, 16) else rtTime

    val lateness = temporal?.arrival?.realtimeAdvertisedLateness
        ?: temporal?.departure?.realtimeAdvertisedLateness
        ?: 0

    val platform = stop.locationMetadata?.platform?.actual
        ?: stop.locationMetadata?.platform?.planned
        ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Line-drawing Column for Timeline
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            // Top connecting line
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.primary)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Timeline Circle
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = if (isCancelled) Color.Red else MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )

            // Bottom connecting line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.primary)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Details Column
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .padding(start = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.0f)) {
                    Text(
                        text = name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isCancelled) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Sched: $formattedTime",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        if (formattedRtTime.isNotEmpty() && !isCancelled) {
                            Spacer(modifier = Modifier.width(8.dp))
                            val rtColor = if (lateness > 0) Color(0xFFD32F2F) else Color(0xFF388E3C)
                            Text(
                                text = "Act: $formattedRtTime (${if (lateness > 0) "+$lateness min" else "On Time"})",
                                fontSize = 12.sp,
                                color = rtColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                if (platform.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "Plat $platform",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
