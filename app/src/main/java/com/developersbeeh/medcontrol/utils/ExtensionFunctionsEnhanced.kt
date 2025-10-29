// ExtensionFunctionsEnhanced.kt
package com.developersbeeh.medcontrol.utils

import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.animation.Animator
import android.animation.AnimatorListenerAdapter

/**
 * Extension Functions Aprimoradas para o NidusCare
 */

// Fade In Animation
fun View.fadeIn(duration: Long = 300, onEnd: (() -> Unit)? = null) {
    if (visibility == View.VISIBLE) {
        onEnd?.invoke()
        return
    }
    
    alpha = 0f
    visibility = View.VISIBLE
    
    animate()
        .alpha(1f)
        .setDuration(duration)
        .setInterpolator(DecelerateInterpolator())
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onEnd?.invoke()
            }
        })
        .start()
}

// Fade Out Animation
fun View.fadeOut(duration: Long = 300, gone: Boolean = true, onEnd: (() -> Unit)? = null) {
    if (visibility != View.VISIBLE) {
        onEnd?.invoke()
        return
    }
    
    animate()
        .alpha(0f)
        .setDuration(duration)
        .setInterpolator(AccelerateDecelerateInterpolator())
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                visibility = if (gone) View.GONE else View.INVISIBLE
                alpha = 1f
                onEnd?.invoke()
            }
        })
        .start()
}

// Shake Animation
fun View.shake(duration: Long = 500, amplitude: Float = 10f) {
    val animator = android.animation.ObjectAnimator.ofFloat(
        this, "translationX", 
        0f, amplitude, -amplitude, amplitude, -amplitude, 0f
    )
    animator.duration = duration
    animator.interpolator = DecelerateInterpolator()
    animator.start()
}
