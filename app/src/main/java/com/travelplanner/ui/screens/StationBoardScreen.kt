package com.travelplanner.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelplanner.model.api.LocationLineUpObject
import com.travelplanner.viewmodel.MainViewModel
import com.travelplanner.viewmodel.StationBoardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationBoardScreen(
    crs: String,
    stationName: String,
    formattedDateTime: String?,
    boardViewModel: StationBoardViewModel,
    mainViewModel: MainViewModel,
    onNavigateToServiceDetail: (uniqueIdentity: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val isLoading by boardViewModel.isLoading.collectAsState()
    val error by boardViewModel.error.collectAsState()

    val groupedArrivals by boardViewModel.groupedArrivals.collectAsState()
    val groupedDepartures by boardViewModel.groupedDepartures.collectAsState()

    val isFavorited by mainViewModel.isFavoriteFlow(crs).collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Departures, 1 = Arrivals

    // Initial load and reload when requested
    LaunchedEffect(crs, formattedDateTime) {
        boardViewModel.loadStationBoard(crs, formattedDateTime)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stationName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("CRS: $crs" + (formattedDateTime?.let { " • ${it.replace("T", " ")}" } ?: " • Live"), fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Favorite/Unfavorite action
                    IconButton(onClick = { mainViewModel.toggleFavorite(crs, stationName) }) {
                        Icon(
                            imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorited) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Refresh action
                    IconButton(onClick = { boardViewModel.loadStationBoard(crs, formattedDateTime) }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab row to switch between Departures and Arrivals
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Departures", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Arrivals", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = error ?: "Unknown error occurred",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(onClick = { boardViewModel.loadStationBoard(crs, formattedDateTime) }) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                val currentBoard = if (selectedTab == 0) groupedDepartures else groupedArrivals

                if (currentBoard.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No trains found in this time window.",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        currentBoard.forEach { (platform, services) ->
                            item {
                                // Platform Section Header
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp, bottom = 4.dp)
                                ) {
                                    Text(
                                        text = "Platform $platform",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            items(services) { service ->
                                TrainServiceItem(
                                    service = service,
                                    isArrivalTab = selectedTab == 1,
                                    onClick = {
                                        service.scheduleMetadata?.uniqueIdentity?.let { uid ->
                                            onNavigateToServiceDetail(uid)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrainServiceItem(
    service: LocationLineUpObject,
    isArrivalTab: Boolean,
    onClick: () -> Unit
) {
    val temporal = service.temporalData
    val isCancelled = temporal?.arrival?.isCancelled == true || temporal?.departure?.isCancelled == true
    val displayAs = temporal?.displayAs ?: "CALL"

    // Set time and status info
    val advertisedTime = if (isArrivalTab) {
        temporal?.arrival?.scheduleAdvertised ?: ""
    } else {
        temporal?.departure?.scheduleAdvertised ?: ""
    }

    // Advertised time usually is "2025-10-26T13:45:00Z" or similar. Let's extract HH:mm
    val formattedTime = if (advertisedTime.length >= 16) {
        advertisedTime.substring(11, 16)
    } else {
        advertisedTime
    }

    val lateness = if (isArrivalTab) {
        temporal?.arrival?.realtimeAdvertisedLateness ?: 0
    } else {
        temporal?.departure?.realtimeAdvertisedLateness ?: 0
    }

    val statusText: String
    val statusColor: Color

    if (isCancelled || displayAs == "CANCELLED") {
        statusText = "Cancelled"
        statusColor = Color.Red
    } else if (lateness > 0) {
        statusText = "$lateness min late"
        statusColor = Color(0xFFD32F2F) // Dark Red
    } else {
        statusText = "On Time"
        statusColor = Color(0xFF388E3C) // Dark Green
    }

    val originStr = service.origin?.firstOrNull()?.location?.description ?: "Unknown"
    val destStr = service.destination?.firstOrNull()?.location?.description ?: "Unknown"
    val routeText = if (isArrivalTab) "From: $originStr" else "To: $destStr"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.0f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedTime,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = statusText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = routeText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                service.scheduleMetadata?.operator?.name?.let { opName ->
                    Text(
                        text = opName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Details",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
