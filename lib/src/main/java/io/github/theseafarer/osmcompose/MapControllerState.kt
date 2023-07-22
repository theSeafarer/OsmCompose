/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.theseafarer.osmcompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.osmdroid.api.IGeoPoint
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView


data class Position(
    val center: IGeoPoint = GeoPoint(0.0, 0.0),
    val zoom: Double = 0.0,
    val rotation: Float = 0f,
)

class MapControllerState(
    position: Position = Position(),
    onScroll: (ScrollEvent) -> Boolean = { false },
    onZoom: (ZoomEvent) -> Boolean = { false }
) {

    internal var _position: Position by mutableStateOf(position)

    var position: Position
        get() = _position
        set(value) {
            if (mapView != null) {
                mapView!!.apply {
                    rotation = position.rotation
                    controller.apply {
                        setCenter(position.center)
                        setZoom(position.zoom)
                    }
                }
            }
            _position = value
        }

    var onScroll: (ScrollEvent) -> Boolean by mutableStateOf(onScroll)
    var onZoom: (ZoomEvent) -> Boolean by mutableStateOf(onZoom)


    private var mapView: MapView? by mutableStateOf(null)


    private val listener = object : MapListener {
        override fun onScroll(ev: ScrollEvent?): Boolean {
            mapView?.mapCenter?.let { c -> this@MapControllerState._position = this@MapControllerState._position.copy(center = c) }
            mapView?.rotation?.let { r -> this@MapControllerState._position = this@MapControllerState._position.copy(rotation = r) }
            return ev?.let { this@MapControllerState.onScroll(it) } ?: false
        }

        override fun onZoom(ev: ZoomEvent?): Boolean {
            ev?.zoomLevel?.let { z -> this@MapControllerState._position = this@MapControllerState._position.copy(zoom = z) }
            return ev?.let { this@MapControllerState.onZoom(it) } ?: false
        }
    }

    internal fun setMapView(mapView: MapView?) {
        if (this.mapView == null && mapView == null) return
        if (this.mapView != null && mapView != null) {
            error("MapControllerState may only be associated with one MapView at a time")
        }
        this.mapView = mapView
        if (mapView != null) {
            mapView.apply {
                rotation = position.rotation
                controller.apply {
                    setCenter(position.center)
                    setZoom(position.zoom)
                }
            }
            mapView.addMapListener(listener)
        } else {
            this.mapView?.removeMapListener(listener)
        }
    }

    fun animateTo(
        point: IGeoPoint,
        pZoom: Double? = null,
        pSpeed: Long? = null,
        pOrientation: Float? = null,
        pClockwise: Boolean? = null
    ) {
        mapView?.controller?.animateTo(point, pZoom, pSpeed, pOrientation, pClockwise)
    }

    //FIXME
//    companion object {
//        val Saver: Saver<MapControllerState, Triple<IGeoPoint, Double, Float>> = Saver(
//            save = { Triple(it._position.center, it._position.zoom, it._position.rotation) },
//            restore = { MapControllerState(Position(it.first, it.second, it.third)) }
//        )
//    }
}


@Composable
fun rememberMapControllerState(
    key: String? = null, init: MapControllerState.() -> Unit = {}
): MapControllerState = remember(key) {
    MapControllerState().apply(init)
}