package net.prunusmume.recordingvisualizer

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions


class MainActivity : AppCompatActivity() {

    companion object {
        private val OUTPUT_FILE_PATH = Environment.getExternalStorageDirectory().absolutePath + "/record_test.mp3"
    }

    private lateinit var recorder: AudioRecorder

    private val visualizerView: AudioVisualizerView by lazy {
        findViewById<AudioVisualizerView>(R.id.visualizer_view)
    }

    private val fab: FloatingActionButton by lazy {
        findViewById<FloatingActionButton>(R.id.fab)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        RxPermissions(this)
                .request(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe({ granted ->
                    init()
                })
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun init() {
        recorder = AudioRecorder()

        fab.setImageResource(R.drawable.ic_mic_black_24px)
        fab.setOnClickListener { view ->
            val status = if (!recorder.isRecording) {
                recorder.startRecording(OUTPUT_FILE_PATH)
                fab.setImageResource(R.drawable.ic_mic_off_black_24px)

                "Recording Started"
            } else {
                recorder.stopRecording()
                fab.setImageResource(R.drawable.ic_mic_black_24px)

                "Recording Stopped"
            }
            Snackbar.make(view, status, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()
        }

        recorder.onAudioDataChanged()
                .subscribe({ audioData ->
                    visualizerView.samples = audioData
                })
    }
}
