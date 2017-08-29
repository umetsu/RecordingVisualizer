package net.prunusmume.recordingvisualizer

import android.media.MediaRecorder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * MediaRecorderを使って振幅を定期的に取る処理はこれを参考にした。
 * https://stackoverflow.com/questions/14295427/android-audio-recording-with-voice-level-visualization
 *
 * 最初はAudioRecordを使っていた。
 * そのときの処理は次のものを参考にした。
 * https://github.com/newventuresoftware/WaveformControl
 */
class AudioRecorder {

    companion object {

        private val AUDIO_DATA_PUBLISH_INTERVAL_MILLIS: Long = 33L
        private val AUDIO_DATA_SIZE: Int = 50

        private val random = Random()

        private fun createPreparedMediaRecorder(filePath: String): MediaRecorder {
            // mp3で録音する
            // https://stackoverflow.com/questions/11985518/android-record-sound-in-mp3-format
            val r = MediaRecorder()
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setOutputFile(filePath)
            r.prepare()
            return r
        }
    }

    private val audioDataSubject: PublishSubject<ShortArray> = PublishSubject.create()

    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var disposable: Disposable

    var isRecording: Boolean = false
        private set(value) {
            field = value
        }

    fun onAudioDataChanged(): Observable<ShortArray> {
        return audioDataSubject
    }

    fun startRecording(filePath: String) {
        if (isRecording) return

        isRecording = true

        mediaRecorder = createPreparedMediaRecorder(filePath)
        mediaRecorder.start()

        disposable = Observable.interval(AUDIO_DATA_PUBLISH_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
                .map { mediaRecorder.maxAmplitude }
                .map { amplitude ->
                    ShortArray(AUDIO_DATA_SIZE)
                            .mapIndexed { index, _ ->
                                val degree = index / AUDIO_DATA_SIZE.toFloat() * 180f
                                val input = Math.sin(degree * Math.PI / 180)
                                val noise = random.nextFloat() * Math.sin(10 * input) * (-(input - 1) * (input - 1) + 1)
                                (amplitude * Math.abs(noise)).toShort()
                            }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturnItem(ShortArray(0).toList())
                .subscribe({
                    audioDataSubject.onNext(it.toShortArray())
                })
    }

    fun stopRecording() {
        if (!isRecording) return

        audioDataSubject.onNext(ShortArray(0))

        disposable.dispose()

        try {
            mediaRecorder.stop()
            mediaRecorder.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        isRecording = false
    }
}