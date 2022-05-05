/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */
package tool.compet.http

/**
 * Value assigned to this annotation will be added to header.
 * If caller use this annotation in a parameter of the method,
 * then value will be content of parameter, and content of `value()` will be ignored.
 */
@Target(
	AnnotationTarget.FUNCTION,
	AnnotationTarget.PROPERTY_GETTER,
	AnnotationTarget.PROPERTY_SETTER,
	AnnotationTarget.VALUE_PARAMETER
)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class DkHttpHeaderEntry(
	/**
	 * @return key of property in header.
	 */
	val key: String,
	/**
	 * @return value of property in header, unused for case of parameter.
	 */
	val value: String = ""
)
