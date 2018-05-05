package employee.summon.asano

import android.app.Application
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.tylerjroach.eventsource.EventSource
import com.tylerjroach.eventsource.EventSourceHandler
import com.tylerjroach.eventsource.MessageEvent
import employee.summon.asano.activity.RequestReceiver

import employee.summon.asano.model.AccessToken
import employee.summon.asano.model.SummonRequestMessage
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class App : Application() {
    var accessToken: AccessToken? = null

    var retrofit: Retrofit? = null
        private set

    override fun onCreate() {
        super.onCreate()

        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                    .baseUrl(getString(R.string.base_url))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        }
    }
    private var requestHandler: RequestHandler? = RequestHandler()

    private var eventSource: EventSource? = null

    fun startEventSource() {
        eventSource = EventSource.Builder(getString(R.string.base_url) + REQUEST_URL_SUFFIX)
                .eventHandler(requestHandler)
                .build()
        eventSource!!.connect()
    }

    fun stopEventSource() {
        if (eventSource != null) {
            eventSource!!.close()
        }
        requestHandler = null
    }

    val services : MutableMap<String, Any> = HashMap()

    inline fun <reified T> getService(): T {
        if (services.contains(T::class.java.simpleName)) {
            return services[T::class.java.simpleName] as T
        }
        val service = retrofit!!.create<T>(T::class.java)
        services[T::class.java.simpleName] = service as Any
        return service
    }

    inner class RequestHandler : EventSourceHandler {
        override fun onConnect() {
            Log.v("SSE connected", "True")
        }

        override fun onComment(comment: String?) {
            Log.v("SSE Comment", comment)
        }

        override fun onMessage(event: String?, message: MessageEvent) {
            Log.v("SSE Message", event)
            Log.v("SSE Message: ", message.data)
            val request = Gson().fromJson<SummonRequestMessage>(message.data, SummonRequestMessage::class.java)
            if (request.data.targetId == accessToken?.userId) {
                val intent = Intent(REQUEST_RECEIVED)
                intent.setClass(this@App, RequestReceiver::class.java)
                intent.putExtra(REQUEST, request.data)
                sendBroadcast(intent)
            }
        }

        override fun onClosed(willReconnect: Boolean) {
            Log.v("SSE Closed", "reconnect? $willReconnect")
        }

        override fun onError(t: Throwable?) {
            Log.e("SSE Error", "Failed", t)
        }
    }

    companion object {
        const val REQUEST_URL_SUFFIX = "summonrequests/change-stream/"
        const val REQUEST_RECEIVED = "employee.summon.asano.REQUEST_RECEIVED"
        const val REQUEST = "request_extra"
    }
}
