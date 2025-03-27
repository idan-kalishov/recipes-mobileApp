import android.graphics.Bitmap
import android.util.Base64
import okhttp3.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

object ImgurUploader {
    private const val CLIENT_ID = "89e0730827f929b"

    fun uploadImage(bitmap: Bitmap, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        val url = "https://api.imgur.com/3/image"
        val client = OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        // Convert Bitmap to Base64
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val encodedImage = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)

        // Build the request
        val body = FormBody.Builder()
            .add("image", encodedImage)
            .build()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Client-ID $CLIENT_ID")
            .post(body)
            .build()

        // Execute the request
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val json = JSONObject(response.body!!.string())
                    val imageUrl = json.getJSONObject("data").getString("link")
                    onSuccess(imageUrl)
                } else {
                    onFailure("Upload failed: ${response.message}")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                onFailure("Upload error: ${e.message}")
            }
        })
    }
}
