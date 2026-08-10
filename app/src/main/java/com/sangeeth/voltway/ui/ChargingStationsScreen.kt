package com.sangeeth.voltway.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.geojson.Point
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.ResourceOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.sangeeth.voltway.BuildConfig
import com.sangeeth.voltway.model.Feature

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargingStationsScreen(
    stations: List<Feature>,
    selectedLocation: com.mapbox.geojson.Point?,
    onNavigateTo: (Double, Double) -> Unit,
) {
    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 160.dp,
        sheetContainerColor = Color(0xFF1C1C1E),
        sheetContentColor = Color.White,
        containerColor = Color.Transparent,
        sheetContent = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                item {
                    Text(
                        text = "Nearby Stations (${stations.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                items(stations) { feature ->
                    StationCard(feature = feature, onNavigate = { lat, lng ->
                        onNavigateTo(lat, lng)
                    })
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    val mapInitOptions = MapInitOptions(
                        context = context,
                        resourceOptions = ResourceOptions.Builder()
                            .accessToken(BuildConfig.MAPBOX_ACCESS_TOKEN)
                            .build()
                    )

                    MapView(context, mapInitOptions).apply {
                        getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
                            location.updateSettings {
                                enabled = true
                                locationPuck = location.createDefault2DPuck(context, withBearing = true)
                            }
                        }

                        getMapboxMap().setCamera(
                            CameraOptions.Builder()
                                .center(selectedLocation ?: Point.fromLngLat(-122.0, 37.0))
                                .zoom(11.0)
                                .build()
                        )
                    }
                },
                update = { mapView ->
                    selectedLocation?.let { point ->
                        mapView.getMapboxMap().flyTo(
                            CameraOptions.Builder()
                                .center(point)
                                .zoom(14.0)
                                .build()
                        )
                    }
                }
            )
        }
    }
}


@Composable
fun StationCard(
    feature: Feature,
    onNavigate: (Double, Double) -> Unit
) {
    val props = feature.properties
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = props.name ?: "Charging Station",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFFFF)
                    )
                    Text(
                        text = props.fullAddress ?: "Address unavailable",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8E8E93)
                    )
                }
                // Availability Indicator (Mocking for now as EV API is restricted)
                Surface(
                    color = Color(0xFF34C759).copy(alpha = 0.2f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "AVAILABLE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF34C759),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (props.metadata?.phone != null || props.metadata?.website != null) {
                Text(
                    text = props.metadata?.phone ?: props.metadata?.website ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF0A84FF)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val distance = props.distance ?: 0.0
                Text(
                    text = "Distance: ${"%.2f".format(distance / 1000.0)} km",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        val lat = props.coordinates?.latitude ?: feature.geometry.coordinates[1]
                        val lng = props.coordinates?.longitude ?: feature.geometry.coordinates[0]
                        onNavigate(lat, lng)
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Navigate", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}