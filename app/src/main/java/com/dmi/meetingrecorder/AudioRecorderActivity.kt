package com.dmi.meetingrecorder

import AlizeSpkRec.SimpleSpkDetSystem
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.dmi.meetingrecorder.model.FirebaseMessageConversationObject
import kotlinx.android.synthetic.main.activity_recorder.*
import java.io.InputStream
import net.gotev.speech.Speech
import net.gotev.speech.GoogleVoiceTypingDisabledException
import net.gotev.speech.SpeechRecognitionNotAvailable
import net.gotev.speech.SpeechDelegate
import java.io.IOException


/**
 * Created by ajindal on 2/22/2018.
 * @author Ankit jindal
 */
class AudioRecorderActivity : AppCompatActivity() {

    lateinit var mSimpleSpkDetection: SimpleSpkDetSystem
    var mCurrentSpeaker = 0
    private val LOG_TAG = "AudioRecordTest"
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private var mFileName: String? = null

    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null

    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    var mModelsArray = arrayOf("Ankit", "Swati", "Pooja", "Karan")
    var count = 0
    var isRecording = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recorder)
        setSupportActionBar(findViewById(R.id.toolbar))
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        Handler().postDelayed(object : Runnable {
            override fun run() {
                initialiseAliZe()
            }

        }, 500)
        fab.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
//                recordAudio()
                startVoiceToTextRecognition()
            }
        })

        Speech.init(this, getPackageName());
    }

    private fun getFileName() {
        mFileName = externalCacheDir!!.absolutePath
        mFileName += "/meeting" + count + "1.3gp"
        count++
    }

    private fun recordAudio() {
        startActivityForResult(Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION), 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1000 -> {
                if (resultCode == Activity.RESULT_OK) {
                    mSimpleSpkDetection.resetAudio()
                    mSimpleSpkDetection.resetFeatures()
                    if (data != null) {
                        createSpeakerModel()
                    }
                }
            }
        }
    }

    private fun initialiseAliZe() {
        val inputStream: InputStream = getApplicationContext().getAssets().open("MeetingRecorder.cfg")
        mSimpleSpkDetection = SimpleSpkDetSystem(inputStream, getApplicationContext().getFilesDir().getPath())
        inputStream.close()

        val backgroundModelAsset: InputStream = getApplicationContext().getAssets().open("world.gmm")
        mSimpleSpkDetection.loadBackgroundModel(backgroundModelAsset)
        backgroundModelAsset.close()

        System.out.println("System status:");
        System.out.println("  # of features: " + mSimpleSpkDetection.featureCount())  // at this point, 0
        System.out.println("  # of models: " + mSimpleSpkDetection.speakerCount())   // at this point, 0
        System.out.println("  UBM is loaded: " + mSimpleSpkDetection.isUBMLoaded())    // true
    }

    private fun checkIfVoiceMatches(data: Intent): Boolean {
        var mIsMatched = false
        mSimpleSpkDetection.addAudio(Util.getByteArrayFromUri(data))
        for (i in 0..mModelsArray.size) {
            val speakerResult = mSimpleSpkDetection.verifySpeaker(mModelsArray[i])
            System.out.println("Speaker's match score base " + speakerResult.score.toString())
            mIsMatched = speakerResult.match
        }
        return mIsMatched
    }

    private fun createSpeakerModel() {
        if (mCurrentSpeaker <= 3) {
            mSimpleSpkDetection.createSpeakerModel(mModelsArray.get(mCurrentSpeaker))
            mSimpleSpkDetection.resetAudio()
            mSimpleSpkDetection.resetFeatures()
            mCurrentSpeaker++
            System.out.println("System status:")
            System.out.println("  # of features: " + mSimpleSpkDetection.featureCount())  // at this point, 0
            System.out.println("  # of models: " + mSimpleSpkDetection.speakerCount())
        }
    }


    /// speech to text
    protected override fun onDestroy() {
        super.onDestroy();
        // prevent memory leaks when activity is destroyed
        Speech.getInstance().shutdown()
    }

    lateinit var mRecognizedText: FirebaseMessageConversationObject;

    protected fun startVoiceToTextRecognition() {
        try {
            // you must have android.permission.RECORD_AUDIO granted at this point
            Speech.getInstance().startListening(object : SpeechDelegate {
                override fun onStartOfSpeech() {
                    Log.i("speech", "speech recognition is now active")
                    if (isRecording) {
                        stopRecording()
                    }
                    startRecording()
                }

                override fun onSpeechRmsChanged(value: Float) {
                    Log.d("speech", "rms is now: " + value)
                }

                override fun onSpeechPartialResults(results: List<String>) {
                    val str = StringBuilder()
                    for (res in results) {
                        str.append(res).append(" ")
                    }

                    Log.i("speech", "partial result: " + str.toString().trim { it <= ' ' })
                }

                override fun onSpeechResult(result: String) {
                    if (result == null || result.length <= 0)
                        return;
                    Log.i("speech-result", "result: " + result)
                    Speech.getInstance().shutdown();
                    Speech.init(baseContext, getPackageName());
                    if (result.toLowerCase().endsWith("over")) {
                        stopRecording()
                        startRecording()
                    }
                    startVoiceToTextRecognition();
                }
            })
        } catch (exc: SpeechRecognitionNotAvailable) {
            Log.e("speech", "Speech recognition is not available on this device!")
            // You can prompt the user if he wants to install Google App to have
            // speech recognition, and then you can simply call:
            //
            // SpeechUtil.redirectUserToGoogleAppOnPlayStore(this);
            //
            // to redirect the user to the Google App page on Play Store
        } catch (exc: GoogleVoiceTypingDisabledException) {
            Log.e("speech", "Google voice typing must be enabled!")
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
        if (!permissionToRecordAccepted) finish()
    }

    private fun startRecording() {
        isRecording = true
        getFileName()
        mRecorder = MediaRecorder()
        mRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mRecorder?.setOutputFile(mFileName)
        mRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

        try {
            mRecorder?.prepare()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }
        mRecorder?.start()
    }

    private fun stopRecording() {
        isRecording = false
        mRecorder?.stop()
        mRecorder?.release()
        mRecorder = null
    }

    public override fun onStop() {
        super.onStop()
        if (mRecorder != null) {
            mRecorder?.release()
            mRecorder = null
        }
    }

}

