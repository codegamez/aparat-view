package com.codegames.aparatview

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.updateLayoutParams
import kotlinx.android.synthetic.main.widget_aparat_view.view.*

class AparatView : FrameLayout {

    private var webChromeClient: VideoEnabledWebChromeClient? = null

    private var mVideoId: String = ""
    var videoId: String
        get() = mVideoId
        set(value) {
            mVideoId = value
            webChromeClient?.onHideCustomView()
            aparatview_webView?.loadUrl(link())
        }

    private var mVideoRatio: String = "16:9"
    var videoRatio: String
        get() = mVideoRatio
        set(value) {
            mVideoRatio = value
            aparatview_webView?.updateLayoutParams<ConstraintLayout.LayoutParams> {
                dimensionRatio = value
            }
        }

    var isFullScreen
        get() = aparatview_webView?.isVideoFullscreen ?: false
        set(value) {
            if(!value) {
                webChromeClient?.onHideCustomView()
            }else {
                // Todo: not implemented yet
            }
        }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {

        inflate(context, R.layout.widget_aparat_view, this)

        val attributeArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.AparatView
        )

        this.mVideoId = attributeArray.getString(R.styleable.AparatView_aparatview_video_id) ?: ""
        this.videoRatio =
            attributeArray.getString(R.styleable.AparatView_aparatview_video_ratio) ?: "16:9"

        attributeArray.recycle()

        DrawableCompat.setTint(
            aparatview_progressBar.indeterminateDrawable,
            ContextCompat.getColor(context, R.color.aparat_color)
        )

        if (isInEditMode) {
            aparatview_webView.visibility = View.INVISIBLE
            return
        }

        val newUA = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0"
        aparatview_webView.settings.userAgentString = newUA

        aparatview_webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                aparatview_progressBar.visibility = View.VISIBLE
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                aparatview_progressBar.visibility = View.GONE
            }
        }

        webChromeClient = object : VideoEnabledWebChromeClient(
            aparatview_root, null, aparatview_webView // See all available constructors...
        ) {
            // Subscribe to standard events, such as onProgressChanged()...
            override fun onProgressChanged(view: WebView, progress: Int) {
                // Your code...
            }

        }

        webChromeClient?.toggleFullscreen =
            toggle@{ fullscreen -> // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
                if (context !is Activity) return@toggle
                if (fullscreen) {
                    val attr = context.window.attributes
                    attr.flags = attr.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
                    attr.flags = attr.flags or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    context.window.attributes = attr
                    context.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE
                } else {
                    val attr = context.window.attributes
                    attr.flags = attr.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
                    attr.flags =
                        attr.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON.inv()
                    context.window.attributes = attr
                    context.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                }
            }

        webChromeClient?.let { aparatview_webView.setWebChromeClient(it) }

        aparatview_webView.loadUrl(link())

    }

    fun play() {
        webChromeClient?.onHideCustomView()
        aparatview_webView.loadUrl(link())
    }

    private fun link() =
        "https://www.aparat.com/video/video/embed/videohash/$videoId/vt/frame?recom=none&allowFullScreen=true&webkitallowfullscreen=true"

}