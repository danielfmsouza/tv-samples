/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.jetstream.presentation.screens.videoPlayer.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.jetstream.R
import com.google.jetstream.data.util.StringConstants
import kotlin.time.Duration

@Composable
fun VideoPlayerSeeker(
    focusRequesterSeeker: FocusRequester,
    state: VideoPlayerState,
    isPlaying: Boolean,
    onSeek: (Float) -> Unit,
    contentProgress: Duration,
    contentDuration: Duration
) {
    val contentProgressString =
        contentProgress.toComponents { h, m, s, _ ->
            if (h > 0) {
                "$h:${m.padStartWith0()}:${s.padStartWith0()}"
            } else {
                "${m.padStartWith0()}:${s.padStartWith0()}"
            }
        }
    val contentDurationString =
        contentDuration.toComponents { h, m, s, _ ->
            if (h > 0) {
                "$h:${m.padStartWith0()}:${s.padStartWith0()}"
            } else {
                "${m.padStartWith0()}:${s.padStartWith0()}"
            }
        }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        VideoPlayerControlsIcon(
            modifier = Modifier
                .focusable(enabled = false)
                .padding(end = 20.dp)
                .size(width = 13.dp, height = 20.dp),
            icon = if (isPlaying) R.drawable.ic_pause_button else R.drawable.ic_play_button,
            state = state,
            isPlaying = isPlaying,
            contentDescription = StringConstants
                .Composable
                .VideoPlayerControlPlayPauseButton
        )

        VideoPlayerControllerIndicator(
            modifier = Modifier.focusRequester(focusRequesterSeeker),
            progress = (contentProgress / contentDuration).toFloat(),
            onSeek = onSeek,
            state = state
        )
    }

    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.fillMaxWidth().padding(top = 18.dp)
    ) {
        VideoPlayerControllerText(text = contentProgressString, Color.White.copy(alpha = 0.9f))
        VideoPlayerControllerText(text = "/", color = Color.White.copy(alpha = 0.3f))
        VideoPlayerControllerText(text = contentDurationString, color = Color.White.copy(alpha = 0.7f))
    }
}

private fun Number.padStartWith0() = this.toString().padStart(2, '0')
