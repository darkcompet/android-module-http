/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */
package tool.compet.http

import androidx.collection.ArrayMap
import androidx.collection.SimpleArrayMap
import tool.compet.core.DkStrings
import tool.compet.core.DkUtils
import java.lang.reflect.Method

class OwnServiceMethod internal constructor(method: Method) {
	/**
	 * Static fields (NOT be re-assigned anymore).
	 */
	// Request method: GET, POST...
	private var originRequestMethod: String? = null

	// Header key-value pairs
	private val originHeaders: SimpleArrayMap<String, String>

	// This contains `alias` (for eg,. user_id, app_name) for replacement
	private var originRelativeUrl: String? = null

	/**
	 * Dynamic fields (be re-assigned when re-build).
	 */
	// Header key-value pairs
	private val _headers: SimpleArrayMap<String, String>

	// Full request url
	private var _link: String? = null

	// Body for post request
	private var _body: ByteArray? = null

	// Relative url which contains query string if provided
	private var _queriableRelativeUrl: String? = null

	// region Parsed result
	fun requestMethod(): String? {
		return originRequestMethod
	}

	fun body(): ByteArray? {
		return _body
	}

	fun headers(): SimpleArrayMap<String, String> {
		_headers.putAll(originHeaders)
		return _headers
	}

	fun link(): String? {
		return _link
	}
	// endregion Parsed result
	// region Origin parsing
	/**
	 * Origin parse info from given method.
	 * Note that, it is parsed ONLY one time when this service method is initialized.
	 */
	private fun originParseOnMethod(method: Method) {
		val methodAnnotations = method.declaredAnnotations
		if (methodAnnotations.isEmpty()) {
			DkUtils.complainAt(this, "Must annotate each method with One of @DkGet, @DkPost...")
		}
		for (annotation in methodAnnotations) {
			when (annotation) {
				is DkHttpHeaderEntry -> {
					originParseHeaderOnMethod(annotation)
				}
				is DkHttpGetRequest -> {
					originParseGetRequestOnMethod(annotation)
				}
				is DkHttpPostRequest -> {
					originParsePostRequestOnMethod(annotation)
				}
			}
		}
		if (originRequestMethod == null) {
			DkUtils.complainAt(this, "Missing request method annotation on the method: $method")
		}
	}

	/**
	 * Origin parse header info.
	 */
	private fun originParseHeaderOnMethod(headerInfo: DkHttpHeaderEntry) {
		originHeaders.put(headerInfo.key, headerInfo.value)
	}

	/**
	 * Origin parse info with GET request method.
	 */
	private fun originParseGetRequestOnMethod(getRequest: DkHttpGetRequest) {
		if (originRequestMethod != null) {
			DkUtils.complainAt(this, "Can specify only one request method")
		}
		originRequestMethod = DkHttpConst.GET
		originRelativeUrl = DkStrings.trimMore(getRequest.value, '/')
		when (getRequest.contentType) {
			DkHttpConst.APPLICATION_JSON -> {
				originHeaders.put(DkHttpConst.CONTENT_TYPE, DkHttpConst.APPLICATION_JSON)
			}
			DkHttpConst.X_WWW_FORM_URLENCODED -> {
				originHeaders.put(DkHttpConst.CONTENT_TYPE, DkHttpConst.X_WWW_FORM_URLENCODED)
			}
		}
	}

	/**
	 * Parse info with POST request method.
	 */
	private fun originParsePostRequestOnMethod(postRequest: DkHttpPostRequest) {
		if (originRequestMethod != null) {
			DkUtils.complainAt(this, "Can specify only one request method")
		}
		originRequestMethod = DkHttpConst.POST
		originRelativeUrl = DkStrings.trimMore(postRequest.value, '/')
		when (postRequest.contentType) {
			DkHttpConst.APPLICATION_JSON -> {
				originHeaders.put(DkHttpConst.CONTENT_TYPE, DkHttpConst.APPLICATION_JSON)
			}
			DkHttpConst.X_WWW_FORM_URLENCODED -> {
				originHeaders.put(DkHttpConst.CONTENT_TYPE, DkHttpConst.X_WWW_FORM_URLENCODED)
			}
		}
	}
	// endregion Origin parsing
	/**
	 * This is called every time when the method is invoked
	 * since this must rebuild with new data of parameters.
	 * We should consider it as dynamic build method.
	 */
	fun build(baseUrl: String?, method: Method, methodParams: Array<Any>) {
		// Reset dynamic fields before re-build
		_headers.clear()
		_link = baseUrl
		_body = null
		_queriableRelativeUrl = originRelativeUrl

//		tmp_postFormData = null;
//		tmp_relativeUrl = null;

		// Parse method parameter annotations
		dynamicParseOnParams(method, methodParams)
		_link += _queriableRelativeUrl

//		if (tmp_postFormData != null) {
//			try {
//				dynamic_body = URLEncoder.encode(tmp_postFormData.toString(), "UTF-8").getBytes(Charset.forName("UTF-8"));
//			}
//			catch (Exception e) {
//				DkLogs.error(this, e);
//			}
//		}
	}

