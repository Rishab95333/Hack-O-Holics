package com.dmi.meetingrecorder.controller

import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeakerLabel
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechTimestamp
import com.ibm.watson.developer_cloud.util.GsonSingleton
import java.util.ArrayList
import java.util.LinkedHashMap
import java.util.concurrent.CountDownLatch

/**
 * Created by dmi on 16/02/18.
 */
public class SpeakerLabelDiarization {
   public class RecoToken {
       var startTime: Double? = null
       var endTime: Double? = null
       var speaker: Int? = null
       var word: String? = null
       var spLabelIsFinal: Boolean? = null

        /**
         * Instantiates a new reco token.
         *
         * @param speechTimestamp the speech timestamp
         */
        internal constructor(speechTimestamp: SpeechTimestamp) {
            startTime = speechTimestamp.startTime
            endTime = speechTimestamp.endTime
            word = speechTimestamp.word
        }

        /**
         * Instantiates a new reco token.
         *
         * @param speakerLabel the speaker label
         */
        internal constructor(speakerLabel: SpeakerLabel) {
            startTime = speakerLabel.from
            endTime = speakerLabel.to
            speaker = speakerLabel.speaker
        }

        /**
         * Update from.
         *
         * @param speechTimestamp the speech timestamp
         */
        internal fun updateFrom(speechTimestamp: SpeechTimestamp) {
            word = speechTimestamp.word
        }

        /**
         * Update from.
         *
         * @param speakerLabel the speaker label
         */
        internal fun updateFrom(speakerLabel: SpeakerLabel) {
            speaker = speakerLabel.speaker
        }
    }

    /**
     * The Class Utterance.
     */
   public class Utterance
    /**
     * Instantiates a new utterance.
     *
     * @param speaker the speaker
     * @param transcript the transcript
     */
    (val speaker: Int?, transcript: String) {
        var transcript = ""

        init {
            this.transcript = transcript
        }
    }

    /**
     * The Class RecoTokens.
     */
    class RecoTokens {

        private val recoTokenMap: MutableMap<Double, RecoToken>

        /**
         * Instantiates a new reco tokens.
         */
        init {
            recoTokenMap = LinkedHashMap()
        }

        /**
         * Adds the.
         *
         * @param speechResults the speech results
         */
        fun add(speechResults: SpeechResults) {
            if (speechResults.results != null)
                for (i in 0 until speechResults.results.size) {
                    val transcript = speechResults.results[i]
                    if (transcript.isFinal) {
                        val speechAlternative = transcript.alternatives[0]

                        for (ts in 0 until speechAlternative.timestamps.size) {
                            val speechTimestamp = speechAlternative.timestamps[ts]
                            add(speechTimestamp)
                        }
                    }
                }
            if (speechResults.speakerLabels != null)
                for (i in 0 until speechResults.speakerLabels.size) {
                    add(speechResults.speakerLabels[i])
                }

        }

        /**
         * Adds the.
         *
         * @param speechTimestamp the speech timestamp
         */
        fun add(speechTimestamp: SpeechTimestamp) {
            var recoToken: RecoToken? = recoTokenMap[speechTimestamp.startTime]
            if (recoToken == null) {
                recoToken = RecoToken(speechTimestamp)
                recoTokenMap[speechTimestamp.startTime] = recoToken
            } else {
                recoToken.updateFrom(speechTimestamp)
            }
        }

        /**
         * Adds the.
         *
         * @param speakerLabel the speaker label
         */
        fun add(speakerLabel: SpeakerLabel) {
            var recoToken: RecoToken? = recoTokenMap[speakerLabel.from]
            if (recoToken == null) {
                recoToken = RecoToken(speakerLabel)
                recoTokenMap[speakerLabel.from] = recoToken
            } else {
                recoToken.updateFrom(speakerLabel)
            }

            if (speakerLabel.isFinal) {
                markTokensBeforeAsFinal(speakerLabel.from)
                report()
                cleanFinal()
            }
        }

        private fun markTokensBeforeAsFinal(from: Double) {
            val recoTokenMap = LinkedHashMap<Double, RecoToken>()

            for (rt in recoTokenMap.values) {
                if(rt.startTime==null)
                    return;
                if (rt.startTime!! <= from)
                    rt.spLabelIsFinal = true
            }
        }

        /**
         * Report.
         */
        fun report() {
            val uttterances = ArrayList<Utterance>()
            var currentUtterance = Utterance(0, "")

            for (rt in recoTokenMap.values) {
                if (currentUtterance.speaker !== rt.speaker) {
                    uttterances.add(currentUtterance)
                    currentUtterance = Utterance(rt.speaker, "")
                }
                currentUtterance.transcript = currentUtterance.transcript + rt.word + " "
            }
            uttterances.add(currentUtterance)

            val result = GsonSingleton.getGson().toJson(uttterances)
            println(result)
        }

        private fun cleanFinal() {
            val set = recoTokenMap.entries
            for ((key, value) in set) {
                if (value.spLabelIsFinal!!) {
                    recoTokenMap.remove(key)
                }
            }
        }

    }


    private val lock = CountDownLatch(1)
}