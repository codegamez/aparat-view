package com.codegames.aparatview

import android.content.DialogInterface
import android.media.MediaPlayer
import android.view.KeyEvent
import android.view.SurfaceView
import android.view.View
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import android.widget.VideoView


/**
 * This class serves as a WebChromeClient to be set to a WebView, allowing it to play video.
 * Video will play differently depending on target API level (in-line, fullscreen, or both).
 *
 *
 * It has been tested with the following video classes:
 * - android.widget.VideoView (typically API level <11)
 * - android.webkit.HTML5VideoFullScreen$VideoSurfaceView/VideoTextureView (typically API level 11-18)
 * - com.android.org.chromium.content.browser.ContentVideoView$VideoSurfaceView (typically API level 19+)
 *
 *
 * Important notes:
 * - For API level 11+, android:hardwareAccelerated="true" must be set in the application manifest.
 * - The invoking activity must call VideoEnabledWebChromeClient's onBackPressed() inside of its own onBackPressed().
 * - Tested in Android API levels 8-19. Only tested on http://m.youtube.com.
 *
 * @author Cristian Perez (http://cpr.name)
 * @modified_by Shahab Yousefi (https://codegames.ir)
 */
internal open class VideoEnabledWebChromeClient
    : WebChromeClient, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
    MediaPlayer.OnErrorListener {

    private var activityNonVideoView: View? = null
    private var loadingView: View? = null
    private var webView: VideoEnabledWebView? = null

    /**
     * Indicates if the video is being displayed using a custom view (typically full-screen)
     *
     * @return true it the video is being displayed using a custom view (typically full-screen)
     */
    var isVideoFullscreen =
        false // Indicates if the video is being displayed using a custom view (typically full-screen)
        private set

    private var videoViewContainer: FrameLayout? = null
    private var videoViewDialog: VideoEnabledFullScreenDialog? = null
    private var videoViewCallback: CustomViewCallback? = null
    var toggleFullscreen: ((fullscreen: Boolean) -> Unit)? = null

    /**
     * Never use this constructor alone.
     * This constructor allows this class to be defined as an inline inner class in which the user can override methods
     */
    constructor() {}

    /**
     * Builds a video enabled WebChromeClient.
     *
     * @param activityNonVideoView A View in the activity's layout that contains every other view that should be hidden when the video goes full-screen.
     */
    constructor(activityNonVideoView: View?) {
        this.activityNonVideoView = activityNonVideoView
        loadingView = null
        webView = null
        isVideoFullscreen = false
    }

    /**
     * Builds a video enabled WebChromeClient.
     *
     * @param activityNonVideoView A View in the activity's layout that contains every other view that should be hidden when the video goes full-screen.
     * @param loadingView          A View to be shown while the video is loading (typically only used in API level <11). Must be already inflated and without a parent view.
     */
    constructor(activityNonVideoView: View?, loadingView: View?) {
        this.activityNonVideoView = activityNonVideoView
        this.loadingView = loadingView
        webView = null
        isVideoFullscreen = false
    }

    /**
     * Builds a video enabled WebChromeClient.
     *
     * @param activityNonVideoView A View in the activity's layout that contains every other view that should be hidden when the video goes full-screen.
     * @param loadingView          A View to be shown while the video is loading (typically only used in API level <11). Must be already inflated and without a parent view.
     * @param webView              The owner VideoEnabledWebView. Passing it will enable the VideoEnabledWebChromeClient to detect the HTML5 video ended event and exit full-screen.
     * Note: The web page must only contain one video tag in order for the HTML5 video ended event to work. This could be improved if needed (see Javascript code).
     */
    constructor(activityNonVideoView: View?, loadingView: View?, webView: VideoEnabledWebView?) {
        this.activityNonVideoView = activityNonVideoView
        this.loadingView = loadingView
        this.webView = webView
        isVideoFullscreen = false
    }

    override fun onShowCustomView(view: View, callback: CustomViewCallback) {
        if (view is FrameLayout) {
            // A video wants to be shown
            val focusedChild = view.focusedChild

            // Save video related variables
            isVideoFullscreen = true
            videoViewContainer = view
            videoViewCallback = callback

            // Hide the non-video view, add the video view, and show it
            activityNonVideoView?.visibility = View.INVISIBLE
            videoViewDialog =
                VideoEnabledFullScreenDialog(view.getContext(), videoViewContainer!!) {
                    onHideCustomView()
                }
            videoViewDialog?.show()

            videoViewDialog?.setOnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    onHideCustomView()
                }
                true
            }

            if (focusedChild is VideoView) {
                // Handle all the required events
                focusedChild.setOnPreparedListener(this)
                focusedChild.setOnCompletionListener(this)
                focusedChild.setOnErrorListener(this)
            } else {
                // Other classes, including:
                // - android.webkit.HTML5VideoFullScreen$VideoSurfaceView, which inherits from android.view.SurfaceView (typically API level 11-18)
                // - android.webkit.HTML5VideoFullScreen$VideoTextureView, which inherits from android.view.TextureView (typically API level 11-18)
                // - com.android.org.chromium.content.browser.ContentVideoView$VideoSurfaceView, which inherits from android.view.SurfaceView (typically API level 19+)

                // Handle HTML5 video ended event only if the class is a SurfaceView
                // Test case: TextureView of Sony Xperia T API level 16 doesn't work fullscreen when loading the javascript below
                if (webView != null && webView!!.settings.javaScriptEnabled && focusedChild is SurfaceView) {
                    // Run javascript code that detects the video end and notifies the Javascript interface
                    val js = """
                        javascript:
                            var _ytrp_html5_video_last;
                            var _ytrp_html5_video = document.getElementsByTagName('video')[0];
                            if (_ytrp_html5_video != undefined && _ytrp_html5_video != _ytrp_html5_video_last) {
                                _ytrp_html5_video_last = _ytrp_html5_video;
                                function _ytrp_html5_video_ended() {
                                    _VideoEnabledWebView.notifyVideoEnd();
                                }
                                _ytrp_html5_video.addEventListener('ended', _ytrp_html5_video_ended);
                            }
                    """.trimIndent()
                    webView!!.loadUrl(js)
                }
            }

            // Notify full-screen change
            toggleFullscreen?.invoke(true)
        }
    }

    override fun onShowCustomView(
        view: View,
        requestedOrientation: Int,
        callback: CustomViewCallback
    ) { // Available in API level 14+, deprecated in API level 18+
        onShowCustomView(view, callback)
    }

    override fun onHideCustomView() {
        // This method should be manually called on video end in all cases because it's not always called automatically.
        // This method must be manually called on back key press (from this class' onBackPressed() method).
        if (isVideoFullscreen) {
            // Hide the video view, remove it, and show the non-video view
            videoViewDialog?.dismiss()
            videoViewDialog = null
            activityNonVideoView!!.visibility = View.VISIBLE

            // Call back (only in API level <19, because in API level 19+ with chromium webview it crashes)
            if (videoViewCallback != null && !videoViewCallback!!.javaClass.name.contains(".chromium.")) {
                videoViewCallback?.onCustomViewHidden()
            }

            // Reset video related variables
            isVideoFullscreen = false
            videoViewContainer = null
            videoViewCallback = null

            // Notify full-screen change
            toggleFullscreen?.invoke(false)
        }
    }

    override fun getVideoLoadingProgressView(): View? { // Video will start loading
        return if (loadingView != null) {
            loadingView!!.visibility = View.VISIBLE
            loadingView
        } else {
            super.getVideoLoadingProgressView()
        }
    }

    override fun onPrepared(mp: MediaPlayer) { // Video will start playing, only called in the case of android.widget.VideoView (typically API level <11)
        if (loadingView != null) {
            loadingView!!.visibility = View.GONE
        }
    }

    override fun onCompletion(mp: MediaPlayer) { // Video finished playing, only called in the case of android.widget.VideoView (typically API level <11)
        onHideCustomView()
    }

    override fun onError(
        mp: MediaPlayer,
        what: Int,
        extra: Int
    ): Boolean { // Error while playing video, only called in the case of android.widget.VideoView (typically API level <11)
        return false // By returning false, onCompletion() will be called
    }

    /**
     * Notifies the class that the back key has been pressed by the user.
     * This must be called from the Activity's onBackPressed(), and if it returns false, the activity itself should handle it. Otherwise don't do anything.
     *
     * @return Returns true if the event was handled, and false if was not (video view is not visible)
     */
    fun onBackPressed(): Boolean {
        return if (isVideoFullscreen) {
            onHideCustomView()
            true
        } else {
            false
        }
    }
}