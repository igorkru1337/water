package com.example.water

import android.opengl.GLSurfaceView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.sqrt

class P internal constructor() {
    var x = 0f
    var y = 0f
    var z = 0f
    var vz = 0f
}

class WaterRenderer : GLSurfaceView.Renderer {

    private val waveLength = 75
    private var kDeformation = 0.1f
    private var kSpread = 0.09f
    private var offs = 0
    private lateinit var p: Array<Array<P>>
    private lateinit var vertices: FloatArray
    private lateinit var floatBuffer: FloatBuffer
    private lateinit var byteBuffer: ByteBuffer

    private fun nioBuff() {
        byteBuffer = ByteBuffer.allocateDirect(2 * 2 * 3 * waveLength * waveLength * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        floatBuffer = byteBuffer.asFloatBuffer()
        floatBuffer.put(vertices)
        floatBuffer.position(0)
    }

    private fun moveWave() {
        for (i in 0 until waveLength) {
            for (j in 0 until waveLength) {
                p[i][j] = P()
                p[i][j].x = 3f * j / waveLength
                p[i][j].y = 3f * i / waveLength
                p[i][j].z = 0f
                p[i][j].vz = 0f
            }
        }
    }

    private fun displayNet() {
        offs = 0
        for (i in 0 until waveLength) {
            for (j in 0 until waveLength - 1) {
                vertices[waveLength * i * 3 * 2 + j * 3 * 2] = 1.0f * j / waveLength
                vertices[waveLength * i * 3 * 2 + j * 3 * 2 + 1] = 1.0f * i / waveLength
                vertices[waveLength * i * 3 * 2 + j * 3 * 2 + 2] = 1.0f * p[i][j].z
                vertices[waveLength * i * 3 * 2 + j * 3 * 2 + 3] = 1.0f * (j + 1) / waveLength
                vertices[waveLength * i * 3 * 2 + j * 3 * 2 + 4] = 1.0f * i / waveLength
                vertices[waveLength * i * 3 * 2 + j * 3 * 2 + 5] = 1.0f * p[i][j + 1].z
                offs += 6
            }
        }

        for (i in 0 until waveLength - 1) {
            for (j in 0 until waveLength) {
                vertices[offs + waveLength * i * 3 * 2 + j * 3 * 2] = 1.0f * j / waveLength
                vertices[offs + waveLength * i * 3 * 2 + j * 3 * 2 + 1] = 1.0f * i / waveLength
                vertices[offs + waveLength * i * 3 * 2 + j * 3 * 2 + 2] = 1.0f * p[i][j].z
                vertices[offs + waveLength * i * 3 * 2 + j * 3 * 2 + 3] = 1.0f * j / waveLength
                vertices[offs + waveLength * i * 3 * 2 + j * 3 * 2 + 4] =
                    1.0f * (i + 1) / waveLength
                vertices[offs + waveLength * i * 3 * 2 + j * 3 * 2 + 5] = 1.0f * p[i + 1][j].z
            }
        }
    }

    private fun fallWaterDrop() {
        if (Math.random() * 500 > 10) {
            return
        }
        val x0 = (Math.random() * waveLength / 2 + 1).toInt() + 20
        val y0 = (Math.random() * waveLength / 2 + 1).toInt() + 20
        for (y in y0 - 5 until y0 + 5) {
            if (y < 1 || y >= waveLength - 1) continue
            for (x in x0 - 5 until x0 + 5) {
                if (x < 1 || x >= waveLength - 1) continue
                p[x][y].z =
                    10.0f / waveLength - (sqrt((sqr((y - y0).toFloat()) + sqr((x - x0).toFloat())).toDouble()) * 1.0 / waveLength).toFloat()
            }
        }
    }

    private fun timer() {
        val dx = intArrayOf(-1, 0, 1, 0)
        val dy = intArrayOf(0, 1, 0, -1)
        fallWaterDrop()
        for (y in 1 until waveLength - 1) {
            for (x in 1 until waveLength - 1) {
                val p0: P = p[x][y]
                for (i in 0..3) {
                    val p1: P = p[x + dx[i]][y + dy[i]]
                    val d =
                        sqrt((sqr(p0.x - p1.x) + sqr(p0.y - p1.y) + sqr(p0.z - p1.z)).toDouble()).toFloat() // расстояние между координатами
                    p0.vz += kDeformation * (p1.z - p0.z) / d * kSpread
                    p0.vz *= 0.99f
                }
            }
        }

        for (y in 1 until waveLength - 1) for (x in 1 until waveLength - 1) {
            val p0: P = p[x][y]
            p0.z += p0.vz
        }
        displayNet()
    }

    private fun sqr(x: Float): Float {
        return x * x
    }

    override fun onDrawFrame(gl: GL10) {
        gl.glClearColor(0.1f, 0.5f, 1f, 1f)
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT)
        gl.glLoadIdentity()
        gl.glTranslatef(
            -1f,
            -1f,
            0f
        )
        gl.glScalef(2f, 4f, 0f)
        gl.glRotatef(60f, 1f, 0f, 0f)
        gl.glColor4f(0.8f, 1f, 1f, 1f)
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY) // устанавливает состояние клиентской части
        timer()
        nioBuff()
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, floatBuffer) // определяет массив данных вершин
        gl.glDrawArrays(GL10.GL_LINES, 0, 4 * waveLength * waveLength)
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY) // отключение массивов
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        vertices = FloatArray(12 * waveLength * waveLength) // массив вершин
        p = Array(waveLength) { Array(waveLength) { P() } } // позиций
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        moveWave()
    }
}