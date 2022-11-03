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
	private var status = -1

	// Http message
	private var message: String? = null

	// Body content from server
	private var body: TheHttpResponseBody? = null

	@Throws(IOException::class)
	fun status(): Int {
		return if (status != -1) status else connection.responseCode.also { status = it }
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
	 * Note: it does NOT check body status.
	 */
	@get:Throws(IOException::class)
	val succeed: Boolean
		get() = status() == HttpURLConnection.HTTP_OK

	/**
	 * Check http request has failed or not.
	 * Note: it does NOT check body status.
	 */
	@get:Throws(IOException::class)
	val failed: Boolean
		get() = status() != HttpURLConnection.HTTP_OK
}
