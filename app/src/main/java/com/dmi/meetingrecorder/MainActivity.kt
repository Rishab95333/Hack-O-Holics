package com.dmi.meetingrecorder

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DialogTitle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.dmi.meetingrecorder.controller.DialogAdapter
import com.dmi.meetingrecorder.controller.SpeakerLabelDiarization
import com.dmi.meetingrecorder.model.DialogConversation
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView
    private var listening = false
    private var speechService: SpeechToText? = null
    private var capture: MicrophoneInputStream? = null
    var recoTokens: SpeakerLabelDiarization.RecoTokens? = null
    private var microphoneHelper: MicrophoneHelper? = null
    lateinit var dialogConversationList: ArrayList<DialogConversation>
    lateinit var dialogAdapter: DialogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        //Setup Recycler view
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(true)

        dialogConversationList = ArrayList<DialogConversation>()

        dialogAdapter = DialogAdapter()
        dialogAdapter.list = dialogConversationList
        recyclerView.adapter = dialogAdapter

        microphoneHelper = MicrophoneHelper(this)

        fab.setOnClickListener { view ->
            // recordMessage()
            startActivity(Intent(this, AudioRecorderActivity::class.java))
        }
    }

    //Record a message via Watson Speech to Text
    private fun recordMessage() {
        speechService = SpeechToText()
        speechService!!.setUsernameAndPassword("<UserName>", "<Password>")

        if (!listening) {
            capture = microphoneHelper?.getInputStream(true)
            Thread(Runnable {
                try {
                    speechService!!.recognizeUsingWebSocket(capture, getRecognizeOptions(), MicrophoneRecognizeDelegate())
                } catch (e: Exception) {
                    showError(e)
                }
            }).start()
            listening = true
            Toast.makeText(this@MainActivity, "Listening....Click to Stop", Toast.LENGTH_LONG).show()


        } else {
            try {
                microphoneHelper?.closeInputStream()
                listening = false
                Toast.makeText(this@MainActivity, "Stopped Listening....Click to Start", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun enableMicButton() {
        runOnUiThread { fab.setEnabled(true) }
    }

    private fun showError(e: Exception) {
        runOnUiThread {
            Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    //Private Methods - Speech to Text
    private fun getRecognizeOptions(): RecognizeOptions {
        return RecognizeOptions.Builder()
                .contentType(ContentType.OPUS.toString())
                .model("en-UK_NarrowbandModel")
                .interimResults(true)
                .inactivityTimeout(2000)
                .speakerLabels(true)
                .build()
    }

    private inner class MicrophoneRecognizeDelegate : BaseRecognizeCallback() {

        override fun onTranscription(speechResults: SpeechResults?) {
            println(speechResults)
            recoTokens = SpeakerLabelDiarization.RecoTokens()
            if (speechResults!!.speakerLabels != null) {
                recoTokens!!.add(speechResults)
                Log.i("SpeechResults", speechResults.speakerLabels[0].toString())
            }

            if (speechResults.results != null && !speechResults.results.isEmpty()) {
                val text = speechResults.results[0].alternatives[0].transcript
                var dialogConversation = DialogConversation()
                dialogConversation.dialog = text
                dialogConversationList.add(dialogConversation)
                runOnUiThread(Runnable { dialogAdapter.notifyDataSetChanged() })
            }
        }

        override fun onConnected() {

        }

        override fun onError(e: Exception) {
            showError(e)
            enableMicButton()
        }

        override fun onDisconnected() {
            enableMicButton()
        }

        override fun onInactivityTimeout(runtimeException: RuntimeException?) {

        }

        override fun onListening() {

        }

        override fun onTranscriptionComplete() {

        }
    }
}
