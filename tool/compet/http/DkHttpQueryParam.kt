/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */
package tool.compet.http

/**
 * Use this to form url with dynamic params.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class DkHttpQueryParam(
	/**
	 * @return name of parameter in query
	 */
	val value: String
)
