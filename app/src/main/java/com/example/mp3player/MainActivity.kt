package com.example.mp3player

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mp: MediaPlayer
    private var totalTime: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mp = MediaPlayer.create(this, R.raw.first)
        mp.isLooping = true
        mp.setVolume(0.5f, 0.5f)
        totalTime = mp.duration

        playButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                playButtonClick(v)
            }
        })
        

        loudnessBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        var volume = progress / 100.0f
                        mp.setVolume(volume, volume)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            }
        )

        progressBar.max = totalTime
        progressBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mp.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )
        Thread(Runnable {
            while(mp!=null){
            try{
                var msg = Message()
                msg.what = mp.currentPosition
                handler.sendMessage(msg)
                Thread.sleep(1000)

            } catch(e: InterruptedException){

            }
        }
        }).start()


    }
    @SuppressLint("HandlerLeak")
    var handler = object : Handler(){
        override fun handleMessage(msg: Message) {
            var currentPosition = msg.what

            progressBar.progress = currentPosition

            var elapsedTime = createTimeLabel(currentPosition)
            currentTime.text = elapsedTime //TODO

            var remainingTimeValue = createTimeLabel(totalTime - currentPosition)
            remainingTime.text = "-$remainingTimeValue"

        }
    }

    fun createTimeLabel(time: Int):String{
        var timeLabel = ""
        var min = time /1000/60
        var sec = time /1000 %60

        timeLabel = "$min:"
        if(sec<10)timeLabel+="0"
        timeLabel+=sec
        return timeLabel
    }




    fun playButtonClick(v: View?){
        if (mp.isPlaying){
            mp.pause()
            playButton.setBackgroundResource(R.drawable.play)
        }
        else{
            mp.start()
            playButton.setBackgroundResource(R.drawable.pause)
        }
    }
}

