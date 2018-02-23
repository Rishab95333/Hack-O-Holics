package com.dmi.meetingrecorder

import AlizeSpkRec.SimpleSpkDetSystem
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
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





/**
 * Created by ajindal on 2/22/2018.
 * @author Ankit jindal
 */
class AudioRecorderActivity : AppCompatActivity() {

    lateinit var mSimpleSpkDetection: SimpleSpkDetSystem

    var mCurrentSpeaker = 0

    var mModelsArray = arrayOf("Ankit", "Swati", "Pooja", "Karan")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recorder)
        setSupportActionBar(findViewById(R.id.toolbar))
        Handler().postDelayed(object : Runnable {
            override fun run() {
                initialiseAliZe()
            }

        }, 500)
        fab.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                recordAudio()
                startVoiceToTextRecognition();
            }
        })

        Speech.init(this, getPackageName());
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

    protected fun startVoiceToTextRecognition(){
        try {
            // you must have android.permission.RECORD_AUDIO granted at this point
            Speech.getInstance().startListening(object : SpeechDelegate {
                override fun onStartOfSpeech() {
                    Log.i("speech", "speech recognition is now active")
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
                    if(result == null || result.length <= 0)
                        return;
                    Log.i("speech-result", "result: " + result)

//                    var index1 = result.indexOf("Over");
//                    var index2 = result.indexOf("over");
//                    var index3 = result.indexOf("OVER");
//
//                    if(index1 >= 0 || index2 >= 0 || index3 >= 0){
//                        var position=-1;
//                        if(index1 >= 0){
//                            position = index1;
//                        }else if(index2 >= 0){
//                            position = index2;
//                        } else if(index3 >= 0){
//                            position = index3;
//                        }
//                        if(position > 0) {
//                            mRecognizedText.message = mRecognizedText.message + result.substring(0, position)
//                            Toast.makeText(baseContext,mRecognizedText.message,Toast.LENGTH_SHORT)
//                        }
                    Speech.getInstance().shutdown();
                    Speech.init(baseContext, getPackageName());
                        startVoiceToTextRecognition();
                        // save conversation on server

                        // initialize speaker recognition again
//                    }
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
}

