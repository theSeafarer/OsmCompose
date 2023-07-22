# OsmCompose
### 🚧⛔ This is an exercise to familiarise myself with Compose and
### Compose-style APIs, it's incomplete (and will remain so) and untested
### Do NOT use this for anything  🚧⛔️

## Description
[osmdroid](https://github.com/osmdroid/osmdroid)'s MapView as Jetpack Compose composable with
Compose-style APIs for creating markers, polylines and such. 

here's what the API looks like:
```kotlin
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
```
And the resulting map on the screen:
![Screenshot for the example code provided][screenshots/s0.jpg]