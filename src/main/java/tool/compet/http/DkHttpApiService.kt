/*
 * Copyright (c) 2017-2020 DarkCompet. All rights reserved.
 */
package tool.compet.http

import android.content.Context
import android.util.Base64
import androidx.collection.ArrayMap
import androidx.collection.SimpleArrayMap
import tool.compet.core.DkConst
import tool.compet.core.DkUtils
import tool.compet.json.DkJsons
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * It is convenience class. It will create new instance of http api requester,
 * called as ServiceApi, so caller can use it to request to server.
 * Usage example:
 * <pre>
 * // Create new instance of UserApi
 * UserApi userApi = DkApiService.newIns()
 * .configWith(App.getContext(), "server/server_coresystem.json")
 * .create(UserApi.class);
 *
 * // Now we can request to server via methods inside userApi
 * ProfileResponse profileResponse = userApi
 * .downloadProfile(accessToken)
 * .map(res -> ResponseValidator.validate(res).response)
 * .scheduleInBackgroundAndObserveOnMainThread()
 * .subscribe();
 * </pre>
 */
class DkHttpApiService {
	var baseUrl: String? = null
		protected set
	var credential: String? = null
		protected set
	protected var connectTimeoutMillis = 15000 // 15s
	protected var readTimeoutMillis = 30000 // 30s
	protected val serviceMethods: SimpleArrayMap<Method, OwnServiceMethod>

	/**
	 * For convenience, this provides setup with json config file.
	 *
	 * @param filename Json file under `asset` folder.
	 */
	fun configWithJson(context: Context, filename: String): DkHttpApiService {
		val json = DkUtils.asset2string(context, filename)
		val serverConfig = DkJsons.toObj(json, DkHttpServerConfig::class.java)!!

		if (serverConfig.baseUrl != null) {
			baseUrl = serverConfig.baseUrl
		}

		if (serverConfig.basicAuthUsername != null || serverConfig.basicAuthPassword != null) {
			val pair = serverConfig.basicAuthUsername + ":" + serverConfig.basicAuthPassword
			val credential = String(Base64.encode(pair.toByteArray(), Base64.NO_WRAP))

			setBasicCredential(credential)
		}

		if (serverConfig.connectTimeoutMillis != 0) {
			connectTimeoutMillis = serverConfig.connectTimeoutMillis
		}

		if (serverConfig.readTimeoutMillis != 0) {
			readTimeoutMillis = serverConfig.readTimeoutMillis
		}

		return this
	}

	fun setBaseUrl(baseUrl: String?): DkHttpApiService {
		this.baseUrl = baseUrl
		return this
	}

	/**
	 * Set basic credential to authenticate with server, called as `basic auth`.
	 *
	 * @param base64Credential Normally, it is base64 of `username:password`;
	 */
	fun setBasicCredential(base64Credential: String): DkHttpApiService {
		credential = DkHttpConst.Companion.BASIC_AUTH + DkConst.SPACE + base64Credential
		return this
	}

	fun setConnectTimeoutMillis(connectTimeoutSecond: Int): DkHttpApiService {
		connectTimeoutMillis = connectTimeoutSecond
		return this
	}

	fun setReadTimeoutMillis(readTimeoutSecond: Int): DkHttpApiService {
		readTimeoutMillis = readTimeoutSecond
		return this
	}

	fun getConnectTimeoutMillis(): Long {
		return connectTimeoutMillis.toLong()
	}

	fun getReadTimeoutMillis(): Long {
		return readTimeoutMillis.toLong()
	}

	fun <S> create(serviceClass: Class<S>): S {
		validateAndShapingConfig()

		// Create service object from given service class,
		// And register handler on the service object to listen
		// each invocation of methods.
		val handler = InvocationHandler { proxy: Any?, method: Method, args: Array<Any> ->
			// Don't handle method which is not in service class
			if (method.declaringClass != serviceClass) {
				return@InvocationHandler method.invoke(proxy, *args)
			}
			executeHttpRequest(method, args)
		}
		return Proxy.newProxyInstance(
			serviceClass.classLoader,
			arrayOf<Class<*>>(serviceClass),
			handler
		) as S
	}

	private fun validateAndShapingConfig() {
		if (baseUrl == null) {
			DkUtils.complainAt(this, "Must specify non-null baseUrl")
		}
		if (!baseUrl!!.endsWith("/")) {
			baseUrl += '/'
		}
	}

	@Throws(Exception::class)
	private fun executeHttpRequest(method: Method, args: Array<Any>): TheHttpResponse? {
		// For each call of method, we need build HTTP request for it
		// To avoid instantiate multiple times, we will try to cache own service method
		var serviceMethod: OwnServiceMethod?
		synchronized(serviceMethods) {
			serviceMethod = serviceMethods[method]

			// Because we cache this service method, so ONLY pass
			// fixed data (no change more) to it at construct time.
			if (serviceMethod == null) {
				serviceMethod = OwnServiceMethod(method)
				serviceMethods.put(method, serviceMethod)
			}
		}

		// Re-build service method since parameter
		// on the method maybe changed each time
		var link: String?
		var requestMethod: String?
		var headers: SimpleArrayMap<String, String>
		var body: ByteArray?

		// Lock this method for re-build process
		synchronized(serviceMethod!!) {

			// Note that this service method is cached, so we need pass
			// every non-fixed materials to it (fixed materials can be ignored at this time)
			serviceMethod!!.build(baseUrl, method, args)
			link = serviceMethod!!.link()
			requestMethod = serviceMethod!!.requestMethod()
			headers = serviceMethod!!.headers()
			body = serviceMethod!!.body()
		}

		// Ok this is time to execute HTTP request
		// with own built service method
		val httpClient = DkHttpClient(link)
			.setReadTimeout(readTimeoutMillis)
			.setConnectTimeout(connectTimeoutMillis)
			.setRequestMethod(requestMethod)
			.setBody(body)

		// When credential is set via this service, just set it
		// But if credential is declared at method service, this set will be overridden.
		if (credential != null) {
			httpClient!!.addToHeader(DkHttpConst.Companion.AUTHORIZATION, credential!!)
		}
		httpClient!!.addAllToHeader(headers)
		return httpClient.execute()
	}

	init {
		serviceMethods = ArrayMap()
	}
}
