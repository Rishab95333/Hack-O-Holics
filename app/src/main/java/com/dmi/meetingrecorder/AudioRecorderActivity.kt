package com.dmi.meetingrecorder

import AlizeSpkRec.SimpleSpkDetSystem
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_recorder.*
import java.io.InputStream

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
            }
        })
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
}

