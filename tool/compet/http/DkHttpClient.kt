/*
 * Copyright (c) 2017-2020 DarkCompet. All rights reserved.
 */
package tool.compet.http

import androidx.collection.ArrayMap
import androidx.collection.SimpleArrayMap
import tool.compet.core.BuildConfig
import tool.compet.core.DkLogs
import tool.compet.core.DkUtils
import tool.compet.json.DkJsons
import java.io.BufferedOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Performs request (GET, POST...) to server. You can specify response type or it will be
 * acted as String, so be converted from Json string to Object.
 * Note that you must do it in IO thread. Here is an example of usage;
 * `<pre>
 * DkHttpClient client = new DkHttpClient();
 * TheHttpResponse httpResponse = client.execute(url);
 *
 * int code = httpResponse.code;
 * String message = httpResponse.message;
 * String json = httpResponse.body().json();
</pre>` *
 */
class DkHttpClient {
	// By default, we request with content as json
	var contentTypeAsJson = true

	private val headers: SimpleArrayMap<String, String> = ArrayMap()
	private var body: ByteArray? = null
	private var connectTimeout = 15000
	private var readTimeout = 30000

	fun putToHeader(key: String, value: String): DkHttpClient {
		headers.put(key, value)
		return this
	}

	fun putAllToHeader(map: SimpleArrayMap<String, String>?): DkHttpClient {
		headers.putAll(map!!)
		return this
	}

	fun setConnectTimeout(connectTimeout: Int): DkHttpClient {
		this.connectTimeout = connectTimeout
		return this
	}

	fun setReadTimeout(readTimeout: Int): DkHttpClient {
		this.readTimeout = readTimeout
		return this
	}

	fun setBody(body: ByteArray?): DkHttpClient {
		this.body = body
		return this
	}

