package com.dmi.meetingrecorder

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesOptions
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.SentimentOptions

/**
 * Created by ajindal on 2/23/2018.
 * @author Ankit jindal
 */
class NLPActivity : AppCompatActivity() {

    private val mDocumentContent = "Hi, my name is Ankit. I live in Delhi. My Hobby is badmintion and I love travelling."
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
            }
        }
    }
}