/*
 * Copyright 2023 Google LLC
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

package com.google.jetstream.presentation.screens.videoPlayer

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.google.jetstream.R
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.util.StringConstants
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.common.Loading
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerControlsIcon
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerMainFrame
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerMediaTitle
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerMediaTitleType
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerOverlay
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerPulse
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerSeeker
import com.google.jetstream.presentation.screens.videoPlayer.components.VideoPlayerState
import com.google.jetstream.presentation.screens.videoPlayer.components.rememberVideoPlayerPulseState
import com.google.jetstream.presentation.screens.videoPlayer.components.rememberVideoPlayerState
import com.google.jetstream.presentation.utils.handleDPadKeyEvents
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

object VideoPlayerScreen {
    const val MovieIdBundleKey = "movieId"
}

/**
 * [Work in progress] A composable screen for playing a video.
 *
 * @param onBackPressed The callback to invoke when the user presses the back button.
 * @param videoPlayerScreenViewModel The view model for the video player screen.
 */
@Composable
fun VideoPlayerScreen(
    onBackPressed: () -> Unit,
    videoPlayerScreenViewModel: VideoPlayerScreenViewModel = hiltViewModel()
) {
    val uiState by videoPlayerScreenViewModel.uiState.collectAsStateWithLifecycle()

    // TODO: Handle Loading & Error states
    when (val s = uiState) {
        is VideoPlayerScreenUiState.Loading -> {
            Loading(modifier = Modifier.fillMaxSize())
        }
        is VideoPlayerScreenUiState.Error -> {
            Error(modifier = Modifier.fillMaxSize())
        }
        is VideoPlayerScreenUiState.Done -> {
            VideoPlayerScreenContent(
                movieDetails = s.movieDetails,
                onBackPressed = onBackPressed
            )
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreenContent(movieDetails: MovieDetails, onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val videoPlayerState = rememberVideoPlayerState(hideSeconds = 4)

    // TODO: Move to ViewModel for better reuse
    val exoPlayer = rememberExoPlayer(context)
    LaunchedEffect(exoPlayer, movieDetails) {
        exoPlayer.setMediaItem(
            MediaItem.Builder()
                .setUri(movieDetails.videoUri)
                .setSubtitleConfigurations(
                    if (movieDetails.subtitleUri == null) {
                        emptyList()
                    } else {
                        listOf(
                            MediaItem.SubtitleConfiguration
                                .Builder(Uri.parse(movieDetails.subtitleUri))
                                .setMimeType("application/vtt")
                                .setLanguage("en")
                                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                                .build()
                        )
                    }
                ).build()
        )
        exoPlayer.prepare()
    }

    var contentCurrentPosition by remember { mutableLongStateOf(0L) }
    var isPlaying: Boolean by remember { mutableStateOf(exoPlayer.isPlaying) }
    // TODO: Update in a more thoughtful manner
    LaunchedEffect(Unit) {
        while (true) {
            delay(300)
            contentCurrentPosition = exoPlayer.currentPosition
            isPlaying = exoPlayer.isPlaying
        }
    }

    BackHandler(onBack = onBackPressed)

    var isCcOn by remember { mutableStateOf(false) }
    val pulseState = rememberVideoPlayerPulseState()
    val focusRequesterCcButton = remember { FocusRequester() }
    val focusRequesterSeeker = remember { FocusRequester() }
    val onPressLeftOrRight = focusButton(focusRequester = focusRequesterSeeker, videoPlayerState)
    val onPlayPauseToggle = { shouldPlay: Boolean ->
        if (shouldPlay) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }
    val onPressEnter = {
        if (videoPlayerState.controlsVisible) {
            onPlayPauseToggle.invoke(!isPlaying)
        }
        videoPlayerState.showControls()
    }

    Box(
        Modifier
            .handleDPadKeyEvents(
                onEnter = onPressEnter,
                onLeft = onPressLeftOrRight,
                onRight = onPressLeftOrRight,
                onDown = { videoPlayerState.showControls() },
                onUp = focusButton(focusRequester = focusRequesterCcButton, videoPlayerState)
            )
            .focusable()
    ) {
        AndroidView(
            factory = {
                PlayerView(context).apply { useController = false }
            },
            update = { it.player = exoPlayer },
            onRelease = { exoPlayer.release() }
        )
        VideoPlayerOverlay(
            modifier = Modifier.align(Alignment.BottomCenter),
            state = videoPlayerState,
            isPlaying = isPlaying,
            centerButton = { VideoPlayerPulse(pulseState) },
            subtitles = { /* TODO Implement subtitles */ },
            controls = {
                VideoPlayerControls(
                    movieDetails = movieDetails,
                    isPlaying = isPlaying,
                    isCcOn = isCcOn,
                    onToggleCcButton = { isCcOn = it },
                    contentCurrentPosition = contentCurrentPosition,
                    exoPlayer = exoPlayer,
                    state = videoPlayerState,
                    focusRequesterCcButton = focusRequesterCcButton,
                    focusRequesterSeeker = focusRequesterSeeker
                )
            }
        )
    }
}

@Composable
fun VideoPlayerControls(
    movieDetails: MovieDetails,
    isPlaying: Boolean,
    isCcOn: Boolean,
    onToggleCcButton: (Boolean) -> Unit,
    contentCurrentPosition: Long,
    exoPlayer: ExoPlayer,
    state: VideoPlayerState,
    focusRequesterCcButton: FocusRequester,
    focusRequesterSeeker: FocusRequester
) {
    VideoPlayerMainFrame(
        mediaTitle = {
            VideoPlayerMediaTitle(
                title = movieDetails.name,
                secondaryText = movieDetails.releaseDate,
                tertiaryText = movieDetails.director,
                classification = movieDetails.pgRating,
                type = VideoPlayerMediaTitleType.DEFAULT
            )
        },
        mediaActions = {
            Row(
                modifier = Modifier.padding(bottom = 0.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                VideoPlayerControlsIcon(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(width = 22.dp, height = 19.dp)
                        .focusRequester(focusRequesterCcButton)
                        .handleDPadKeyEvents(
                            onDown = { focusRequesterSeeker.requestFocus() }
                        ),
                    onClick = { onToggleCcButton(!isCcOn) },
                    icon = if (isCcOn) R.drawable.ic_cc_on else R.drawable.ic_cc_off,
                    state = state,
                    isPlaying = isPlaying,
                    contentDescription = StringConstants
                        .Composable
                        .VideoPlayerControlClosedCaptionsButton
                )
            }
        },
        seeker = {
            VideoPlayerSeeker(
                focusRequesterSeeker = focusRequesterSeeker,
                state = state,
                isPlaying = isPlaying,
                onSeek = { exoPlayer.seekTo(exoPlayer.duration.times(it).toLong()) },
                contentProgress = contentCurrentPosition.milliseconds,
                contentDuration = exoPlayer.duration.milliseconds
            )
        },
        more = null
    )
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun rememberExoPlayer(context: Context) = remember {
    ExoPlayer.Builder(context)
        .setSeekForwardIncrementMs(10)
        .setSeekBackIncrementMs(10)
        .setMediaSourceFactory(
            ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context))
        )
        .setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
        .build()
        .apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ONE
        }
}

private fun focusButton(
    focusRequester: FocusRequester,
    videoPlayerState: VideoPlayerState
): () -> Unit =
    {
        if (videoPlayerState.controlsVisible) {
            focusRequester.requestFocus()
        }
        videoPlayerState.showControls()
    }

