package tool.compet.http

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
open class DkApiResponse {
	@Expose
	@SerializedName("status")
	var status = 0

	@Expose
	@SerializedName("code")
	var code: String? = null

	@Expose
	@SerializedName("message")
	var message: String? = null

	// Utility funs for check our result status
	val succeed get() = status == 200
	val failed get() = status != 200
}
