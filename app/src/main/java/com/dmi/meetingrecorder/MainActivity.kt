package com.dmi.meetingrecorder

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.dmi.meetingrecorder.controller.DialogAdapter
import com.dmi.meetingrecorder.controller.SpeakerLabelDiarization
import com.dmi.meetingrecorder.model.DialogConversation
import com.dmi.meetingrecorder.summarize.SummarizationActivity
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.BaseRecognizeCallback
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


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
    var isFirst = true
    var path = ""
    var fileName = ""
    var meetingName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (savedInstanceState == null) {
            val extras = intent.extras
            if (extras == null) {
                meetingName = ""
            } else {
                meetingName = extras.getString("MeetingName")
            }
        } else {
            meetingName = savedInstanceState.getSerializable("MeetingName") as String
        }
        fileName = meetingName + ".txt"

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

        recordMessage()

        path = Environment.getExternalStorageDirectory().toString();

        fab.setOnClickListener { view ->
            recordMessage()
        }
    }

    //Record a message via Watson Speech to Text
    private fun recordMessage() {
        speechService = SpeechToText()
        speechService!!.setUsernameAndPassword("fe2a111e-931b-4043-b1b0-b629d5eb5456", "Bo6qfsomMvuV")

        if (!listening) {
            fab.setImageResource(android.R.drawable.ic_media_pause)
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
            R.id.action_clear -> {
                dialogConversationList.clear()
                dialogAdapter.notifyDataSetChanged()
                return true
            }

            R.id.action_stop -> {
                if (listening) {
                    //if listening stop recording
                    recordMessage()
                }
                makeMinutesOfMeeting()
                return true
            }
            R.id.action_processing -> {
                return true
            }
            else -> {
                super.onOptionsItemSelected(item)

            }
        }
    }

    private fun makeMinutesOfMeeting() {
        var conversation = ""
        var summary = ""
        for (dialogConversation in dialogConversationList) {
            conversation = conversation + dialogConversation.speaker + ": " + dialogConversation.message + "\n"
            summary = summary + dialogConversation.message + "."
        }

        var intent = Intent(baseContext, SummarizationActivity::class.java)
        intent.putExtra("conversation", conversation)
        intent.putExtra("text", summary)
        intent.putExtra("fileName", fileName)
        intent.putExtra("meetingName", meetingName)
        intent.putExtra("path", path)

        startActivity(intent)

//        val file = File(path, fileName)
//        val stream = FileOutputStream(file)
//        try {
//            stream.write(conversation.toByteArray())
//        } finally {
//            stream.close()
//        }
//
//        shareOnMail(file)
    }

    private fun shareOnMail(file: File) {
        var emailIntent = Intent(Intent.ACTION_SEND);
        emailIntent.setType("*/*");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf("swati90.cdac@gmail.com"));
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                meetingName + " Notes");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                "Please find the summary and detailed conversation below");
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
        startActivity(emailIntent)
    }

    private fun enableMicButton() {
        runOnUiThread {
            fab.setEnabled(true)
            fab.setImageResource(android.R.drawable.ic_media_play)
        }
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
//                .model("en-UK_NarrowbandModel")
                .interimResults(true)
                .inactivityTimeout(2000)
                .speakerLabels(true)
                .build()
    }

    private inner class MicrophoneRecognizeDelegate : BaseRecognizeCallback() {

        override fun onTranscription(speechResults: SpeechResults?) {
            println(speechResults)
            var speakerLabelString = ""
            recoTokens = SpeakerLabelDiarization.RecoTokens()
            if (speechResults!!.speakerLabels != null) {
                var finalPosition = 0;
                for (count in 1 until speechResults.speakerLabels.size) {
                    if (speechResults.speakerLabels[0].confidence < speechResults.speakerLabels[count].confidence) {
                        finalPosition == count;
                    }
                }

                recoTokens!!.add(speechResults)
                speakerLabelString = "Spk " + speechResults!!.speakerLabels[finalPosition].speaker
                Log.i("SpeechResults", speechResults.speakerLabels[finalPosition].toString())
                if (speakerLabelString == null)
                    speakerLabelString = ""
                var text = dialogConversationList.get(dialogConversationList.size - 1).message
//                dialogConversationList.get(dialogConversationList.size - 1).dialog = speakerLabelString + " " + text
                dialogConversationList.get(dialogConversationList.size - 1).message = text
                dialogConversationList.get(dialogConversationList.size - 1).speaker = speakerLabelString
            }

            if (speechResults.results != null && !speechResults.results.isEmpty()) {
                var text = speechResults.results[0].alternatives[0].transcript

                if (isFirst) {
                    isFirst = false
                    var dialogConversation = DialogConversation()
                    dialogConversation.message = text
                    dialogConversationList.add(dialogConversation)
                } else {
                    dialogConversationList.get(dialogConversationList.size - 1).message = text
                }
                if (speechResults.results[0].isFinal)
                    isFirst = true
                runOnUiThread(Runnable { dialogAdapter.notifyDataSetChanged() })
            }
//            recyclerView.scrollToPosition(dialogAdapter.itemCount - 1);
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
