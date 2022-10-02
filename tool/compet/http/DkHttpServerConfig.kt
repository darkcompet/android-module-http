/*
 * Copyright (c) 2017-2020 DarkCompet. All rights reserved.
 */
package tool.compet.http

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep // Tell R8 don't shrink (remove) this
class DkHttpServerConfig {
	@Expose
	@SerializedName("baseUrl")
	var baseUrl: String? = null

	@Expose
	@SerializedName("basicAuthUsername")
	var basicAuthUsername: String? = null

	@Expose
	@SerializedName("basicAuthPassword")
	var basicAuthPassword: String? = null

	@Expose
	@SerializedName("connectTimeoutMillis")
	var connectTimeoutMillis = -1

	@Expose
	@SerializedName("readTimeoutMillis")
	var readTimeoutMillis = -1
}
