package github.leavesc.monitorsamples

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import github.leavesc.monitor.Monitor
import github.leavesc.monitor.MonitorInterceptor
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

/**
 * 作者：leavesC
 * 时间：2019/11/7 17:01
 * 描述：
 */
class MainActivity : AppCompatActivity() {

    private val okHttpClient by lazy {
        return@lazy OkHttpClient.Builder()
                .addInterceptor(MonitorInterceptor(applicationContext))
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .addInterceptor(FilterInterceptor())
                .build()
    }

    private val clickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.btnDoHttp -> {
                doHttpActivity()
            }
            R.id.btnDoHttp2 -> {
                doHttpActivity2()
            }
            R.id.btnLaunchMonitor -> {
                startActivity(Monitor.getLaunchIntent(this@MainActivity))
            }
            R.id.btnClearCache -> {
                Thread(Runnable { Monitor.clearCache() }).start()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnDoHttp.setOnClickListener(clickListener)
        btnDoHttp2.setOnClickListener(clickListener)
        btnLaunchMonitor.setOnClickListener(clickListener)
        btnClearCache.setOnClickListener(clickListener)
        Log.e("TAG", okHttpClient.toString())
        //参数用于监听最新指定条数的数据变化，如果不传递参数则会监听所有的数据变化
        Monitor.queryAllRecord(10).observe(this, Observer { httpInformationList ->
            tv_log.text = null
            if (httpInformationList != null) {
                for (httpInformation in httpInformationList) {
                    tv_log.append(httpInformation.toString())
                    tv_log.append("\n\n")
                    tv_log.append("*************************************")
                    tv_log.append("\n\n")
                }
            }
        })
    }

    private class FilterInterceptor : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val originalRequest = chain.request()
            val httpBuilder = originalRequest.url.newBuilder()
            httpBuilder.addEncodedQueryParameter("key", "fb0a1b0d89f3b93adca639f0a29dbf23")
            val requestBuilder = originalRequest.newBuilder()
                    .url(httpBuilder.build())
            return chain.proceed(requestBuilder.build())
        }

    }

    private fun doHttpActivity() {
        val api = SampleApiService.getInstance_1(okHttpClient)
        val cb = object : Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {

            }
        }
        api.get().enqueue(cb)
        api.post(SampleApiService.Data("posted")).enqueue(cb)
        api.patch(SampleApiService.Data("patched")).enqueue(cb)
        api.put(SampleApiService.Data("put")).enqueue(cb)
        api.delete().enqueue(cb)
        api.status(201).enqueue(cb)
        api.status(401).enqueue(cb)
        api.status(500).enqueue(cb)
        api.delay(9).enqueue(cb)
        api.delay(15).enqueue(cb)
        api.redirectTo("https://http2.akamai.com").enqueue(cb)
        api.redirect(3).enqueue(cb)
        api.redirectRelative(2).enqueue(cb)
        api.redirectAbsolute(4).enqueue(cb)
        api.stream(500).enqueue(cb)
        api.streamBytes(2048).enqueue(cb)
        api.image("image/png").enqueue(cb)
        api.gzip().enqueue(cb)
        api.xml().enqueue(cb)
        api.utf8().enqueue(cb)
        api.deflate().enqueue(cb)
        api.cookieSet("v").enqueue(cb)
        api.basicAuth("me", "pass").enqueue(cb)
        api.drip(512, 5, 1, 200).enqueue(cb)
        api.deny().enqueue(cb)
        api.cache("Mon").enqueue(cb)
        api.cache(30).enqueue(cb)
    }

    private fun doHttpActivity2() {
        val api = SampleApiService.getInstance_2(okHttpClient)
        val cb = object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                t.printStackTrace()
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {

            }

        }
        api.getProvince().enqueue(cb)
        api.getCity("440000").enqueue(cb)
        api.getCounty("440100").enqueue(cb)
    }

}