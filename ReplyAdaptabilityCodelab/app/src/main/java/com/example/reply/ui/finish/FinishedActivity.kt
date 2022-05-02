package com.example.reply.ui.finish

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.example.reply.ui.DevicePosture
import com.example.reply.ui.ReplyHomeViewModel
import com.example.reply.ui.isBookPosture
import com.example.reply.ui.isTableTopPosture
import com.example.reply.ui.isSeparating
import com.example.reply.ui.theme.ReplyTheme
import com.example.reply.ui.rememberWindowSizeClass
import com.example.reply.ui.start.ReplyApp
import com.example.reply.ui.WindowSize
import com.example.reply.ui.ReplyHomeUIState
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

class FinishedActivity : ComponentActivity() {

    private val viewModel: ReplyHomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * Flow of [DevicePosture] that emits every time there's a change in the windowLayoutInfo
         */
        val devicePostureFlow =  WindowInfoTracker.getOrCreate(this).windowLayoutInfo(this)
            .flowWithLifecycle(this.lifecycle)
            .map { layoutInfo ->
                val foldingFeature =
                    layoutInfo.displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()
                when {
                    isTableTopPosture(foldingFeature) ->
                        DevicePosture.TableTopPosture(foldingFeature.bounds)
                    isBookPosture(foldingFeature) ->
                        DevicePosture.BookPosture(foldingFeature.bounds)
                    isSeparating(foldingFeature) ->
                        DevicePosture.Separating(foldingFeature.bounds, foldingFeature.orientation)
                    else -> DevicePosture.NormalPosture
                }
            }
            .stateIn(
                scope = lifecycleScope,
                started = SharingStarted.Eagerly,
                initialValue = DevicePosture.NormalPosture
            )

        setContent {
            ReplyTheme {
                val windowSize = rememberWindowSizeClass()
                val devicePosture = devicePostureFlow.collectAsState().value
                val uiState = viewModel.uiState.collectAsState().value
                ReplyApp(windowSize, devicePosture, uiState)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ReplyTheme {
        ReplyApp(
            windowSize = WindowSize.COMPACT,
            foldingDevicePosture = DevicePosture.NormalPosture,
            replyHomeUIState = ReplyHomeUIState()
        )
    }
}