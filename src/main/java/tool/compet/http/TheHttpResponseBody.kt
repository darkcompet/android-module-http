/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */
package tool.compet.http

import android.graphics.Bitmap
import tool.compet.core.*
import tool.compet.graphics.DkBitmaps
import tool.compet.json.DkJsons
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection

/**
 * Actual content which be sent from server, we call it as body.
 */
class TheHttpResponseBody(protected val httpResponse: TheHttpResponse) {
	protected val connection: HttpURLConnection?

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
	fun asBytes(): ByteArray? {
		return stream2bytes(if (httpResponse.isSucceed) connection!!.inputStream else connection!!.errorStream)
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
	fun asString(): String? {
		return stream2string(if (httpResponse.isSucceed) connection!!.inputStream else connection!!.errorStream)
	}

	/**
	 * Consider body as json and Decode it to object which has type of given class.
	 * This method automatically close the stream and disconnect remote server after called.
	 *
	 * Note that, call this when http request has succeed since this method
	 * read response from input stream of connection.
	 *
	 * @return Body as json for succeed/failed response.
	 */
	@Throws(IOException::class)
	fun <R> asJson(responseClass: Class<R>): R? {
		return stream2json(
			if (httpResponse.isSucceed) connection!!.inputStream else connection!!.errorStream,
			responseClass
		)
	}

	/**
	 * Obtain input stream of body.
	 * Caller must call `body.close()` to release resource, disconnect remote after manual decoded.
	 *
	 * @return Raw input stream for succeed/failed response.
	 */
	@Throws(IOException::class)
	fun asByteStream(): InputStream {
		return if (httpResponse.isSucceed) connection!!.inputStream else connection!!.errorStream
	}

	fun bitmap(): Bitmap? {
		return try {
			DkBitmaps.load(connection!!.inputStream)
		}
		catch (e: Exception) {
			DkLogcats.error(DkLogcats::class.java, e)
			null
		}
		finally {
			connection!!.disconnect()
		}
	}

	/**
	 * Call this to disconnect server.
	 */
	fun close() {
		connection!!.disconnect()
	}

	// region Private
	private fun stream2bytes(inputStream: InputStream): ByteArray? {
		return try {
			val byteList = DkByteArrayList()
			val buffer = ByteArray(1 shl 12)
			var readCount: Int
			while (inputStream.read(buffer).also { readCount = it } != -1) {
				byteList.addRange(buffer, 0, readCount)
			}
			if (BuildConfig.DEBUG) {
				DkLogs.info(this, "Got response as bytes, count: %d", byteList.size())
			}
			byteList.toArray()
		} catch (e: Exception) {
			DkLogs.error(this, e)
			null
		} finally {
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

		if (BuildConfig.DEBUG) {
			DkLogs.info(this, "Got response as string: %s", builder.toString())
		}
		return builder.toString()
	}

	private fun <R> stream2json(inputStream: InputStream, responseClass: Class<R>): R? {
		return try {
			val json = DkUtils.stream2string(inputStream)
			if (BuildConfig.DEBUG) {
				DkLogs.info(this, "Got respond body as json: %s", json)
			}
			DkJsons.toObj(json, responseClass)
		} catch (e: Exception) {
			DkLogs.error(TheHttpResponseBody::class.java, e)
			null
		} finally {
			connection!!.disconnect()
		}
	} // endregion Private

	init {
		connection = httpResponse.connection
	}
}
