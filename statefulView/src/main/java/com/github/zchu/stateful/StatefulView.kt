package com.github.zchu.stateful

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.*

/**
 * <pre>
 *     author: z-chu
 *     blog  : https://github.com/z-chu
 *     time  : 18/01/30
 * </pre>
 */
class StatefulView : FrameLayout {

    private lateinit var mLayoutInflater: LayoutInflater
    private var mLoadingView: View? = null
        set(value) {
            if (mLoadingTextViewId != View.NO_ID) {
                tvLoadingMessage = value?.findViewById(mLoadingTextViewId)
            }
            field = value
        }

    private var mErrorView: View? = null
        set(value) {
            if (mRetryViewId != View.NO_ID) {
                val retryView = value?.findViewById<View>(mRetryViewId)
                retryView?.setOnClickListener {
                    onRetryListener?.invoke(it)
                }
            }
            if (mErrorTextViewId != View.NO_ID) {
                tvErrorMessage = value?.findViewById(mErrorTextViewId)
            }
            field = value
        }
    private var mContentView: View? = null

    private var mContentLayoutId: Int = View.NO_ID
        set(value) {
            if (lazyLoading && value != View.NO_ID) {
                val contentView = mLayoutInflater.inflate(mContentLayoutId, this, false)
                mContentView = contentView
                addView(contentView, 0)
                onContentViewCreatedListener?.invoke(contentView)
            }
            field = value
        }

    private var mLoadingLayoutId: Int = View.NO_ID
        set(value) {
            if (lazyLoading && value != View.NO_ID) {
                val loadingView = mLayoutInflater.inflate(mLoadingLayoutId, this, false)
                mLoadingView = loadingView
                addView(loadingView)
                onLoadingViewCreatedListener?.invoke(loadingView)
            }
            field = value
        }

    private var mErrorLayoutId: Int = View.NO_ID
        set(value) {
            if (lazyLoading && value != View.NO_ID) {
                val errorView = mLayoutInflater.inflate(mErrorLayoutId, this, false)
                mErrorView = errorView
                addView(errorView)
                onErrorViewCreatedListener?.invoke(errorView)
            }
            field = value
        }
    var state = STATE_NONE
        private set

    private var tvLoadingMessage: TextView? = null
    private var tvErrorMessage: TextView? = null
    private var mLoadingTextViewId: Int = View.NO_ID
    private var mErrorTextViewId: Int = View.NO_ID
    private var mRetryViewId: Int = View.NO_ID
    private var lazyLoading: Boolean = false


