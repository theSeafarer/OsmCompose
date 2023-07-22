/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */


package io.github.theseafarer.osmcompose

import android.content.res.Resources
import android.util.Log
import org.osmdroid.util.GeoPoint

class TehranPoints(res: Resources) {
    private val _points: MutableList<GeoPoint> = mutableListOf()
    val points: List<GeoPoint> = _points

    init {
        try {
            res.openRawResource(R.raw.tehran_points).use {
                it.bufferedReader().forEachLine { line ->
                    val sp = line.split(Regex("\\s"))
                    _points.add(GeoPoint(sp[1].toDouble(), sp[0].toDouble()))
                }
            }
        } catch (e: Exception) {
            Log.e("Points", "Loading polygon points failed!")
        }
    }
}