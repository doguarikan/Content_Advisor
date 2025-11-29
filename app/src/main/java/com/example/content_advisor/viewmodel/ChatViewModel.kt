package com.example.content_advisor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.content_advisor.model.Message
import com.example.content_advisor.repository.GptRepository
import kotlinx.coroutines.launch


class ChatViewModel : ViewModel() {

    private val repository = GptRepository()

    /**
     * Mesaj listesi - UI'da gösterilecek
     */
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    /**
     * Loading durumu - Progress bar gösterimi için
     */
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Hata mesajı - Toast gösterimi için
     */
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * Konuşma geçmişi - GPT'ye gönderilecek
     */
    private val conversationHistory = mutableListOf<Message>()

    /**
     * ViewModel başlatıldığında çağrılır
     */
    init {
        _messages.value = emptyList()
        _isLoading.value = false
        _error.value = null
        
        // Hoş geldin mesajı ekle
        val welcomeMessage = Message(
            "assistant", 
            "Hello! I'm CineMate AI, your specialized assistant for movie and TV series recommendations. " +
            "I can help you with:\n\n" +
            "🎬 Movie and TV show recommendations\n" +
            "🎭 Actor and director filmographies\n" +
            "📺 Genre-based suggestions (comedy, drama, action, etc.)\n" +
            "🔍 Similar content recommendations\n" +
            "⭐ Movie/TV show reviews and ratings\n\n" +
            "What kind of content are you looking for?"
        )
        conversationHistory.add(welcomeMessage)
        updateMessages()
    }

    /**
     * Kullanıcı mesajını GPT'ye gönderir
     *
     * @param userMessage Kullanıcının yazdığı mesaj
     * @param apiKey OpenAI API anahtarı
     */
    fun sendMessage(userMessage: String, apiKey: String) {
        // Boş mesaj kontrolü
        if (userMessage.isBlank()) return

        // Kullanıcı mesajını konuşma geçmişine ekle
        val userMsg = Message("user", userMessage)
        conversationHistory.add(userMsg)
        updateMessages()

        // Loading durumunu başlat
        _isLoading.value = true
        _error.value = null

        // Coroutine ile API çağrısını yap
        viewModelScope.launch {
            val result = repository.sendMessage(userMessage, conversationHistory, apiKey)

            // Sonucu işle
            result.fold(
                onSuccess = { response ->
                    // Başarılı yanıt - GPT mesajını ekle
                    val botMsg = Message("assistant", response)
                    conversationHistory.add(botMsg)
                    updateMessages()
                },
                onFailure = { exception ->
                    // Hata durumu - UI'da göster
                    _error.value = "Error: ${exception.message}"
                }
            )

            // Loading durumunu bitir
            _isLoading.value = false
        }
    }

    /**
     * Mesaj listesini UI'a bildir
     */
    private fun updateMessages() {
        _messages.value = conversationHistory.toList()
    }

    /**
     * Hata mesajını temizle
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Konuşma geçmişini temizle
     */
    fun clearConversation() {
        conversationHistory.clear()
        updateMessages()
    }
}