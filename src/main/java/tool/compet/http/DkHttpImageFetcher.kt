package tool.compet.http

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object DkHttpImageFetcher {
	fun fetch(url: String, onMainThreadResultCallback: (Bitmap) -> Unit) {
		GlobalScope.launch(Dispatchers.IO) {
			DkHttpClient().let { httpClient ->
				httpClient.setUrl(url)
				val bitmap = httpClient.execute().body().bitmap()!!

				onMainThreadResultCallback(bitmap)
			}
		}
	}
}