	fun <T : DkApiResponse> get(url: String, responseClass: Class<T>): T {
		try {
			val response = execute(url, DkHttpConst.GET)
			if (response.failed) {
				if (BuildConfig.DEBUG) {
					DkLogs.warning(
						this,
						"Get failed, status: ${response.status()}, message: ${response.message()}, body: ${
							response.body().readAsString()
						}"
					)
				}
				return responseClass.newInstance().apply {
					this.status = response.status()
					this.message = response.message()
				}
			}

			return DkJsons.toObj(response.body().readAsString(), responseClass)!!
		}
		catch (e: Exception) {
			if (BuildConfig.DEBUG) {
				DkLogs.error(this, "Get exception, error: ${e.message}")
			}
			return responseClass.newInstance().apply {
				this.status = -1
				this.message = e.message ?: "DkError"
			}
		}
	}

	fun <T> getForType(url: String, responseClass: Class<T>): T? {
		try {
			val response = execute(url, DkHttpConst.GET)
			if (response.failed) {
				if (BuildConfig.DEBUG) {
					DkLogs.warning(
						this,
						"GetForType failed, status: ${response.status()}, message: ${response.message()}, body: ${
							response.body().readAsString()
						}"
					)
				}
				return null
			}
			return DkJsons.toObj(response.body().readAsString(), responseClass)
		}
		catch (e: Exception) {
			if (BuildConfig.DEBUG) {
				DkLogs.error(this, "GetForType exception, error: ${e.message}")
			}
			return null
		}
	}

	fun <T : DkApiResponse> post(url: String, body: ByteArray?, responseClass: Class<T>): T {
		try {
			this.body = body

			val response = execute(url, DkHttpConst.POST)
			if (response.failed) {
				if (BuildConfig.DEBUG) {
					DkLogs.warning(
						this,
						"Post failed, status: ${response.status()}, message: ${response.message()}, body: ${
							response.body().readAsString()
						}"
					)
				}
				return responseClass.newInstance().apply {
					this.status = response.status()
					this.message = response.message()
				}
			}

			return DkJsons.toObj(response.body().readAsString(), responseClass)!!
		}
		catch (e: Exception) {
			if (BuildConfig.DEBUG) {
				DkLogs.error(this, "Post exception, error: ${e.message}")
			}
			return responseClass.newInstance().apply {
				this.status = -1
				this.message = e.message ?: "DkError"
			}
		}
	}

	fun <T : DkApiResponse> post(url: String, body: Any, responseClass: Class<T>): T {
		try {
			this.body = DkJsons.toJson(body).toByteArray()

			val response = execute(url, DkHttpConst.POST)
			if (response.failed) {
				if (BuildConfig.DEBUG) {
					DkLogs.warning(
						this,
						"Post failed, status: ${response.status()}, message: ${response.message()}, body: ${
							response.body().readAsString()
						}"
					)
				}
				return responseClass.newInstance().apply {
					this.status = response.status()
					this.message = response.message()
				}
			}

			return DkJsons.toObj(response.body().readAsString(), responseClass)!!
		}
		catch (e: Exception) {
			if (BuildConfig.DEBUG) {
				DkLogs.error(this, "Post exception, error: ${e.message}, stackTrace: ${e.stackTrace.contentDeepToString()}")
			}
			return responseClass.newInstance().apply {
				this.status = -1
				this.message = e.message ?: "DkError"
			}
		}
	}

	fun <T> postForType(url: String, body: ByteArray, responseClass: Class<T>): T? {
		try {
			this.body = body

			val response = execute(url, DkHttpConst.POST)
			if (response.failed) {
				if (BuildConfig.DEBUG) {
					DkLogs.warning(
						this,
						"PostForType failed, status: ${response.status()}, message: ${response.message()}, body: ${
							response.body().readAsString()
						}"
					)
				}
				return null
			}

			return DkJsons.toObj(response.body().readAsString(), responseClass)
		}
		catch (e: Exception) {
			if (BuildConfig.DEBUG) {
				DkLogs.error(this, "PostForType exception, error: ${e.message}")
			}
			return null
		}
	}

	fun <T> postForType(url: String, body: Any, responseClass: Class<T>): T? {
		try {
			this.body = DkJsons.toJson(body).toByteArray()

			val response = execute(url, DkHttpConst.POST)
			if (response.failed) {
				if (BuildConfig.DEBUG) {
					DkLogs.warning(
						this,
						"PostForType failed, status: ${response.status()}, message: ${response.message()}, body: ${
							response.body().readAsString()
						}"
					)
				}
				return null
			}

			return DkJsons.toObj(response.body().readAsString(), responseClass)
		}
		catch (e: Exception) {
			if (BuildConfig.DEBUG) {
				DkLogs.error(this, "PostForType exception, error: ${e.message}")
			}
			return null
		}
	}

	/**
	 * For GET method, we just connect to remote server without decode stream.
	 * For POST method, we write to remote server and wait for client decode stream.
	 */
	@Throws(Exception::class)
	fun execute(url: String, requestMethod: String): TheHttpResponse {
		// Build
		if (this.contentTypeAsJson) {
			putToHeader(DkHttpConst.CONTENT_TYPE, DkHttpConst.APPLICATION_JSON)
		}

		if (BuildConfig.DEBUG) {
			DkLogs.info(
				this,
				"Execute Http %s-request with link: %s, thread: %s, headers: %s",
				requestMethod,
				url,
				Thread.currentThread().name,
				headers.toString()
			)
		}

		val connection = URL(url).openConnection() as HttpURLConnection
		val httpResponse = TheHttpResponse(connection)

		// Apply headers to connection
		// For eg,. Content-Type (json),...
		for (index in headers.size() - 1 downTo 0) {
			connection.setRequestProperty(headers.keyAt(index), headers.valueAt(index))
		}
		connection.connectTimeout = connectTimeout
		connection.readTimeout = readTimeout
		connection.requestMethod = requestMethod
		connection.doInput = true

		// Perform with request method (get, post,...)
		when (requestMethod) {
			DkHttpConst.GET -> doGet(connection)
			DkHttpConst.POST -> doPost(connection)
			else -> DkUtils.complainAt(this, "Invalid request method: $requestMethod")
		}

		// We return response without decoding result
		// since we don't have converter to do that
		return httpResponse
	}

	@Throws(Exception::class)
	private fun doGet(connection: HttpURLConnection?) {
		if (BuildConfig.DEBUG) {
			DkLogs.info(this, "Perform GET request")
		}
	}

	@Throws(Exception::class)
	private fun doPost(connection: HttpURLConnection) {
		if (BuildConfig.DEBUG) {
			DkLogs.info(this, "Perform POST request. Body length: " + if (body == null) 0 else body!!.size)
		}
		if (body != null) {
			connection.doOutput = true
			// Write full post data to body
			val bos = BufferedOutputStream(connection.outputStream)
			bos.write(body)
			bos.close()
		}
	}
}
