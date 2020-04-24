package com.dmi.meetingrecorder

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatTextView
import android.view.View
import android.widget.TextView
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesOptions
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.SentimentOptions

/**
 * Created by Rishab on 11/10/2019.
 * @author Rishab Arora
 */
class NLPActivity : AppCompatActivity() {

    private var mDocumentContent = "Speaker 0. Speaker 1. speaker 2."
    private var output = ""


    val entityOptions = EntitiesOptions.Builder()
            .emotion(true)
            .sentiment(true)
            .build()

    val sentimentOptions = SentimentOptions.Builder()
            .document(true)
            .build()
    val features = Features.Builder()
            .entities(entityOptions)
            .sentiment(sentimentOptions)
            .build()

    val analyzerOptions = AnalyzeOptions.Builder()
            .text(mDocumentContent)
            .features(features)
            .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nlp)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar!!.title = "LanguageProcessing"
        if (intent.hasExtra("ContentData")) {
            mDocumentContent = intent.getStringExtra("ContentData")
        }
        var analyzer = NaturalLanguageUnderstanding(
                NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27,
                resources.getString(
                        R.string.natural_language_understanding_username),
                resources.getString(
                        R.string.natural_language_understanding_password)
        )

        AsyncTask.execute {
            val results = analyzer.analyze(analyzerOptions).execute()

            for (entity in results.entities) {
                output += "${entity.text} (${entity.type})\n"

                val validEmotions = arrayOf("Anger", "Joy", "Disgust",
                        "Fear", "Sadness")
                val emotionValues = arrayOf(
                        entity.emotion.anger,
                        entity.emotion.joy,
                        entity.emotion.disgust,
                        entity.emotion.fear,
                        entity.emotion.sadness
                )
                val currentEmotion = validEmotions[
                        emotionValues.indexOf(
                                emotionValues.max()
                        )
                        ]

                output += "Emotion: ${currentEmotion}, " +
                        "Sentiment: ${entity.sentiment.score}" +
                        "\n\n"

                System.out.println(output)
                setText()
            }
        }
    }

    private fun setText() {
        runOnUiThread(object : Runnable {
            override fun run() {
                var viewText: AppCompatTextView = findViewById<View>(R.id.document_text) as AppCompatTextView
                viewText.setText(output)
            }

        })
    }
}