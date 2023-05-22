package ru.dolbak.circles

import android.graphics.Canvas
import android.util.Log
import android.view.SurfaceHolder
import com.sergeyrusak.lamps.MySurface

class DrawThread(private val mySurface: MySurface, private val surfaceHolder: SurfaceHolder): Thread() {
    var isRunning: Boolean = true
    var nowTime = 0L
    var prevTime = 0L
    var deltaTime = 0L

    init {
        prevTime = System.currentTimeMillis()
    }

    fun setRun(r: Boolean){
        isRunning = r
    }

    override fun run(){
        Log.d("myLog", "THREAD STARTED")
        while (isRunning){
            if (!surfaceHolder.surface.isValid){
                continue
            }
            nowTime = System.currentTimeMillis()
            deltaTime = nowTime - prevTime
            if (deltaTime > 30){
                prevTime = nowTime
                val canvas = surfaceHolder.lockCanvas(null)
                if (canvas == null){
                    continue
                }
                doDraw(mySurface, surfaceHolder, canvas)
                if (canvas != null){
                    surfaceHolder.unlockCanvasAndPost(canvas)
                }
            }
        }

        Log.d("myLog", "THREAD STOPPED")
    }

    @Synchronized fun doDraw(mySurface: MySurface, surfaceHolder: SurfaceHolder, canvas: Canvas){
        mySurface.draw(canvas)
    }
}