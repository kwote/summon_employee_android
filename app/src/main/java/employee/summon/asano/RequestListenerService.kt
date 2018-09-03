package employee.summon.asano

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Message
import android.os.PowerManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.squareup.moshi.Moshi
import com.tylerjroach.eventsource.EventSource
import com.tylerjroach.eventsource.EventSourceHandler
import com.tylerjroach.eventsource.MessageEvent
import employee.summon.asano.activity.MainActivity
import employee.summon.asano.activity.SummonActivity
import employee.summon.asano.model.SummonRequestMessage
import employee.summon.asano.model.SummonRequestUpdate
import employee.summon.asano.rest.PeopleService
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit


class RequestListenerService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private lateinit var accessToken: String
    private var userId: Int = 0

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                ACTION_LISTEN_REQUEST -> {
                    if (intent.hasExtra(App.ACCESS_TOKEN)) {
                        acquireWakeLock()
                        closeConnection()
                        accessToken = intent.getStringExtra(App.ACCESS_TOKEN)
                        userId = intent.getIntExtra(USER_ID_EXTRA, 0)
                        openConnection(accessToken, userId)
                        schedulePing()
                    }
                }
                ACTION_CLOSE_CONNECTION -> {
                    closeConnection()
                    stopSelf()
                }
            }

        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                WAKELOCK_TAG)
        wakeLock?.acquire(3600 * 8 * 1000)
    }

    private var connected = false
    private val disposable = AndroidDisposable()
    private var pingSchedule: Disposable? = null

    private fun schedulePing() {
        pingSchedule = Observable.interval(PING_PERIOD, PING_PERIOD, TimeUnit.SECONDS)
                .subscribe { _ ->
                    val app = App.getApp(this)
                    accessToken = app.accessToken
                    app.getService<PeopleService>()
                            .ping(accessToken)
                            .subscribe({valid->
                                if (valid && !connected) {
                                    openConnection(accessToken, userId)
                                } else if (!valid && connected) {
                                    closeConnection()
                                    val message = Message().apply { what = ConnectionState.Disconnected.code }
                                    messageBus.onNext(message)
                                }
                            }, {
                                closeConnection()
                            })
                }.addTo(disposable)
    }

    private fun openConnection(accessToken: String, userId: Int) {
        val headers = mutableMapOf("Authorization" to accessToken)
        eventSource = EventSource.Builder(
                App.getApp(this).serverUrl + "/api/" + REQUEST_URL_SUFFIX +
                        URLEncoder.encode(String.format(
                                REQUEST_URL_ESC_SUFFIX, userId, userId
                        ), "UTF-8"))
                .headers(headers)
                .reconnectInterval(PING_PERIOD / 4)
                .eventHandler(requestHandler)
                .build()
        eventSource?.connect()
        connected = true
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
                .setSmallIcon(R.drawable.baseline_dialpad_24)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.connection))
                .setOngoing(true)
                .build()

        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        closeConnection()
        stopForeground(true)
        wakeLock?.release()
        disposable.dispose()
        super.onDestroy()
    }

    private fun closeConnection() {
        eventSource?.close()
        connected = false
    }

    private var requestHandler = RequestHandler()

    private var eventSource: EventSource? = null

    private val moshi = Moshi.Builder().build()

    inner class RequestHandler : EventSourceHandler {
        override fun onConnect() {
            Log.v("SSE connected", "True")
            connected = true
        }

        override fun onComment(comment: String?) {
            Log.v("SSE Comment", comment)
        }

        override fun onMessage(event: String?, message: MessageEvent) {
            Log.v("SSE", event)
            Log.v("SSE Message: ", message.data)
            val adapter = moshi.adapter(SummonRequestMessage::class.javaObjectType)
            val requestMessage = adapter.fromJson(message.data) ?: return
            val request = requestMessage.request()
            if (request.targetId == userId) {
                when (requestMessage.type) {
                    "create" -> {
                        requestUpdateBus.onNext(SummonRequestUpdate(request, SummonRequestUpdate.UpdateType.Create))
                        val launchIntent = Intent(this@RequestListenerService, SummonActivity::class.java)
                        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        launchIntent.putExtra(SummonActivity.IS_INCOMING, true)
                        launchIntent.putExtra(SummonActivity.IS_WAKEFUL, true)
                        launchIntent.putExtra(App.REQUEST, request)
                        this@RequestListenerService.startActivity(launchIntent)
                    }
                    "accept" ->
                        requestUpdateBus.onNext(SummonRequestUpdate(request, SummonRequestUpdate.UpdateType.Accept))
                    "reject" ->
                        requestUpdateBus.onNext(SummonRequestUpdate(request, SummonRequestUpdate.UpdateType.Reject))
                    "cancel" -> {
                        requestUpdateBus.onNext(SummonRequestUpdate(request, SummonRequestUpdate.UpdateType.Cancel))
                    }
                }
            } else if (request.callerId == userId) {
                when (requestMessage.type) {
                    "accept" ->
                        requestUpdateBus.onNext(SummonRequestUpdate(request, SummonRequestUpdate.UpdateType.Accept))
                    "reject" ->
                        requestUpdateBus.onNext(SummonRequestUpdate(request, SummonRequestUpdate.UpdateType.Reject))
                }
            }
        }

        override fun onClosed(willReconnect: Boolean) {
            Log.v("SSE Closed", "reconnect? $willReconnect")
            if (!willReconnect) {
                connected = false
            }
        }

        override fun onError(t: Throwable?) {
            Log.e("SSE Error", "Failed", t)
        }
    }

    enum class ConnectionState(val code: Int) {
        None(0),
        Connecting(1),
        Connected(2),
        Disconnected(3)
    }

    companion object {
        val requestUpdateBus: PublishSubject<SummonRequestUpdate> = PublishSubject.create()
        val messageBus: PublishSubject<Message> = PublishSubject.create()

        private const val REQUEST_URL_SUFFIX = "summonrequests/change-stream?options="
        private const val REQUEST_URL_ESC_SUFFIX = "{\"where\":{\"or\":[{\"targetId\":%d},{\"callerId\":%d}]}}"
        private const val ACTION_LISTEN_REQUEST = "employee.summon.asano.action.LISTEN_REQUEST"
        private const val ACTION_CLOSE_CONNECTION = "employee.summon.asano.action.CLOSE_CONNECTION"
        private const val USER_ID_EXTRA = "user_id_extra"
        private const val WAKELOCK_TAG = "SumEmpWakelockTag"
        const val PING_PERIOD: Long = 60
        const val ONGOING_NOTIFICATION_ID = 1
        /**
         * Starts this service to perform action Foo with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        @JvmStatic
        fun startActionListenRequest(context: Context, accessToken: String, userId: Int) {
            val intent = Intent(context, RequestListenerService::class.java).apply {
                action = ACTION_LISTEN_REQUEST
            }
            intent.putExtra(App.ACCESS_TOKEN, accessToken)
            intent.putExtra(USER_ID_EXTRA, userId)
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
