/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */
package tool.compet.http

import tool.compet.core.DkByteArrayList
import tool.compet.core.DkConst
import tool.compet.core.DkLogs
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection

/**
 * Actual content which be sent from server, we call it as body.
 */
class TheHttpResponseBody(private val response: TheHttpResponse) {
	private val connection: HttpURLConnection?

	init {
		connection = response.connection
	}

	/**
	 * Decode body to array of byte.
	 * This method auto disconnect remote server after called even error occured.
	 *
	 * Note that, call this when http request has succeed since this method
	 * read response from input stream of connection.
	 *
	 * @return Body as byte[] for succeed/failed response.
	 */
	@Throws(IOException::class)
	fun readAsBytes(): ByteArray? {
		val connection = this.connection!!

		return stream2bytes(if (response.succeed) connection.inputStream else connection.errorStream)
	}

	/**
	 * Decode body to string with default charset utf-8.
	 * This method auto disconnect remote server after called even error occured.
	 *
	 * Note that, call this when http request has succeed since this method
	 * read response from input stream of connection.
	 *
	 * @return Body as string for succeed/failed response.
	 */
	@Throws(IOException::class)
	fun readAsString(): String? {
		val connection = this.connection!!

		return stream2string(if (response.succeed) connection.inputStream else connection.errorStream)
	}

	/**
	 * Obtain input stream of body.
	 * Caller must call `body.close()` to release resource, disconnect remote after manual decoded.
	 *
	 * @return Raw input stream for succeed/failed response.
	 */
	@Throws(IOException::class)
	fun readAsStream(): InputStream {
		val connection = this.connection!!

		return if (response.succeed) connection.inputStream else connection.errorStream
	}

	/**
	 * Call this to disconnect server.
	 */
	fun close() {
		connection!!.disconnect()
	}

	private fun stream2bytes(inputStream: InputStream): ByteArray? {
		return try {
			val capacity = 1 shl 12
			val byteList = DkByteArrayList(capacity)
			val buffer = ByteArray(capacity)
			var readCount: Int

			while (inputStream.read(buffer).also { readCount = it } != -1) {
				byteList.addRange(buffer, 0, readCount)
			}

			byteList.toArray()
		}
		catch (e: Exception) {
			DkLogs.error(this, e)
			null
		}
		finally {
			connection!!.disconnect()
		}
	}

	private fun stream2string(inputStream: InputStream): String? {
		val builder = StringBuilder(1 shl 12)
		var line: String?

		try {
			BufferedReader(InputStreamReader(inputStream)).use { br ->
				while (br.readLine().also { line = it } != null) {
					builder.append(line).append(DkConst.LS)
				}
			}
		}
		catch (e: Exception) {
			DkLogs.error(TheHttpResponseBody::class.java, e)
			return null
		}
		finally {
			connection!!.disconnect()
		}

		return builder.toString()
	}
}
