package uz.safar.ttsproxy

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Ilovaning ko'rinadigan (launcher) ekrani. PhoneGroupingTtsService o'zi
 * hech qanday UI'ga ega emas (u faqat tizim nutq mexanizmi sifatida
 * fonda ishlaydi), shuning uchun bu Activity ilova nima ekanini
 * tushuntiradi va uni standart nutq mexanizmi qilib tanlashga yordam
 * beradi.
 *
 * MUHIM: Android'da ilova o'zini dasturiy ravishda standart TTS
 * mexanizmi qilib o'rnata olmaydi - bu doim foydalanuvchining o'zi
 * tizim sozlamalaridan qo'lda tanlashi shart (xavfsizlik cheklovi).
 * Shuning uchun bu ekran faqat sozlamalar sahifasini ochib, tanlashni
 * osonlashtiradi.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private var promptShownThisLaunch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        val openSettingsButton = findViewById<Button>(R.id.openSettingsButton)
        val checkRhvoiceButton = findViewById<Button>(R.id.checkRhvoiceButton)

        openSettingsButton.setOnClickListener { openTtsSettings() }
        checkRhvoiceButton.setOnClickListener { checkRhvoiceInstalled() }
    }

    override fun onResume() {
        super.onResume()
        val isDefault = refreshStatus()

        // Ilovaga har kirganda (onCreate/onResume birinchi marta) hali
        // standart mexanizm qilib tanlanmagan bo'lsa - so'raymiz. Faqat
        // shu "launch" davomida bir marta (masalan sozlamalarga o'tib
        // qaytganda qayta-qayta chiqavermasligi uchun).
        if (!isDefault && !promptShownThisLaunch) {
            promptShownThisLaunch = true
            showSetDefaultPrompt()
        }
    }

    /** Tizimning hozirgi standart TTS mexanizmini o'qiydi va uni shu
     *  ilova bilan solishtiradi. true - agar bu ilova allaqachon standart
     *  bo'lsa. */
    private fun refreshStatus(): Boolean {
        val defaultEngine = try {
            Settings.Secure.getString(contentResolver, Settings.Secure.TTS_DEFAULT_SYNTH)
        } catch (e: Exception) {
            null
        }
        val isDefault = defaultEngine == packageName
        statusText.text = getString(
            if (isDefault) R.string.status_is_default else R.string.status_not_default
        )
        return isDefault
    }

    private fun showSetDefaultPrompt() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title)
            .setMessage(R.string.dialog_message)
            .setPositiveButton(R.string.dialog_positive) { _, _ -> openTtsSettings() }
            .setNegativeButton(R.string.dialog_negative, null)
            .setCancelable(true)
            .show()
    }

    /** Tizimning "Nutqni sintez qilish" (Text-to-Speech) sozlamalar
     *  sahifasini ochadi - foydalanuvchi shu yerdan standart mexanizmni
     *  qo'lda tanlaydi (dasturiy yo'l bilan tanlab bo'lmaydi). */
    private fun openTtsSettings() {
        try {
            startActivity(
                Intent(Settings.ACTION_TTS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.toast_no_tts_settings, Toast.LENGTH_LONG).show()
        }
    }

    private fun checkRhvoiceInstalled() {
        val installed = try {
            packageManager.getPackageInfo(RHVOICE_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }

        if (installed) {
            Toast.makeText(this, "RHVoice o'rnatilgan ✓", Toast.LENGTH_SHORT).show()
        } else {
            AlertDialog.Builder(this)
                .setTitle(R.string.rhvoice_missing_title)
                .setMessage(R.string.rhvoice_missing_message)
                .setPositiveButton(R.string.rhvoice_missing_ok, null)
                .show()
        }
    }

    companion object {
        private const val RHVOICE_PACKAGE = "com.github.olga_yakovleva.rhvoice.android"
    }
}
