/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.theseafarer.osmcompose

import androidx.compose.runtime.AbstractApplier
import org.osmdroid.views.MapView


internal interface MapNode {
    fun onAttached()
    fun onRemoved()
    fun onCleared()
}

private object MapNodeRoot : MapNode {
    override fun onAttached() {}
    override fun onRemoved() {}
    override fun onCleared() {}
}

internal class MapApplier (
    val mapView: MapView
) : AbstractApplier<MapNode>(MapNodeRoot) {

    private val overlays = mutableListOf<MapNode>()

    override fun insertBottomUp(index: Int, instance: MapNode) {
        overlays.add(index, instance)
        instance.onAttached()
        mapView.invalidate()
    }

    override fun insertTopDown(index: Int, instance: MapNode) {

    }

    override fun move(from: Int, to: Int, count: Int) {
        overlays.move(from, to, count)
    }

    override fun onClear() {
        overlays.forEach { it.onCleared() }
        overlays.clear()
        mapView.invalidate()
    }

    override fun remove(index: Int, count: Int) {
        repeat(count) {
            overlays[index + it].onRemoved()
        }
        overlays.remove(index, count)
        mapView.invalidate()
    }

}