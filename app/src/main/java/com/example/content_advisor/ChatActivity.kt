package com.example.content_advisor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.content_advisor.databinding.ActivityChatBinding
import com.example.content_advisor.viewmodel.ChatViewModel

/**
 * Chat ekranı - GPT ile sohbet etmek için
 */
class ChatActivity : AppCompatActivity() {

    /**
     * View binding - layout elementlerine erişim için
     */
    private lateinit var binding: ActivityChatBinding

    /**
     * Chat adapter - RecyclerView için
     */
    private lateinit var chatAdapter: ChatAdapter

    /**
     * ViewModel - business logic için
     */
    private val viewModel: ChatViewModel by viewModels()

    /**
     * GPT API anahtarı - BuildConfig'den alınır
     * Güvenlik için local.properties'de saklanır
     */
    private val gptApiKey = BuildConfig.OPENAI_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // View binding'i başlat
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Edge-to-edge display için
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // UI bileşenlerini kur
        setupRecyclerView()
        setupSendButton()
        setupObservers()
        setupNavigation()
    }
    /**
     * RecyclerView'ı konfigüre et
     */
    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(emptyArray())
        binding.chatRecyclerview.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.chatRecyclerview.adapter = chatAdapter
    }

    /**
     * Gönder butonunu konfigüre et
     */
    private fun setupSendButton() {
        binding.sendButton.setOnClickListener {
            val messageText = binding.messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                viewModel.sendMessage(messageText, gptApiKey)
                binding.messageInput.setText("")
            }
        }
    }

    /**
     * ViewModel observer'larını kur
     */
    private fun setupObservers() {
        // Mesaj listesi değişikliklerini dinle
        viewModel.messages.observe(this) { messages ->
            // Message listesini ChatAdapter formatına çevir
            val messageArray = messages.map { message ->
                if (message.role == "user") {
                    "USER: ${message.content}"
                } else {
                    "BOT: ${message.content}"
                }
            }.toTypedArray()

            // Adapter'ı güncelle
            chatAdapter.updateMessages(messageArray)
            // En alta kaydır
            scrollToBottom()
        }

        // Loading durumunu dinle
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.sendButton.isEnabled = !isLoading // Loading sırasında butonu devre dışı bırak
        }

        // Hata durumunu dinle
        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    /**
     * Navigation butonlarını kur
     */
    private fun setupNavigation() {
        binding.navHome.setOnClickListener {
            val intent = Intent(this, MainPageActivity::class.java)
            startActivity(intent)
        }

        binding.navProfile.setOnClickListener {
            val intent = Intent(this, ProfilePageActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * RecyclerView'ı en alta kaydır
     */
    private fun scrollToBottom() {
        if (chatAdapter.itemCount > 0) {
            binding.chatRecyclerview.smoothScrollToPosition(chatAdapter.itemCount - 1)
        }
    }
}