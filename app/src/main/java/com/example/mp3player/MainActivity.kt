package com.example.mp3player

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.SeekBar
import com.mtechviral.mplaylib.MusicFinder
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.longToast
import android.provider.MediaStore
import android.graphics.Bitmap
import org.jetbrains.anko.image
import java.io.FileNotFoundException


class MainActivity : AppCompatActivity() {

    private lateinit var mp: MediaPlayer
    private var totalTime: Int = 0
    private var songs:MutableList<MusicFinder.Song> =  mutableListOf<MusicFinder.Song>()
    var currentSongNumber = 0
    var currentVolume =0.5f
    var isPlayed = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // storage permission
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            //asking for permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),0)
        }else{
            createSongList()
        }


        changeMusic(currentSongNumber, true)

        playButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                playButtonClick(v)
            }
        })

        nextButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                nextSong()
            }
        })
        previousButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                previousSong()
            }
        })


        loudnessBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        currentVolume = progress / 100.0f
                        mp.setVolume(currentVolume, currentVolume)
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
            while(true){
                try{
                    val msg = Message()
                    msg.what = mp.currentPosition
                    handler.sendMessage(msg)
                    Thread.sleep(1000)

                } catch(e: InterruptedException){
                    Log.d("CUSTOM", "thread Interrupted")
                }
        }
        }).start()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            createSongList()
        } else {
            longToast("Permission not granted")
        }
    }

    private fun createSongList(){
        val songFinder = MusicFinder(contentResolver)
        songFinder.prepare()
        songs = songFinder.allSongs
        Log.d("CUSTOM", "list Size "+songs.size.toString())
    }


    @SuppressLint("HandlerLeak")
    var handler = object : Handler(){
        override fun handleMessage(msg: Message) {
            val currentPosition = msg.what

            progressBar.progress = currentPosition

            val elapsedTime = createTimeLabel(currentPosition)
            currentTime.text = elapsedTime

            val remainingTimeValue = createTimeLabel(totalTime - currentPosition)
            remainingTime.text = remainingTimeValue

        }
    }

    fun createTimeLabel(time: Int):String{
        var timeLabel: String
        val min = time /1000/60
        val sec = time /1000 %60

        timeLabel = "$min:"
        if(sec<10)timeLabel+="0"
        timeLabel+=sec
        return timeLabel
    }

    fun playButtonClick(v: View?){
        if (mp.isPlaying){
            isPlayed=false
            mp.pause()
            playButton.setBackgroundResource(R.drawable.play)
        }
        else{
            isPlayed=true
            mp.start()
            playButton.setBackgroundResource(R.drawable.pause)
        }
    }

    private fun changeMusic( id:Int, firstTime:Boolean=false){Log.d("CUSTOM", "!!!")
        if (!songs.isEmpty()) {
            Log.d("CUSTOM", "change music to id: $id")
            val song = songs[id]

            if(!firstTime)
                mp.reset()

            mp = MediaPlayer.create(this, song.uri)
            Log.d("CUSTOM", "choose "+song.title)
            if(isPlayed)
                mp.start()
            Log.d("CUSTOM", "started ")
            mp.isLooping = true
            mp.setVolume(currentVolume, currentVolume)

            totalTime = mp.duration
            progressBar.max = totalTime

            Log.d("CUSTOM", "wait for image load"+song.albumArt.toString())
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, song.albumArt)
                albumArt.setImageBitmap(Bitmap.createScaledBitmap(bitmap,250,250,false))

            } catch (e: FileNotFoundException) {
                albumArt.image = ContextCompat.getDrawable(this, R.drawable.musical_note)
                Log.d("CUSTOM", "Load default image")
            } catch (e: Exception){
                albumArt.image = ContextCompat.getDrawable(this, R.drawable.musical_note)
                Log.d("CUSTOM", e.toString())
            }

        }else
            Log.d("CUSTOM", "Empty music list")
    }

    private fun nextSong(){
        if (currentSongNumber+1==songs.size)
            currentSongNumber=0
        else
            currentSongNumber++
        changeMusic(currentSongNumber)
    }
    private fun previousSong(){
        if (currentSongNumber==0)
            currentSongNumber=songs.size-1
        else
            currentSongNumber--
        changeMusic(currentSongNumber)
    }
}

