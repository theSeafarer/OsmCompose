/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.theseafarer.osmcompose

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayManager

internal class MarkerNode(
    val overlays: OverlayManager,
    val marker: Marker,
    val markerState: MarkerState,
    var onClick: (Marker, MapView) -> Boolean
): MapNode {
    override fun onCleared() {
        markerState.marker = null
        marker.setOnMarkerClickListener(null)
        overlays.remove(marker)
    }

    override fun onRemoved() {
        markerState.marker = null
        marker.setOnMarkerClickListener(null)
        overlays.remove(marker)
    }

    override fun onAttached() {
        markerState.marker = marker
        marker.setOnMarkerClickListener(onClick)
        overlays.add(marker)
    }
}


enum class AnchorU(val `value`: Float) {
    TOP(0f), CENTER(0.5f), BOTTOM(1f)
}

enum class AnchorV(val `value`: Float) {
    LEFT(0f), CENTER(0.5f), RIGHT(1f)
}


class MarkerState(
    position: GeoPoint
) {

    var position: GeoPoint by mutableStateOf(position)

    private val markerState: MutableState<Marker?> = mutableStateOf(null)
    internal var marker: Marker?
        get() = markerState.value
        set(value) {
            if (markerState.value == null && value == null) return
            if (markerState.value != null && value != null) {
                error("MarkerState may only be associated with one Marker at a time.")
            }
            markerState.value = value
        }

    companion object {
        val Saver: Saver<MarkerState, GeoPoint> = Saver(
            save = { it.position },
            restore = { MarkerState(it) }
        )
    }

}

@Composable
fun rememberMarkerState(
    key: String? = null,
    position: GeoPoint = GeoPoint(0.0, 0.0)
): MarkerState = rememberSaveable(key = key, saver = MarkerState.Saver) {
    MarkerState(position)
}

@OsmMapComposable
@Composable
fun Marker(
    state: MarkerState = rememberMarkerState(),
    alpha: Float = 1.0f,
    anchor: Offset = Offset(AnchorU.BOTTOM.value, AnchorV.CENTER.value),
    draggable: Boolean = false,
    flat: Boolean = false,
    icon: Drawable? = null,
    rotation: Float = 0.0f,
    snippet: String? = null,
    title: String? = null,
    visible: Boolean = true,
    onClick: (Marker, MapView) -> Boolean = { _, _ -> false },
) {
    MarkerImpl(
        state,
        alpha,
        anchor,
        draggable,
        flat,
        icon,
        rotation,
        snippet,
        title,
        visible,
        onClick
    )
}

@OsmMapComposable
@Composable
private fun MarkerImpl(
    state: MarkerState,
    alpha: Float = 1.0f,
    anchor: Offset = Offset(AnchorU.BOTTOM.value, AnchorV.CENTER.value),
    draggable: Boolean = false,
    flat: Boolean = false,
    icon: Drawable? = null,
    rotation: Float = 0.0f,
    snippet: String? = null,
    title: String? = null,
    visible: Boolean = true,
    onClick: (Marker, MapView) -> Boolean = { _, _ -> false },
    //InfoWindow stuff
) {
    val mapApplier = currentComposer.applier as? MapApplier
    if (mapApplier != null) {
        ComposeNode<MarkerNode, MapApplier>(
            factory = {
                val marker = Marker(mapApplier.mapView)
                MarkerNode(
                    overlays = mapApplier.mapView.overlayManager,
                    marker = marker,
                    markerState = state,
                    onClick = onClick
                )
            }, update = {
                set(alpha) { this.marker.alpha = it }
                set(anchor) { this.marker.setAnchor(it.x, it.y) }
                set(draggable) { this.marker.isDraggable = it }
                set(flat) { this.marker.isFlat = it }
                set(icon) { this.marker.icon = it }
                set(state.position) { this.marker.position = it }
                set(rotation) { this.marker.rotation = it }
                set(snippet) {
                    this.marker.snippet = it
                    if (this.marker.isInfoWindowShown) {
                        this.marker.showInfoWindow()
                    }
                }
                set(title) {
                    this.marker.title = it
                    if (this.marker.isInfoWindowShown) {
                        this.marker.showInfoWindow()
                    }
                }
                set(visible) { this.marker.setVisible(it) }
                set(onClick) { this.onClick = it }
            }
        )
    }
}