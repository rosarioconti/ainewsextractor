package it.rc.mainactivity

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import it.rc.mainactivity.feature.aiprovides.UniversalAiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private val prefs by lazy { getSharedPreferences("settings", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        setupSettingsUI()

        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.data?.toString()?.let {
            processUrl(it)
        }
    }

    private fun processUrl(url: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val rawHtml = fetchHtmlContent(url)

            val isSummary = prefs.getBoolean("summary_mode", false)
            val aiType = prefs.getString("ai_type", "ChatGPT") ?: "ChatGPT"
            val key = prefs.getString("api_key", "") ?: ""

            val provider = UniversalAiProvider(key, aiType)
            val aiResultHtml = provider.processContent(rawHtml, isSummary)

            withContext(Dispatchers.Main) {
                webView.loadDataWithBaseURL(null, aiResultHtml, "text/html", "UTF-8", null)
            }
        }
    }

    private fun setupSettingsUI() {
        val modeGroup = findViewById<RadioGroup>(R.id.modeGroup)
        val aiSpinner = findViewById<Spinner>(R.id.aiSpinner)
        val apiKeyInput = findViewById<EditText>(R.id.apiKeyInput)
        val saveBtn = findViewById<Button>(R.id.saveBtn)

        val savedMode = prefs.getBoolean("summary_mode", false)
        val savedAi = prefs.getString("ai_type", "ChatGPT")
        val savedKey = prefs.getString("api_key", "")

        if (savedMode) {
            findViewById<RadioButton>(R.id.radioSummary).isChecked = true
        } else {
            findViewById<RadioButton>(R.id.radioFull).isChecked = true
        }

        val adapter = aiSpinner.adapter as ArrayAdapter<String>
        val spinnerPosition = adapter.getPosition(savedAi)
        aiSpinner.setSelection(spinnerPosition)

        apiKeyInput.setText(savedKey)

        saveBtn.setOnClickListener {
            val isSummary = modeGroup.checkedRadioButtonId == R.id.radioSummary
            val selectedAi = aiSpinner.selectedItem.toString()
            val key = apiKeyInput.text.toString()

            prefs.edit().apply {
                putBoolean("summary_mode", isSummary)
                putString("ai_type", selectedAi)
                putString("api_key", key)
                apply()
            }

            Toast.makeText(this, "Settings Saved!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchHtmlContent(url: String): String {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android 14)")
            connection.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            "Error fetching content: ${e.message}"
        }
    }
}