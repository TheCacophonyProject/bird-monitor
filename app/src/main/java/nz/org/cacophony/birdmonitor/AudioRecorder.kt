package nz.org.cacophony.birdmonitor

import android.Manifest
import android.media.AudioRecord
import androidx.annotation.RequiresApi
import android.os.Build
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.app.Activity
import android.content.Context
import android.media.AudioFormat
import nz.org.cacophony.birdmonitor.AudioRecorder
import android.media.MediaRecorder
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception

class AudioRecorder {
    private var isRecording = false
    protected var audioRecord: AudioRecord? = null
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun startRecording(context: Context?) {
        if (ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    (context as Activity?)!!,
                    Manifest.permission.RECORD_AUDIO
                )
            ) {
                ActivityCompat.requestPermissions(
                    (context as Activity?)!!,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    1
                )
            }
        }
        try {
            audioRecord = AudioRecord.Builder()
                .setAudioSource(AUDIO_SOURCE)
                .setBufferSizeInBytes(BUFFER_SIZE_RECORDING)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(CHANNEL_CONFIG)
                        .setEncoding(AUDIO_FORMAT)
                        .build()
                )
                .build()
            audioRecord.startRecording()
            isRecording = true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating AudioRecord", e)
            return
        }
    }

    fun writeAudioData(file: File?) { // to be called in a Runnable for a Thread created after call to startRecording()
        val data =
            ByteArray(BUFFER_SIZE_RECORDING / 2) // assign size so that bytes are read in in chunks inferior to AudioRecord internal buffer size
        var outputStream: FileOutputStream? = null
        try {
            outputStream =
                FileOutputStream(file) //fileName is path to a file, where audio data should be written
        } catch (e: FileNotFoundException) {
            // handle error
        }
        while (isRecording) { // continueRecording can be toggled by a button press, handled by the main (UI) thread
            val read = audioRecord!!.read(data, 0, data.size)
            try {
                outputStream!!.write(data, 0, read)
            } catch (e: IOException) {
                Log.d(TAG, "exception while writing to file")
                e.printStackTrace()
            }
        }
        try {
            outputStream!!.flush()
            outputStream.close()
        } catch (e: IOException) {
            Log.d(TAG, "exception while closing output stream $e")
            e.printStackTrace()
        }

        // Clean up
        audioRecord!!.stop()
        audioRecord!!.release()
        audioRecord = null
    }

    fun stop() {
        isRecording = false
    }

    companion object {
        private val TAG = AudioRecorder::class.java.name
        const val AUDIO_SOURCE =
            MediaRecorder.AudioSource.MIC // for raw audio, use MediaRecorder.AudioSource.UNPROCESSED, see note in MediaRecorder section
        const val SAMPLE_RATE = 44100
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        val BUFFER_SIZE_RECORDING =
            AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
    }
}