	// region Dynamic parsing (re-build)
	// Dynamic parsing on rebuild
	private fun dynamicParseOnParams(method: Method, methodParams: Array<Any>) {
		val paramAnnotations = method.parameterAnnotations
		val query = StringBuilder()
		for (i in paramAnnotations.indices.reversed()) {
			for (annotation in paramAnnotations[i]) {
				when (annotation) {
					is DkHttpUrlParameter -> {
						parseUrlReplacementOnParams(annotation, methodParams[i])
					}
					is DkHttpHeaderEntry -> {
						parseHeaderEntryOnParams(annotation, methodParams[i])
					}
					is DkHttpQueryParam -> {
						parseQueryOnParams(query, annotation, methodParams[i])
					}
					is DkHttpBody -> {
						parseBodyOnParams(annotation, methodParams[i])
					}
				}
			}
		}

		// Build full relative url (relative path + query string)
		if (query.isNotEmpty()) {
			_queriableRelativeUrl += "?$query"
		}
	}

	// Dynamic parsing on rebuild
	private fun parseUrlReplacementOnParams(urlReplacement: DkHttpUrlParameter, paramValue: Any) {
		// url: app/{name}
		// alias: name
		// replacement: gpscompass
		// -> final url: app/gpscompass
		val alias: String = urlReplacement.value
		val replacement = if (paramValue is String) paramValue else paramValue.toString()

		// Maybe alias placed in static relative url,
		// so we need replace them with value of method's parameter
		while (true) {
			val target = "{$alias}"
			val index = _queriableRelativeUrl!!.indexOf(target)
			if (index < 0) {
				break
			}
			_queriableRelativeUrl = _queriableRelativeUrl!!.replace(target, replacement)
		}
	}

	// Dynamic parsing on rebuild
	private fun parseHeaderEntryOnParams(headerEntry: DkHttpHeaderEntry, paramValue: Any) {
		val key: String = headerEntry.key
		val value = paramValue.toString()
		if (headerEntry.value.isNotEmpty()) {
			DkUtils.complainAt(this, "Pls don't use `value()` in `DkHttpHeaderEntry` for params")
		}
		_headers.put(key, value)
	}

	// Dynamic parsing on rebuild
	private fun parseQueryOnParams(
		query: StringBuilder,
		queryInfo: DkHttpQueryParam,
		paramValue: Any
	) {
		if (query.isNotEmpty()) {
			query.append('&')
		}
		query.append(queryInfo.value).append("=").append(paramValue)
	}

	// Dynamic parsing on rebuild
	private fun parseBodyOnParams(bodyInfo: DkHttpBody, paramValue: Any) {
		if (paramValue !is ByteArray) {
			throw RuntimeException("Body must be in `byte[]`, bodyInfo: $bodyInfo")
		}
		_body = paramValue
		//		body = ((String) paramValue).getBytes();
//		else if (paramValue.getClass().isPrimitive()) {
//			body = String.valueOf(paramValue).getBytes();
//		}
//		else if (paramValue instanceof Bitmap) {
//			body = DkBitmaps.toByteArray((Bitmap) paramValue);
//		}
//		else {
//			body = DkJsonConverter.getIns().obj2json(paramValue).getBytes();
//		}
	} // endregion Dynamic parsing (re-build)

	init {
		originHeaders = ArrayMap()
		_headers = ArrayMap()
		originParseOnMethod(method)
	}
}
