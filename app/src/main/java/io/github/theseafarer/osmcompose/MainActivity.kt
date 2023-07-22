/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.theseafarer.osmcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import io.github.theseafarer.osmcompose.ui.theme.OsmComposeTheme
import io.github.theseafarer.osmcompose.ui.theme.Purple40
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = "io.github.theseafarer.osmcompose"
        val polygonPoints = TehranPoints(resources).points
        setContent {
            OsmComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(Purple40)
                    ) {
                        var clicked by remember { mutableStateOf(false) }

                        val controllerState = rememberMapControllerState {
                            position = Position(GeoPoint(35.69555556, 51.37833333))
                        }
                        if (clicked) {
                            controllerState.position = controllerState.position.copy(zoom = 14.0)
                        } else {
                            controllerState.position = controllerState.position.copy(zoom = 12.0)
                        }

                        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                modifier = Modifier
                                    .weight(1f),
                                text = "OsmCompose",
                                style = MaterialTheme.typography.displaySmall
                            )
                            OsmMap(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(9f)
                                    .clip(
                                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                    ),
                                mapControllerState = controllerState
                            ) {
                                Marker(
                                    state = rememberMarkerState(
                                        position = GeoPoint(
                                            35.69555,
                                            51.37833
                                        )
                                    ),
                                    icon = ContextCompat.getDrawable(
                                        this@MainActivity,
                                        R.drawable.baseline_push_pin_24
                                    ),
                                    onClick = { _, _ -> clicked = !clicked; true })
                                Polyline(
                                    state = rememberPolylineState(
                                        points = listOf(
                                            GeoPoint(
                                                35.69555,
                                                51.37833
                                            ),
                                            GeoPoint(
                                                35.69083,
                                                51.38861
                                            )
                                        )
                                    ),
                                    outlinePaint = Paint().apply {
                                        color = Color.Red
                                        style = PaintingStyle.Stroke
                                        strokeWidth = 10f
                                        isAntiAlias = true
                                    }
                                )
                                Polygon(
                                    state = rememberPolygonState(
                                        points = polygonPoints
                                    ),
                                    fillPaint = Paint().apply {
                                        color = Color.Blue
                                        style = PaintingStyle.Fill
                                        alpha = 0.3f
                                    },
                                    outlinePaint = Paint().apply {
                                        color = Color.Blue
                                        alpha = 0.5f
                                        style = PaintingStyle.Stroke
                                        strokeWidth = 10f
                                        isAntiAlias = true
                                    }
                                )
                                Polygon(
                                    state = rememberPolygonState(
                                        points = pointsAsCircle(
                                            controllerState.position.center as GeoPoint,
                                            3000.0
                                        )
                                    ),
                                    fillPaint = Paint().apply {
                                        color = Color.Yellow
                                        style = PaintingStyle.Fill
                                        alpha = 0.3f
                                    },
                                    outlinePaint = Paint().apply {
                                        color = Color.Yellow
                                        alpha = 0.5f
                                        style = PaintingStyle.Stroke
                                        strokeWidth = 10f
                                        isAntiAlias = true
                                    }
                                )
                            }
                        }
                        if (clicked) {
                            Snackbar {
                                Text("Clicked! ${controllerState.position}")
                            }
                        }
                    }
                }
            }
        }
    }
}

val tehranPoints = listOf<GeoPoint>(

)