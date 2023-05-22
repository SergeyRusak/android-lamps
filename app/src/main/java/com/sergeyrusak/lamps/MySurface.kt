package com.sergeyrusak.lamps

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import ru.dolbak.circles.DrawThread
import kotlin.math.abs
import kotlin.random.Random

class MySurface(context: Context?) : SurfaceView(context), SurfaceHolder.Callback {
    private var side = 2
    private lateinit var field: ArrayList<ArrayList<Boolean>>
    private lateinit var drawThread: DrawThread
    private lateinit var paint: Paint
    private lateinit var centres: ArrayList<ArrayList<Point>>
    private var gap = 0.0
    private var started = false
    private var freezed = false
    private var timeStarted = 0L
    private var finishedTime = 0L
    private var restartX = 0f
    private var restartY = 0f

    data class Point(var x: Float, var y: Float)

    init {
        generateField()
        Log.d("mylog", "INIT")
        holder.addCallback(this)
        paint = Paint()
    }

    fun checkWin(){
        for (i in 0 until side){
            for (j in 0 until side){
                if (field[0][0] != field[i][j]){
                    return
                }
            }
        }
        side+=2
        if (side > 8){
            side = 8
            freezed = true
            finishedTime = System.currentTimeMillis() - timeStarted
            return
        }
        generateField()
    }

    private fun generateField(){
        field = ArrayList<ArrayList<Boolean>>()
        centres = ArrayList<ArrayList<Point>>(0)
        for (i in 0 until side){
            val newRaw = ArrayList<Boolean>()
            val newCentres = ArrayList<Point>()
            for (j in 0 until side){
                newRaw.add(Random.nextBoolean())
                newCentres.add(Point(0f, 0f))
            }
            field.add(newRaw)
            centres.add(newCentres)
        }


    }

    private fun onTouch(x: Float, y: Float){
        Log.d("mylog", "$x $y")
        for (i in 0 until side){
            for (j in 0 until side){
                if (abs(centres[i][j].x - x) < gap / 2 && abs(centres[i][j].y - y) < gap / 2){
                    Log.d("myLog", "BUBBLE: $x $y")
                    reverseBubbles(i, j)
                    checkWin()
                    return
                }
            }
        }
    }

    private fun reverseBubbles(y: Int, x: Int){
        for (i in 0 until side){
            field[y][i] = !field[y][i]
        }
        for (i in 0 until side){
            if (i == y){
                continue
            }
            field[i][x] = !field[i][x]
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!started){
            started = true
            timeStarted = System.currentTimeMillis()
        }
        if (event != null) {
            if (event.action == MotionEvent.ACTION_DOWN){
                Log.d("myLog", "click")
                if (!freezed){
                    onTouch(event.x, event.y)
                }
                if (abs(event.x - restartX) < 600 && abs(event.y - restartY) < 100){
                    Log.d("myLog", "RESET")
                    side = 2
                    timeStarted = 0
                    finishedTime = 0
                    started = false
                    freezed = false
                    generateField()
                }
            }
        }
        return true
    }

    override fun draw(canvas: Canvas){
        super.draw(canvas)
        gap = width / (side * 1.5)
        var y = (height - width) / 2 + gap / 2
        var x = gap * 0.75
        canvas.drawColor(Color.WHITE)
        paint.color = Color.BLACK

        paint.textSize = 100.0f
        var lvl = side/2
        if (!freezed){
            canvas.drawText("Уровень ${lvl}", 50f, 200f, paint)
        }
        var time = "0.0"
        if (started){
            time = String.format("%.2f", (System.currentTimeMillis() - timeStarted)  / 1000f)
        }
        if (freezed){
            time = String.format("%.2f", finishedTime  / 1000f)
            paint.color = Color.GREEN
            canvas.drawText("Игра окончена!", 200f, 200f, paint)
            paint.color = Color.BLACK
        }
        canvas.drawText("Время $time", 50f, 350f, paint)


        for (i in 0 until field.size){
            for (j in 0 until field[i].size){
                paint.color =  if (field[i][j]) Color.YELLOW else Color.BLACK

                centres[i][j].x = (x + gap * j * 1.5).toFloat()
                centres[i][j].y = (y + gap * i * 1.5).toFloat()
                canvas.drawCircle(centres[i][j].x,centres[i][j].y,(gap / 2).toFloat(), paint)
            }
        }

        paint.color = Color.BLACK
        canvas.drawRect(200f, height - 350f, 1000f, height - 150f, paint)
        paint.color = Color.WHITE
        canvas.drawText("ПЕРЕЗАПУСК", 250f, height - 200f, paint)
        restartY = (height - 350f + height - 150f) / 2
        restartX = (200f + 1000f) / 2
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d("myLog", "create")
        drawThread = DrawThread(this, getHolder())
        drawThread.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var stop = true
        drawThread.setRun(false)
        while (stop){
            try{
                drawThread.join()
                stop = false
            } catch (e: InterruptedException){
                e.printStackTrace()
            }
        }
    }
}