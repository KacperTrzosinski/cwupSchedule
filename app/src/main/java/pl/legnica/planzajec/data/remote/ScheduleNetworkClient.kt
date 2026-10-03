package pl.legnica.planzajec.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleNetworkClient @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val iso88592 = Charset.forName("ISO-8859-2")
    private val baseUrl = "http://www.plan.pwsz.legnica.edu.pl"

    suspend fun fetchDepartmentsHtml(): String = fetchWithRetry("$baseUrl/index.html")

    suspend fun fetchCoursesHtml(departmentId: Int): String =
        fetchWithRetry("$baseUrl/schedule_view.php?site=show_kierunek.php&id=$departmentId")

    suspend fun fetchGroupScheduleHtml(groupCode: String, weekDate: String? = null): String {
        val url = "$baseUrl/checkSpecjalnosc.php?specjalnosc=$groupCode"
        return if (weekDate.isNullOrBlank()) {
            fetchWithRetry(url)
        } else {
            postWithRetry(url, mapOf("dzien" to weekDate))
        }
    }

    suspend fun fetchTeachersHtml(): String =
        fetchWithRetry("$baseUrl/schedule_view.php?site=show_nauczyciel.php&id=11")

    suspend fun fetchRoomsHtml(): String =
        fetchWithRetry("$baseUrl/schedule_view.php?site=show_sala.php&id=11")

    suspend fun fetchTeacherScheduleHtml(teacherId: String, departmentId: Int): String =
        fetchWithRetry("$baseUrl/checkNauczycielAll.php?pracownik=$teacherId&wydzial=$departmentId")

    suspend fun fetchRoomScheduleHtml(roomId: String): String =
        fetchWithRetry("$baseUrl/checkSala.php?sala=$roomId")

    private suspend fun fetchWithRetry(url: String, maxRetries: Int = 3): String = withContext(Dispatchers.IO) {
        var lastException: Exception? = null
        var currentDelay = 1000L

        repeat(maxRetries) { attempt ->
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (CwupSchedule Android App)")
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected HTTP status code: ${response.code}")
                    }
                    val bytes = response.body?.bytes() ?: ByteArray(0)
                    return@withContext String(bytes, iso88592)
                }
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    delay(currentDelay)
                    currentDelay *= 2
                }
            }
        }
        throw lastException ?: IOException("Failed to fetch $url after $maxRetries retries")
    }

    private suspend fun postWithRetry(url: String, params: Map<String, String>, maxRetries: Int = 3): String = withContext(Dispatchers.IO) {
        var lastException: Exception? = null
        var currentDelay = 1000L

        repeat(maxRetries) { attempt ->
            try {
                val formBuilder = FormBody.Builder(iso88592)
                for ((key, value) in params) {
                    formBuilder.add(key, value)
                }

                val request = Request.Builder()
                    .url(url)
                    .post(formBuilder.build())
                    .header("User-Agent", "Mozilla/5.0 (CwupSchedule Android App)")
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected HTTP status code: ${response.code}")
                    }
                    val bytes = response.body?.bytes() ?: ByteArray(0)
                    return@withContext String(bytes, iso88592)
                }
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    delay(currentDelay)
                    currentDelay *= 2
                }
            }
        }
        throw lastException ?: IOException("Failed to post to $url after $maxRetries retries")
    }
}
