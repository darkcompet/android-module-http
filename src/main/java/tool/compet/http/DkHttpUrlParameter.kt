/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */
package tool.compet.http

/**
 * Use this to replace an parameter in the url that specified in RequestMethod (GET, POST...).
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class DkHttpUrlParameter(
	/**
	 * @return name which matches with some parameter on the url path.
	 */
	val value: String
)
