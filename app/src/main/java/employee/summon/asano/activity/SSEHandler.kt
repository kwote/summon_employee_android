package employee.summon.asano.activity

import android.util.Log
import com.tylerjroach.eventsource.EventSourceHandler
import com.tylerjroach.eventsource.MessageEvent

class SSEHandler : EventSourceHandler {
    override fun onConnect() {
       Log.v("SSE connected", "True")
    }

    override fun onComment(comment: String?) {
        Log.v("SSE Comment", comment)
    }

    override fun onMessage(event: String?, message: MessageEvent) {
        Log.v("SSE Message", event)
        Log.v("SSE Message: ", message.lastEventId)
        Log.v("SSE Message: ", message.data)
    }

    override fun onClosed(willReconnect: Boolean) {
        Log.v("SSE Closed", "reconnect? $willReconnect")
    }

    override fun onError(t: Throwable?) {
        Log.e("SSE Error", "Failed", t)
    }

}
