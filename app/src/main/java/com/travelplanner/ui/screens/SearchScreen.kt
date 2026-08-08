package com.travelplanner.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelplanner.R
import com.travelplanner.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    onNavigateToStationBoard: (crs: String, stationName: String, formattedDateTime: String?) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedTime by viewModel.selectedTime.collectAsState()
    val isCustomSearchActive by viewModel.isCustomSearchActive.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Travel Planner", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
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
                .padding(16.dp)
        ) {
            // Header card with Train Logo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1.0f)) {
                        Text(
                            text = "Where are you heading?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Find departures & arrivals per platform",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.train),
                        contentDescription = "Train Logo",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Search Bar & Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Search Station Name or CRS Code") },
                placeholder = { Text("e.g. Clapham Junction or CLJ") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear Search")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Suggestions List
            if (suggestions.isNotEmpty()) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .heightIn(max = 200.dp)
                ) {
                    LazyColumn {
                        items(suggestions) { station ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.onSearchQueryChanged(station.name)
                                        val dateTimeStr = if (isCustomSearchActive) viewModel.getFormattedDateTimeString() else null
                                        onNavigateToStationBoard(station.crs, station.name, dateTimeStr)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Train,
                                        contentDescription = "Train Icon",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    Text(
                                        text = station.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = station.crs,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = MaterialTheme.shapes.small
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons (Search typing closest match)
            Button(
                onClick = {
                    val match = viewModel.getClosestMatch(searchQuery)
                    if (match != null) {
                        viewModel.onSearchQueryChanged(match.name)
                        val dateTimeStr = if (isCustomSearchActive) viewModel.getFormattedDateTimeString() else null
                        onNavigateToStationBoard(match.crs, match.name, dateTimeStr)
                    }
                },
                enabled = searchQuery.trim().isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.DirectionsTransit, contentDescription = "Go", modifier = Modifier.padding(end = 8.dp))
                Text("Search Location", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Future Search Toggle and controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = "Schedule Search",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column {
                        Text("Search Future Times", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Change search date or time", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
                Switch(
                    checked = isCustomSearchActive,
                    onCheckedChange = { active ->
                        if (active) {
                            viewModel.setCustomDate(LocalDate.now())
                            viewModel.setCustomTime(LocalTime.now())
                        } else {
                            viewModel.resetToCurrentDateTime()
                        }
                    }
                )
            }

            AnimatedVisibility(visible = isCustomSearchActive) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Date selector
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp)
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = "Date")
                            Spacer(Modifier.width(4.dp))
                            Text(selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                        }

                        // Time selector
                        OutlinedButton(
                            onClick = { showTimePicker = true },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp)
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = "Time")
                            Spacer(Modifier.width(4.dp))
                            Text(selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                        }
                    }

                    // Reset button
                    TextButton(
                        onClick = { viewModel.resetToCurrentDateTime() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Reset to Current Time", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Favorited Stations List Section
            Text(
                text = "Favorite Stations",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (favorites.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "No Favorites",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No favorite stations added yet.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(favorites) { fav ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    val dateTimeStr = if (isCustomSearchActive) viewModel.getFormattedDateTimeString() else null
                                    onNavigateToStationBoard(fav.crs, fav.name, dateTimeStr)
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Favorite Star",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    Column {
                                        Text(fav.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                        Text("CRS: ${fav.crs}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                                IconButton(onClick = { viewModel.toggleFavorite(fav.crs, fav.name) }) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Remove Favorite",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Custom inline Date Dialog
    if (showDatePicker) {
        var tempDay by remember { mutableStateOf(selectedDate.dayOfMonth) }
        var tempMonth by remember { mutableStateOf(selectedDate.monthValue) }
        var tempYear by remember { mutableStateOf(selectedDate.year) }

        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Select Search Date") },
            text = {
                Column {
                    Text("Day: $tempDay / Month: $tempMonth / Year: $tempYear")
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Button(onClick = { if (tempDay < 31) tempDay++ }) { Text("+ Day") }
                            Button(onClick = { if (tempDay > 1) tempDay-- }) { Text("- Day") }
                        }
                        Column {
                            Button(onClick = { if (tempMonth < 12) tempMonth++ }) { Text("+ Month") }
                            Button(onClick = { if (tempMonth > 1) tempMonth-- }) { Text("- Month") }
                        }
                        Column {
                            Button(onClick = { tempYear++ }) { Text("+ Year") }
                            Button(onClick = { tempYear-- }) { Text("- Year") }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    try {
                        viewModel.setCustomDate(LocalDate.of(tempYear, tempMonth, tempDay))
                    } catch (e: Exception) {
                        // Fallback to today on invalid dates (e.g. Feb 31)
                        viewModel.setCustomDate(LocalDate.now())
                    }
                    showDatePicker = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Custom inline Time Dialog
    if (showTimePicker) {
        var tempHour by remember { mutableStateOf(selectedTime.hour) }
        var tempMinute by remember { mutableStateOf(selectedTime.minute) }

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Search Time") },
            text = {
                Column {
                    Text("Time: ${String.format("%02d:%02d", tempHour, tempMinute)}")
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Hour", fontWeight = FontWeight.Bold)
                            Button(onClick = { tempHour = (tempHour + 1) % 24 }) { Text("+") }
                            Button(onClick = { tempHour = if (tempHour > 0) tempHour - 1 else 23 }) { Text("-") }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Minute", fontWeight = FontWeight.Bold)
                            Button(onClick = { tempMinute = (tempMinute + 5) % 60 }) { Text("+5") }
                            Button(onClick = { tempMinute = if (tempMinute >= 5) tempMinute - 5 else 55 }) { Text("-5") }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.setCustomTime(LocalTime.of(tempHour, tempMinute))
                    showTimePicker = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
