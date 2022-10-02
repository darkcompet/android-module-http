/*
 * Copyright (c) 2017-2020 DarkCompet. All rights reserved.
 */
package tool.compet.http

import androidx.collection.ArrayMap
import androidx.collection.SimpleArrayMap
import tool.compet.core.DkLogs
import tool.compet.core.DkUtils
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
	protected val headers: SimpleArrayMap<String, String> = ArrayMap()
	protected var requestMethod: String? = DkHttpConst.GET
	protected var link: String? = null
	protected var body: ByteArray? = null
	protected var connectTimeout = 15000
	protected var readTimeout = 30000

	constructor()
	constructor(url: String?) {
		link = url
	}

	fun setUrl(url: String?): DkHttpClient {
		link = url
		return this
	}

	fun setRequestMethod(requestMethod: String?): DkHttpClient {
		this.requestMethod = requestMethod
		return this
	}

	fun addToHeader(key: String, value: String): DkHttpClient {
		headers.put(key, value)
		return this
	}

	fun addAllToHeader(map: SimpleArrayMap<String, String>?): DkHttpClient {
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

	/**
	 * For GET method, we just connect to remote server without decode stream.
	 * For POST method, we write to remote server and wait for client decode stream.
	 */
	@Throws(Exception::class)
	fun execute(): TheHttpResponse {
		if (link == null) {
			throw RuntimeException("Must provide url")
		}
		if (BuildConfig.DEBUG) {
			DkLogs.info(
				this,
				"Execute Http %s-request with link: %s, thread: %s, headers: %s",
				requestMethod,
				link,
				Thread.currentThread().name,
				headers.toString()
			)
		}
		val url = URL(link)
		val connection = url.openConnection() as HttpURLConnection
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
		if (DkHttpConst.Companion.GET == requestMethod) {
			doGet(connection)
		} else if (DkHttpConst.Companion.POST == requestMethod) {
			doPost(connection)
		} else {
			DkUtils.complainAt(this, "Invalid request method: $requestMethod")
		}

		// We return response without decoding result
		// since we don't have converter to do that
		return httpResponse
	}

	@Throws(Exception::class)
	protected fun doGet(connection: HttpURLConnection?) {
		if (BuildConfig.DEBUG) {
			DkLogs.info(this, "Perform GET request")
		}
	}

	@Throws(Exception::class)
	protected fun doPost(connection: HttpURLConnection) {
		if (BuildConfig.DEBUG) { DkLogs.info(this, "Perform POST request. Body length: " + if (body == null) 0 else body!!.size) }
		if (body != null) {
			connection.doOutput = true
			// Write full post data to body
			val bos = BufferedOutputStream(connection.outputStream)
			bos.write(body)
			bos.close()
		}
	}
}
