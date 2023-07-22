/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.theseafarer.osmcompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.AndroidPaint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.OverlayManager
import org.osmdroid.views.overlay.Polygon

internal class PolygonNode(
    val overlays: OverlayManager,
    val polygon: Polygon,
    val polygonState: PolygonState,
    var onClick: (Polygon, MapView, GeoPoint) -> Boolean,
) : MapNode {
    override fun onCleared() {
        polygonState.polygon = null
        polygon.setOnClickListener(null)
        overlays.remove(polygon)
    }

    override fun onRemoved() {
        polygonState.polygon = null
        polygon.setOnClickListener(null)
        overlays.remove(polygon)
    }

    override fun onAttached() {
        polygonState.polygon = polygon
        polygon.setOnClickListener(onClick)
        overlays.add(polygon)
    }
}

class PolygonState(
    points: List<GeoPoint>,
    holes: List<List<GeoPoint>>
) {
    var points: List<GeoPoint> by mutableStateOf(points)
    var holes: List<List<GeoPoint>> by mutableStateOf(holes)

    private val polygonState: MutableState<Polygon?> = mutableStateOf(null)
    internal var polygon: Polygon?
        get() = polygonState.value
        set(value) {
            if (polygonState.value == null && value == null) return
            if (polygonState.value != null && value != null) {
                error("PolygonState may only be associated with one Polygon at a time.")
            }
            polygonState.value = value
        }

    companion object {
        val Saver: Saver<PolygonState, Pair<List<GeoPoint>, List<List<GeoPoint>>>> = Saver(
            save = { Pair(it.points, it.holes) },
            restore = { PolygonState(it.first, it.second) }
        )
    }
}

fun pointsAsCircle(center: GeoPoint, radiusInMeters: Double): List<GeoPoint> =
    Polygon.pointsAsCircle(center, radiusInMeters)

fun pointsAsRect(rect: BoundingBox): List<GeoPoint> =
    Polygon.pointsAsRect(rect) as List<GeoPoint>

fun pointsAsRect(center: GeoPoint, lengthInMeters: Double, widthInMeters: Double): List<GeoPoint> =
    Polygon.pointsAsRect(center, lengthInMeters, widthInMeters) as List<GeoPoint>


@Composable
fun rememberPolygonState(
    key: String? = null,
    points: List<GeoPoint> = emptyList(),
    holes: List<List<GeoPoint>> = emptyList()
): PolygonState = rememberSaveable(key = key, saver = PolygonState.Saver) {
    PolygonState(points, holes)
}

@OsmMapComposable
@Composable
fun Polygon(
    state: PolygonState,
    outlinePaint: Paint = AndroidPaint().apply {
        color = Color.Black
        strokeWidth = 10.0f
        style = PaintingStyle.Stroke
        isAntiAlias = true
    },
    fillPaint: Paint = AndroidPaint().apply {
        color = Color.Transparent
        style = PaintingStyle.Fill
    },
    snippet: String? = null,
    title: String? = null,
    visible: Boolean = true,
    onClick: (Polygon, MapView, GeoPoint) -> Boolean = { _, _, _ -> false }
) {
    val mapApplier = currentComposer.applier as? MapApplier
    if (mapApplier != null) {
        ComposeNode<PolygonNode, MapApplier>(
            factory = {
                val polygon = Polygon(mapApplier.mapView)
                PolygonNode(
                    overlays = mapApplier.mapView.overlayManager,
                    polygon = polygon,
                    polygonState = state,
                    onClick = onClick
                )
            },
            update = {
                set(state.points) { this.polygon.points = it }
                set(state.holes) { this.polygon.holes = it }
                set(outlinePaint) { this.polygon.outlinePaint.set(it.asFrameworkPaint()) }
                set(fillPaint) { this.polygon.fillPaint.set(it.asFrameworkPaint()) }
                set(snippet) {
                    this.polygon.snippet = it
                    if (this.polygon.isInfoWindowOpen) {
                        this.polygon.showInfoWindow()
                    }
                }
                set(title) {
                    this.polygon.title = it
                    if (this.polygon.isInfoWindowOpen) {
                        this.polygon.showInfoWindow()
                    }
                }
                set(visible) { this.polygon.isVisible = it }
                set(onClick) { this.onClick = it }
            }
        )
    }
}