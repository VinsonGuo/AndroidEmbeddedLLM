package com.cerence.embeddedllm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cerence.embeddedllm.ui.ChatMessage
import de.kherud.llama.InferenceParameters
import de.kherud.llama.LlamaModel
import de.kherud.llama.ModelParameters
import de.kherud.llama.args.LogFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MainState(
    val messages: List<ChatMessage> = listOf(),
    val isLoadingModel: Boolean = false,
)

class MainViewModel : ViewModel() {

    companion object {
        const val TAG = "MainViewModel"
    }

    private var model: LlamaModel? = null

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state

    fun initModel(modelPath: String, adapterPath: String? = null) = viewModelScope.launch {
        _state.update { it.copy(isLoadingModel = true) }
        withContext(Dispatchers.IO) {
            LlamaModel.setLogger(LogFormat.JSON) { _, msg ->
                Log.d(TAG, msg)
            }
            val modelParams: ModelParameters = ModelParameters()
                .setModelFilePath(modelPath)
                .setNThreads(16)
            if (adapterPath != null) {
                modelParams.setLoraAdapters(mapOf(adapterPath to 1F))
            }
            Log.d(TAG, modelParams.toString())
            model = LlamaModel(modelParams)
        }
        _state.update { it.copy(isLoadingModel = false) }
    }

    fun sendMessage(msg: String) {
        Log.d(TAG, "sendMessage -> $msg")
        _state.update {
            val messages = it.messages + ChatMessage(msg, true)
            it.copy(messages = messages)
        }
        generate(msg)
    }

    fun generate(prompt: String) = viewModelScope.launch {
        Log.d(TAG, "generate for prompt ->  $prompt")
        var msg = ""
        withContext(Dispatchers.IO) {
            model?.complete(InferenceParameters(""))
            val parameters = InferenceParameters(prompt)
            model?.generate(parameters)?.forEach { output ->
                Log.d(TAG, "generate output -> " + output.text)
                msg += output.text

                val copiedList = state.value.messages.toMutableList()
                val last = copiedList.lastOrNull()
                if (last != null && !last.isMe) {
                    val newItem = last.copy(text = msg)
                    copiedList.removeLast()
                    _state.update {
                        it.copy(messages = copiedList + newItem)
                    }
                } else {
                    _state.update {
                        it.copy(messages = it.messages + ChatMessage(msg, false))
                    }
                }
            }
        }
    }

}