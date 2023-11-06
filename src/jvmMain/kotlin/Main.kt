import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.panpf.zoomimage.compose.rememberZoomState
import com.github.panpf.zoomimage.compose.subsampling.fromResource
import com.github.panpf.zoomimage.compose.subsampling.subsampling
import com.github.panpf.zoomimage.compose.zoom.zoomable
import com.github.panpf.zoomimage.compose.zoom.zooming
import com.github.panpf.zoomimage.subsampling.ImageSource
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        val zoomState = rememberZoomState()
        val thumbnailPainter = painterResource("sample_exif_girl_normal_thumbnail.jpg")
        LaunchedEffect(thumbnailPainter) {
            zoomState.zoomable.contentSize =
                IntSize(
                    thumbnailPainter.intrinsicSize.width.roundToInt(),
                    thumbnailPainter.intrinsicSize.height.roundToInt()
                )
        }
        LaunchedEffect(Unit) {
            zoomState.subsampling.showTileBounds = true
            zoomState.subsampling.setImageSource(
                ImageSource.fromResource("sample_exif_girl_normal.jpg")
            )
        }

        NoClipContentImage(
            painter = thumbnailPainter,
            contentDescription = "Hello, World!",
            contentScale = ContentScale.None,
            alignment = Alignment.TopStart,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .zoomable(zoomState.logger, zoomState.zoomable) // Monitor touch events and calculate scale, pan
                .zooming(zoomState.logger, zoomState.zoomable)  // Apply scale, pan, rotate
                .subsampling(zoomState.logger, zoomState.zoomable, zoomState.subsampling)   // Draw tiles
        )

        val info by remember {
            derivedStateOf {
                val zoomable = zoomState.zoomable
                """
                |scale: ${zoomable.transform.scale}
                |offset: ${zoomable.transform.offset}
                |contentSize: ${zoomable.contentSize},
                |contentOriginSize: ${zoomState.subsampling.imageInfo?.size},
                |containerSize: ${zoomable.containerSize},
                """.trimMargin()
            }
        }
        Text(
            text = info,
            modifier = Modifier.padding(20.dp),
            color = Color.White,
            style = TextStyle(
                shadow = Shadow(Color.Black, offset = Offset(0f, 0f), blurRadius = 3f)
            )
        )
    }
}

@Composable
fun NoClipContentImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) {
    val semantics = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else {
        Modifier
    }

    // Explicitly use a simple Layout implementation here as Spacer squashes any non fixed
    // constraint with zero
    Layout(
        {},
//        modifier.then(semantics).clipToBounds().paint(
        modifier
            .then(semantics)
            .paint(
                painter,
                alignment = alignment,
                contentScale = contentScale,
                alpha = alpha,
                colorFilter = colorFilter
            )
    ) { _, constraints ->
        layout(constraints.minWidth, constraints.minHeight) {}
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
