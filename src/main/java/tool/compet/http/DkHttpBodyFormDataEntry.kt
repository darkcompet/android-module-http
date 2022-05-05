/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */
package tool.compet.http

/**
 * Use this annotation on params of the method.
 * When request with POST method, we will convert all body entries
 * to format `k1=v1&k2=v2...` and send it as bytes to server after headers.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class DkHttpBodyFormDataEntry(
	/**
	 * @return Name of form-data entry.
	 */
	val value: String
)
