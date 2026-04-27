package fi.developer.mapbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import fi.developer.mapbox.ui.theme.MapBoxTheme

val locationName = mutableStateOf("none")
val locationAddress = mutableStateOf("none")
val locationPhoneNumber = mutableStateOf("none")

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val sheetState = rememberModalBottomSheetState()
            var showBottomSheet by remember { mutableStateOf(false) }
            MapBoxTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(Modifier.padding(innerPadding)) {
                        val markerImage = rememberIconImage(
                            key = R.drawable.outline_add_location_24,
                            painter = painterResource(R.drawable.outline_add_location_24)
                        )

                        MapboxMap(
                            Modifier.fillMaxSize(),
                            mapViewportState = rememberMapViewportState {
                                setCameraOptions {
                                    zoom(2.0)
                                    center(Point.fromLngLat(-98.0, 39.5))
                                    pitch(0.0)
                                    bearing(0.0)
                                }
                            },
                            scaleBar = {
                                ScaleBar(Modifier.padding(top = 60.dp))
                            },
                            logo = {
                                Logo(Modifier.padding(bottom = 40.dp))
                            },
                            attribution = {
                                Attribution(Modifier.padding(bottom = 40.dp))
                            }) {
                            val geoJson = assets.open("coffee_shops.geojson").bufferedReader()
                                .use { it.readText() }
                            val featureCollection = FeatureCollection.fromJson(geoJson)

                            featureCollection.features()?.forEach { feature ->
                                val geometry = feature.geometry()
                                if (geometry is Point) {
                                    val properties = feature.properties()
                                    val jsonObjectStoreName = properties?.get("name")?.asString
                                    val jsonObjectAddress = properties?.get("address")?.asString
                                    val jsonObjectPhoneNumber = properties?.get("phone")?.asString

                                    PointAnnotation(point = geometry) {
                                        iconImage = markerImage
                                        interactionsState.onClicked {
                                            locationName.value = jsonObjectStoreName.toString()
                                            locationAddress.value = jsonObjectAddress.toString()
                                            locationPhoneNumber.value =
                                                jsonObjectPhoneNumber.toString()
                                            showBottomSheet = true
                                            true
                                        }
                                    }
                                }
                            }
                        }
                        if (showBottomSheet) {
                            ModalBottomSheet(
                                onDismissRequest = {
                                    showBottomSheet = false

                                }, sheetState = sheetState
                            ) {
                                // Aligns the contents of the ModalBottomSheet for better readability
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // These values update when the marker is selected by querying the GeoJSON
                                    // for the related data, and then updating the global variables so the text prints correctly here.
                                    Text(locationName.value)
                                    Text(locationAddress.value)
                                    Text(locationPhoneNumber.value)

                                    // Adds padding at the bottom of the ModalBottomSheet
                                    Spacer(modifier = Modifier.padding(vertical = 50.dp))

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MapBoxTheme {}
}