package com.dmi.meetingrecorder

import AlizeSpkRec.SimpleSpkDetSystem
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_recorder.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * Created by ajindal on 2/22/2018.
 * @author Ankit jindal
 */
class AudioRecorderActivity : AppCompatActivity() {

    lateinit var mSimpleSpkDetection: SimpleSpkDetSystem

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
                    System.out.println("File Name Retrieved: " + data?.data)
                    val uri: Uri = data?.data!!
                    System.out.print("Audio file: " + uri.path)
                    mSimpleSpkDetection.addAudio(Util.getByteArrayFromUri(data))
                    mSimpleSpkDetection.createSpeakerModel("Ankit")
                    System.out.println("System status:");
                    System.out.println("  # of features: " + mSimpleSpkDetection.featureCount())  // at this point, 0
                    System.out.println("  # of models: " + mSimpleSpkDetection.speakerCount())
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
}

