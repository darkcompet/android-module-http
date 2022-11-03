/*
 * Copyright (c) 2017-2020 DarkCompet. All rights reserved.
 */
package tool.compet.http

object DkHttpConst {
	// Http request methods
	const val GET = "GET"
	const val POST = "POST"
	const val HEAD = "HEAD"
	const val OPTIONS = "OPTIONS"
	const val PUT = "PUT"
	const val DELETE = "DELETE"
	const val TRACE = "TRACE"

	// Authentication method
	const val AUTHORIZATION = "Authorization"
	const val BASIC_AUTH = "Basic"

	// Content Format
	const val CONTENT_TYPE = "Content-Type"
	const val ACCEPT = "Accept"
	const val APPLICATION_JSON = "application/json"
	const val X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded"
}
