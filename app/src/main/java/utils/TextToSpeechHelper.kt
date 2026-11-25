package com.visionmate.app.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TextToSpeechHelper(context: Context) :
    TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var isReady = false
    private var pendingText: String? = null

    init {
        textToSpeech = TextToSpeech(
            context.applicationContext,
            this
        )
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            val result = textToSpeech?.setLanguage(
                Locale.getDefault()
            )

            isReady =
                result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED

            if (isReady) {
                pendingText?.let {
                    speak(it)
                    pendingText = null
                }
            }
        }
    }

    fun speak(text: String) {

        if (isReady) {
            textToSpeech?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "VisionMateSpeech"
            )
        } else {
            pendingText = text
        }
    }

    fun stop() {
        textToSpeech?.stop()
        pendingText = null
    }

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isReady = false
    }
}