    var onRetryListener: ((View) -> Unit)? = null
    var onContentViewCreatedListener: ((View) -> Unit)? = null
        set(value) {
            mContentView?.let {
                value?.invoke(it)
            }
            field = value
        }
    var onLoadingViewCreatedListener: ((View) -> Unit)? = null
        set(value) {
            mLoadingView?.let {
                value?.invoke(it)
            }
            field = value
        }
    var onErrorViewCreatedListener: ((View) -> Unit)? = null
        set(value) {
            mErrorView?.let {
                value?.invoke(it)
            }
            field = value
        }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        inflateView(context, context.obtainStyledAttributes(attrs, R.styleable.StatefulView, defStyleAttr, 0))
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        inflateView(context, context.obtainStyledAttributes(attrs, R.styleable.StatefulView, defStyleAttr, defStyleRes))

    }

    private fun inflateView(context: Context, typedArray: TypedArray) {
        mLayoutInflater = LayoutInflater.from(context)
        lazyLoading = typedArray.getBoolean(R.styleable.StatefulView_LazyLoading, false)
        mContentLayoutId = typedArray.getResourceId(R.styleable.StatefulView_contentLayout, mContentLayoutId)
        mLoadingLayoutId = typedArray.getResourceId(R.styleable.StatefulView_loadingLayout, mLoadingLayoutId)
        mErrorLayoutId = typedArray.getResourceId(R.styleable.StatefulView_errorLayout, mErrorLayoutId)
        mErrorTextViewId = typedArray.getResourceId(R.styleable.StatefulView_errorTextViewId, mErrorTextViewId)
        mLoadingTextViewId = typedArray.getResourceId(R.styleable.StatefulView_loadingTextViewId, mLoadingTextViewId)
        mRetryViewId = typedArray.getResourceId(R.styleable.StatefulView_retryViewId, mRetryViewId)
        typedArray.recycle()

    }

    @JvmOverloads
    fun showLoading(message: String? = null) {
        if (state == STATE_LOADING) {
            return
        }
        this.state = STATE_LOADING
        var loadingView = mLoadingView
        if (loadingView != null) {
            loadingView.visibility = View.VISIBLE
        } else {
            if (mLoadingLayoutId != View.NO_ID) {
                loadingView = mLayoutInflater.inflate(mLoadingLayoutId, this, false)
                mLoadingView = loadingView
                addView(loadingView)
                onLoadingViewCreatedListener?.invoke(loadingView)
            }
        }
        if (message != null) {
            tvLoadingMessage?.let {
                it.visibility = View.VISIBLE
                it.text = message
            }
        }
        mErrorView?.visibility = View.GONE

    }

    fun showLoading(@StringRes resId: Int) {
        showLoading(context.getString(resId))
    }

    fun showError(@StringRes resId: Int) {
        showError(context.getString(resId))
    }


    @JvmOverloads
    fun showError(message: String? = null) {
        if (state == STATE_ERROR) {
            return
        }
        this.state = STATE_ERROR
        mLoadingView?.visibility = View.GONE
        var errorView = mErrorView
        if (errorView != null) {
            errorView.visibility = View.VISIBLE

        } else {
            if (mErrorLayoutId != View.NO_ID) {
                errorView = mLayoutInflater.inflate(mErrorLayoutId, this, false)
                mErrorView = errorView
                addView(errorView)
                onErrorViewCreatedListener?.invoke(errorView)
            }
        }
        if (message != null) {
            tvErrorMessage?.let {
                it.visibility = View.VISIBLE
                it.text = message
            }
        }
    }

    @JvmOverloads
    fun showContent(anim: Boolean = true) {
        if (state == STATE_FINISH) {
            return
        }
        this.state = STATE_FINISH
        mErrorView?.visibility = View.GONE
        var contentView = mContentView
        if (contentView != null) {
            contentView.visibility = View.VISIBLE
        } else {
            if (mContentLayoutId != View.NO_ID) {
                contentView = mLayoutInflater.inflate(mContentLayoutId, this, false)
                mContentView = contentView
                addView(contentView, 0)
                onContentViewCreatedListener?.invoke(contentView)
            }
        }
        contentView?.visibility = View.VISIBLE
        if (anim) {
            startShowContentAnim(contentView, mLoadingView)
        } else {
            mLoadingView?.visibility = View.GONE
        }
    }

    private fun startShowContentAnim(contentView: View?, loadingView: View?) {
        val animatorSet = AnimatorSet()
        if (contentView != null && contentView.visibility == View.VISIBLE) {
            val contentFadeIn = ObjectAnimator.ofFloat(contentView, View.ALPHA, 0f, 1f)
            val contentTranslateIn = ObjectAnimator.ofFloat<View>(
                contentView, View.TRANSLATION_Y,
                ANIM_TRANSLATE_Y, 0F
            )
            animatorSet.playTogether(contentFadeIn, contentTranslateIn)
        }

        if (loadingView != null && loadingView.visibility == View.VISIBLE) {
            val loadingFadeOut = ObjectAnimator.ofFloat(loadingView, View.ALPHA, 1f, 0f)
            val loadingTranslateOut = ObjectAnimator.ofFloat<View>(
                loadingView, View.TRANSLATION_Y, 0F,
                -ANIM_TRANSLATE_Y * 2
            )
            animatorSet.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    loadingView.visibility = View.GONE
                    loadingView.alpha = 1f // For future showLoading calls
                    loadingView.translationY = 0f
                }

                override fun onAnimationStart(animation: Animator) {}
            })
            animatorSet.playTogether(loadingFadeOut, loadingTranslateOut)

        }
        animatorSet.duration = ANIM_TIME_LONG.toLong()
        animatorSet.start()
    }

    fun setLoadingLayoutId(@LayoutRes layoutId: Int) {
        checkIsLegalStatus()
        this.mLoadingLayoutId = layoutId
        mLoadingView = null
    }

    fun setLoadingView(view: View) {
        checkIsLegalStatus()
        view.visibility = View.GONE
        this.mLoadingView = view
        addView(view)
        onLoadingViewCreatedListener?.invoke(view)
    }

    fun setErrorLayoutId(layoutId: Int) {
        checkIsLegalStatus()
        this.mErrorLayoutId = layoutId
        mErrorView = null
    }

    fun setErrorView(view: View) {
        checkIsLegalStatus()
        view.visibility = View.GONE
        this.mErrorView = view
        addView(view)
        onErrorViewCreatedListener?.invoke(view)
    }

    fun setContentLayoutId(layoutId: Int) {
        checkIsLegalStatus()
        this.mContentLayoutId = layoutId
        mContentView = null
    }

    fun setContentView(view: View) {
        checkIsLegalStatus()
        view.visibility = View.GONE
        this.mContentView = view
        addView(view, 0)
        onContentViewCreatedListener?.invoke(view)
    }

    private fun checkIsLegalStatus() {
        if (state != STATE_NONE) {
            throw IllegalStateException("Can not change view , because" + javaClass.simpleName + "\'s state is not STATE_NONE")
        }
    }


    companion object {

        val STATE_NONE = 0
        val STATE_LOADING = 1
        val STATE_ERROR = 2
        val STATE_FINISH = 3
        //单位dp
        private val ANIM_TRANSLATE_Y = 40F
        //动画持续时间
        private val ANIM_TIME_LONG = 500
    }

}
