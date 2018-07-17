package com.guardanis.imageloader.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatImageView
import android.util.Log
import android.view.View
import com.guardanis.imageloader.ImageRequest

class MainActivity: AppCompatActivity() {

    private val TAG = "imageloader"

    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)

        setContentView(R.layout.activity_main)
        standardClicked(null)
        vectorAssetClicked(null)
    }

    fun standardClicked(view: View?) {
        loadImages("https://upload.wikimedia.org/wikipedia/commons/d/d9/Test.png")
    }

    fun svgClicked(view: View?) {
        loadImages("https://upload.wikimedia.org/wikipedia/commons/thumb/b/bd/Test.svg/1024px-Test.svg.png")
    }

    fun gifClicked(view: View?) {
        loadImages("https://upload.wikimedia.org/wikipedia/commons/2/2c/Rotating_earth_%28large%29.gif")
    }

    fun vectorAssetClicked(view: View?) {
        ImageRequest.create(findViewById<AppCompatImageView>(R.id.main__test__vector_medium))
                .setTargetDrawable(R.drawable.ic_android_test)
                .setFadeTransition(150)
                .setTranslateTransition(-1f, -1f, 350)
                .execute()

        ImageRequest.create(findViewById<AppCompatImageView>(R.id.main__test__vector_large))
                .setTargetDrawable(R.drawable.ic_android_test)
                .setFadeTransition(150)
                .setScaleTransition(0f, 1f, 750)
                .execute()
    }

    private fun loadImages(url: String) {
        ImageRequest.create(findViewById<AppCompatImageView>(R.id.main__test_small))
                .setTargetUrl(url)
                .setFadeTransition(150)
                .setTranslateTransition(-1f, -1f, 350)
                .execute()

        ImageRequest.create(findViewById<AppCompatImageView>(R.id.main__test_medium))
                .setTargetUrl(url)
                .setFadeTransition(150)
                .setScaleTransition(0f, 1f, 750)
                .execute()

        ImageRequest.create(findViewById<AppCompatImageView>(R.id.main__test_large))
                .setTargetUrl(url)
                .setFadeTransition(150)
                .setTranslateTransition(1f, 1f, 350)
                .setMaxCacheDurationMs(1)
                .setRequestStartedCallback({
                    Log.d(TAG, "Main test started for $url")
                })
                .setSuccessCallback({ _, _ ->
                    Log.d(TAG, "Main test success for $url")
                })
                .setErrorCallback({ _, error->
                    Log.d(TAG, "Main test error for $url")

                    error?.printStackTrace()
                })
                .execute()
    }
}
