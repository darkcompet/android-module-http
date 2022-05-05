/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */
package tool.compet.http

/**
 * Attach this to a method to make with GET request method.
 */
@Target(
	AnnotationTarget.FUNCTION,
	AnnotationTarget.PROPERTY_GETTER,
	AnnotationTarget.PROPERTY_SETTER
)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class DkHttpGetRequest(
	/**
	 * @return relative url of api, for eg,. user/1/profile
	 */
	val value: String,
	/**
	 * @return Get data format, for eg,. application/json
	 */
	val contentType: String = DkHttpConst.Companion.APPLICATION_JSON
)
