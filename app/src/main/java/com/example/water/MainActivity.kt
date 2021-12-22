package com.example.water

import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = GLSurfaceView(this)
        view.setRenderer(WaterRenderer())
        view.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        setContentView(view)
    }
}