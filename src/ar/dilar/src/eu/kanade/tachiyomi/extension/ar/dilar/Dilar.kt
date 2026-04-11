package eu.kanade.tachiyomi.extension.ar.dilar

import android.content.SharedPreferences
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceScreen
import okhttp3.Headers
import okhttp3.Request
import okhttp3.Response
import eu.kanade.tachiyomi.multisrc.gmanga.Gmanga
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import keiyoushi.utils.getPreferencesLazy

private const val BASE_URL_PREF = "BASE_URL"
private const val DEFAULT_URL = "https://dilar.tube"

class Dilar :
    Gmanga(
        "Dilar",
        DEFAULT_URL,
        "ar",
    ),
    ConfigurableSource {

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        val editTextPref = EditTextPreference(screen.context).apply {
            key = BASE_URL_PREF
            title = "Base URL"
            summary = "غير رابط الموقع من هنا"
            setDefaultValue(DEFAULT_URL)

            setOnPreferenceChangeListener { _, _ ->
                Toast.makeText(screen.context, "أعد تشغيل Mihon لتطبيق التغيير", Toast.LENGTH_LONG).show()
                true
            }
        }
        screen.addPreference(editTextPref)
    }

    private fun getBaseUrl(): String {
        return preferences.getString(BASE_URL_PREF, DEFAULT_URL)!!
    }

    override val baseUrl: String
        get() = getBaseUrl()

    override val cdnUrl: String
        get() = baseUrl

    override fun headersBuilder(): Headers.Builder {
        return super.headersBuilder()
            .add("Referer", baseUrl)
            .add("User-Agent", "Mozilla/5.0")
    }

    override fun chaptersRequest(manga: SManga): Request {
        val mangaId = manga.url.substringAfterLast("/")
        return GET("$baseUrl/api/mangas/$mangaId/releases", headers)
    }

    override fun chaptersParse(response: Response): List<SChapter> {
        val releases = response.parseAs<ChapterListDto>().releases
            .filterNot { it.isMonetized }

        return releases.map { it.toSChapter() }
    }

    private val preferences: SharedPreferences by getPreferencesLazy()
    }
