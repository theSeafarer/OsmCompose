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
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.OverlayManager
import org.osmdroid.views.overlay.Polyline

internal class PolylineNode(
    val overlays: OverlayManager,
    val polyline: Polyline,
    val polylineState: PolylineState,
    var onClick: (Polyline, MapView, GeoPoint) -> Boolean,
) : MapNode {
    override fun onCleared() {
        polylineState.polyline = null
        polyline.setOnClickListener(null)
        overlays.remove(polyline)
    }

    override fun onRemoved() {
        polylineState.polyline = null
        polyline.setOnClickListener(null)
        overlays.remove(polyline)
    }

    override fun onAttached() {
        polylineState.polyline = polyline
        polyline.setOnClickListener(onClick)
        overlays.add(polyline)
    }
}

class PolylineState(
    points: List<GeoPoint>
) {
    var points: List<GeoPoint> by mutableStateOf(points)

    private val polylineState: MutableState<Polyline?> = mutableStateOf(null)
    internal var polyline: Polyline?
        get() = polylineState.value
        set(value) {
            if (polylineState.value == null && value == null) return
            if (polylineState.value != null && value != null) {
                error("PolylineState may only be associated with one Polyline at a time.")
            }
            polylineState.value = value
        }

    companion object {
        val Saver: Saver<PolylineState, List<GeoPoint>> = Saver(
            save = { it.points },
            restore = { PolylineState(it) }
        )
    }
}

@Composable
fun rememberPolylineState(
    key: String? = null,
    points: List<GeoPoint> = emptyList()
): PolylineState = rememberSaveable(key = key, saver = PolylineState.Saver) {
    PolylineState(points)
}

@OsmMapComposable
@Composable
fun Polyline(
    state: PolylineState,
    outlinePaint: Paint = AndroidPaint().apply {
        color = Color.Black
        strokeWidth = 10.0f
        style = PaintingStyle.Stroke
        isAntiAlias = true
    },
    snippet: String? = null,
    title: String? = null,
    visible: Boolean = true,
    onClick: (Polyline, MapView, GeoPoint) -> Boolean = { _, _, _ -> false }
) {
    val mapApplier = currentComposer.applier as? MapApplier
    if (mapApplier != null) {
        ComposeNode<PolylineNode, MapApplier>(
            factory = {
                val polyline = Polyline(mapApplier.mapView)
                PolylineNode(
                    overlays = mapApplier.mapView.overlayManager,
                    polyline = polyline,
                    polylineState = state,
                    onClick = onClick
                )
            },
            update = {
                set(state.points) { this.polyline.setPoints(it) }
                set(outlinePaint) { this.polyline.outlinePaint.set(it.asFrameworkPaint()) }
                set(snippet) {
                    this.polyline.snippet = it
                    if (this.polyline.isInfoWindowOpen) {
                        this.polyline.showInfoWindow()
                    }
                }
                set(title) {
                    this.polyline.title = it
                    if (this.polyline.isInfoWindowOpen) {
                        this.polyline.showInfoWindow()
                    }
                }
                set(visible) { this.polyline.isVisible = it }
                set(onClick) { this.onClick = it }
            }
        )
    }
}