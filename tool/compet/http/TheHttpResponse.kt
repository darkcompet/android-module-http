/*
 * Copyright (c) 2017-2020 DarkCompet. All rights reserved.
 */
package tool.compet.http

import java.io.IOException
import java.net.HttpURLConnection

/**
 * Response of a request. It extracts some basic information from response
 * as code, message, body...
 */
class TheHttpResponse(val connection: HttpURLConnection) {
	// Http status code: HttpURLConnection.*
	protected var code = -1

	// Http message
	protected var message: String? = null

	// Body content from server
	protected var body: TheHttpResponseBody? = null

	@Throws(IOException::class)
	fun code(): Int {
		return if (code != -1) code else connection.responseCode.also { code = it }
	}

	@Throws(IOException::class)
	fun message(): String {
		return if (message != null) message!! else connection.responseMessage.also { message = it }
	}

	fun body(): TheHttpResponseBody {
		return if (body != null) body!! else TheHttpResponseBody(this).also { body = it }
	}

	/**
	 * Check http request has succeed or not.
	 */
	@get:Throws(IOException::class)
	val isSucceed: Boolean
		get() = code() == HttpURLConnection.HTTP_OK

	/**
	 * Check http request has failed or not.
	 */
	@get:Throws(IOException::class)
	val isFailed: Boolean
		get() = code() != HttpURLConnection.HTTP_OK
}
