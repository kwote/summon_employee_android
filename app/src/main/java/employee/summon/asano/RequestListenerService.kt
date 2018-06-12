package employee.summon.asano

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import android.widget.Toast
import com.squareup.moshi.Moshi
import com.tylerjroach.eventsource.EventSource
import com.tylerjroach.eventsource.EventSourceHandler
import com.tylerjroach.eventsource.MessageEvent
import employee.summon.asano.activity.MainActivity
import employee.summon.asano.activity.PersonActivity
import employee.summon.asano.activity.SummonActivity
import employee.summon.asano.model.AccessToken
import employee.summon.asano.model.RequestStatus
import employee.summon.asano.model.SummonRequestMessage
import employee.summon.asano.rest.PeopleService
import java.net.URLEncoder


class RequestListenerService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private lateinit var accessToken: AccessToken

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                ACTION_LISTEN_REQUEST -> {
                    if (intent.hasExtra(App.ACCESS_TOKEN)) {
                        accessToken = intent.getParcelableExtra(App.ACCESS_TOKEN)
                        val headers = mutableMapOf("Authorization" to accessToken.id)
                        eventSource = EventSource.Builder(
                                getString(R.string.base_url) + REQUEST_URL_SUFFIX +
                                        URLEncoder.encode(String.format(REQUEST_URL_ESC_SUFFIX, accessToken.userId), "UTF-8"))
                                .headers(headers)
                                .eventHandler(requestHandler)
                                .build()
                        eventSource.connect()
                    }
                }
                ACTION_CLOSE_CONNECTION -> {
                    closeConnection()
                    stopSelf()
                }
            }
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    WAKELOCK_TAG)
            wakeLock?.acquire(3600 * 8 * 1000)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun getNotificationBuilder(context: Context, channelId: String, importance: Int): NotificationCompat.Builder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareChannel(context, channelId, importance)
            NotificationCompat.Builder(context, channelId)
        } else {
            NotificationCompat.Builder(context)
        }
    }

    @TargetApi(26)
    private fun prepareChannel(context: Context, id: String, importance: Int) {
        val appName = context.getString(R.string.app_name)
        val description = context.getString(R.string.notifications_channel_description)
        val nm = context.getSystemService(Activity.NOTIFICATION_SERVICE) as NotificationManager

        var nChannel: NotificationChannel? = nm.getNotificationChannel(id)

        if (nChannel == null) {
            nChannel = NotificationChannel(id, appName, importance)
            nChannel.description = description
            nm.createNotificationChannel(nChannel)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val notification = getNotificationBuilder(this,
                "employee.summon.asano.CHANNEL_ID_FOREGROUND",
                NotificationManagerCompat.IMPORTANCE_LOW)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.drawable.person_icon)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.ticker_text))
                .setOngoing(true)
                .build()

        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        closeConnection()
        wakeLock?.release()
        super.onDestroy()
    }

    private fun closeConnection() {
        eventSource.close()
        requestHandler = null
        stopForeground(true)
    }

    private var requestHandler: RequestHandler? = RequestHandler()

    private lateinit var eventSource: EventSource

    private val builder = Moshi.Builder().build()

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
            val adapter = builder.adapter(SummonRequestMessage::class.javaObjectType)
            val request = adapter.fromJson(message.data) ?: return
            if (request.data.targetId == accessToken.userId) {
                if (request.data.enabled && request.data.pending) {
                    val callerId = request.data.callerId
                    val app = this@RequestListenerService.applicationContext as App
                    val service = app.getService<PeopleService>()
                    service.getPerson(callerId, app.accessToken).subscribe({
                        val launchIntent = Intent(this@RequestListenerService, SummonActivity::class.java)
                        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        launchIntent.putExtra(SummonActivity.IS_INCOMING, true)
                        launchIntent.putExtra(SummonActivity.IS_WAKEFUL, true)
                        launchIntent.putExtra(App.REQUEST, request.data)
                        launchIntent.putExtra(PersonActivity.PERSON, it)
                        this@RequestListenerService.startActivity(launchIntent)
                    }, {
                        Log.e(RequestListenerService::class.java.simpleName, "request error", it)
                    })
                } else if (!request.data.enabled) {
                    Toast.makeText(this@RequestListenerService, R.string.request_canceled, Toast.LENGTH_LONG).show()
                }
            } else if (request.data.callerId == accessToken.userId) {
                when (request.data.state) {
                    RequestStatus.Accepted.code ->
                        Toast.makeText(this@RequestListenerService, R.string.request_accepted, Toast.LENGTH_LONG).show()
                    RequestStatus.Rejected.code ->
                        Toast.makeText(this@RequestListenerService, R.string.request_rejected, Toast.LENGTH_LONG).show()
                }
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
        private const val REQUEST_URL_SUFFIX = "summonrequests/change-stream?options="
        private const val REQUEST_URL_ESC_SUFFIX = "{\"where\":{\"callerId\":%d}}"
        private const val ACTION_LISTEN_REQUEST = "employee.summon.asano.action.LISTEN_REQUEST"
        private const val ACTION_CLOSE_CONNECTION = "employee.summon.asano.action.CLOSE_CONNECTION"
        private const val WAKELOCK_TAG = "SumEmpWakelockTag"
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

        /**
         * Starts this service to perform action Foo with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        @JvmStatic
        fun cancelActionListenRequest(context: Context) {
            val intent = Intent(context, RequestListenerService::class.java).apply {
                action = ACTION_CLOSE_CONNECTION
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
