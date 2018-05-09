package employee.summon.asano

import android.app.IntentService
import android.app.Notification
import android.content.Intent
import android.content.Context
import android.app.PendingIntent
import android.app.Service
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.google.gson.Gson
import com.tylerjroach.eventsource.EventSource
import com.tylerjroach.eventsource.EventSourceHandler
import com.tylerjroach.eventsource.MessageEvent
import employee.summon.asano.activity.MainActivity
import employee.summon.asano.activity.RequestReceiver
import employee.summon.asano.model.AccessToken
import employee.summon.asano.model.SummonRequestMessage


private const val ACTION_LISTEN_REQUEST = "employee.summon.asano.action.LISTEN_REQUEST"

class RequestListenerService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private var accessToken : AccessToken? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        accessToken = intent?.getParcelableExtra(App.ACCESS_TOKEN)
        eventSource = EventSource.Builder(getString(R.string.base_url) + App.REQUEST_URL_SUFFIX)
                .eventHandler(requestHandler)
                .build()
        eventSource!!.connect()
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = Notification.Builder(this)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.drawable.person_icon)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.ticker_text))
                .build()

        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        if (eventSource != null) {
            eventSource!!.close()
        }
        requestHandler = null
        super.onDestroy()
    }

    private var requestHandler: RequestHandler? = RequestHandler()

    private var eventSource: EventSource? = null

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
                val intent = Intent(App.REQUEST_RECEIVED)
                intent.setClass(this@RequestListenerService, RequestReceiver::class.java)
                intent.putExtra(App.REQUEST, request.data)
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
        const val ONGOING_NOTIFICATION_ID = 1
        /**
         * Starts this service to perform action Foo with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        @JvmStatic
        fun startActionListenRequest(context: Context, accessToken: AccessToken) {
            val intent = Intent(context, RequestListenerService::class.java).apply {
                action = ACTION_LISTEN_REQUEST
            }
            intent.putExtra(App.ACCESS_TOKEN, accessToken)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
