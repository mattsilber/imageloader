package com.guardanis.imageloader.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatImageView
import android.view.View
import com.guardanis.imageloader.ImageRequest

class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)

        setContentView(R.layout.activity_main)
        standardClicked(null)
    }

    fun standardClicked(view: View?) {
        loadImages("https://d3819ii77zvwic.cloudfront.net/wp-content/uploads/2013/07/awkward_photos.jpg")
    }

    fun svgClicked(view: View?) {
        loadImages("https://upload.wikimedia.org/wikipedia/commons/thumb/b/bd/Test.svg/1024px-Test.svg.png")
    }

    fun gifClicked(view: View?) {
        loadImages("https://upload.wikimedia.org/wikipedia/commons/2/2c/Rotating_earth_%28large%29.gif")
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
                .execute()
    }
}
