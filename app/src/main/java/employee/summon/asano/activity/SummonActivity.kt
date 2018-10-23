/*
 * Copyright (c) 2018. $user.name. All rights reserved.
 */

package employee.summon.asano.activity

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import employee.summon.asano.*
import employee.summon.asano.App.Companion.REQUEST
import employee.summon.asano.databinding.SummonActivityBinding
import employee.summon.asano.model.SummonRequest
import employee.summon.asano.model.SummonRequestUpdate
import employee.summon.asano.rest.SummonRequestService
import employee.summon.asano.viewmodel.SummonRequestVM
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_summon.*


class SummonActivity : AppCompatActivity() {
    companion object {
        const val IS_INCOMING = "is_incoming"
        const val IS_WAKEFUL = "is_wakeful"
    }

    private lateinit var requestVM: SummonRequestVM
    private val handlers = ClickHandlers(this)

    private var isWakeful: Boolean = false

    private var r: Ringtone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<SummonActivityBinding>(this, R.layout.activity_summon)

        val request: SummonRequest = intent.getParcelableExtra(REQUEST)
        val isIncoming = intent.getBooleanExtra(IS_INCOMING, true)
        requestVM = SummonRequestVM(request, isIncoming)
        binding.requestVM = requestVM
        binding.handlers = handlers
        isWakeful = intent.getBooleanExtra(IS_WAKEFUL, false)
        if (isWakeful) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
                val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                keyguardManager.requestDismissKeyguard(this, null)
            } else {
                this.window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            }
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            r = RingtoneManager.getRingtone(applicationContext, notification)
            startSignal()
        }
        RequestListenerService.requestUpdateBus
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { update ->
            if (request.id == update.request.id) {
                requestVM.request = update.request
                updateView()
                when (update.type) {
                    SummonRequestUpdate.UpdateType.Cancel ->
                        if (isWakeful)
                            finish()
                        else
                            Snackbar.make(phone_view, R.string.request_canceled, Snackbar.LENGTH_SHORT).show()
                    SummonRequestUpdate.UpdateType.Accept ->
                        if (isWakeful)
                            finish()
                        else
                            Snackbar.make(phone_view, R.string.request_accepted, Snackbar.LENGTH_SHORT).show()
                    SummonRequestUpdate.UpdateType.Reject ->
                        if (isWakeful)
                            finish()
                        else
                            Snackbar.make(phone_view, R.string.request_rejected, Snackbar.LENGTH_SHORT).show()
                    else -> {
                    }
                }
            }
        }.addTo(disposable)

        RequestListenerService.messageBus
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {message->
                    when (message.what) {
                        RequestListenerService.ConnectionState.Disconnected.code -> finish()
                    }
                }.addTo(disposable)
    }

    private fun startSignal() {
        r?.play()// Get instance of Vibrator from current Context
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = VibrationEffect.createWaveform(longArrayOf(0, 100, 1000), 0)

            v.vibrate(pattern)
        } else {
            v.vibrate(longArrayOf(0, 100, 1000), 0)
        }
    }

    override fun onResume() {
        super.onResume()
        if (isWakeful) {
            startSignal()
        }
    }

    override fun onPause() {
        if (isWakeful) {
            stopSignal()
        }
        super.onPause()
    }

    private fun stopSignal() {
        r?.stop()
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        v.cancel()
    }

    private val disposable = AndroidDisposable()
    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }

    private fun acceptRequest(request: SummonRequest) =
            App.getApp(this).getService<SummonRequestService>()
                    .acceptRequest(request.id)
                    .observeOn(AndroidSchedulers.mainThread())

    private fun rejectRequest(request: SummonRequest) =
            App.getApp(this).getService<SummonRequestService>()
                    .rejectRequest(request.id)
                    .observeOn(AndroidSchedulers.mainThread())

    private fun cancelRequest(request: SummonRequest): Observable<SummonRequest> {
        val app = App.getApp(this)
        return app.getService<SummonRequestService>()
                .cancelRequest(request.id, app.accessToken)
                .observeOn(AndroidSchedulers.mainThread())
    }

    inner class ClickHandlers(var context: Context) {
        fun accept(v: View) {
            if (isWakeful) {
                stopSignal()
                r = null
            }
            acceptRequest(requestVM.request)
                    .subscribe({
                        requestVM.accept()
                        updateView()
                        if (isWakeful) finish()
                    }, {
                        Snackbar.make(phone_view, R.string.request_accept_failed, Snackbar.LENGTH_LONG).show()
                    }).addTo(disposable)
        }

        fun reject(v: View) {
            if (isWakeful) {
                stopSignal()
                r = null
            }
            rejectRequest(requestVM.request)
                    .subscribe({
                        requestVM.reject()
                        updateView()
                        if (isWakeful) finish()
                    }, {
                        Snackbar.make(phone_view, R.string.request_reject_failed, Snackbar.LENGTH_LONG).show()
                    }).addTo(disposable)
        }

        fun cancel(v: View) {
            cancelRequest(requestVM.request)
                    .subscribe({
                        requestVM.cancel()
                        updateView()
                    }, {
                        Snackbar.make(phone_view, R.string.request_cancel_failed, Snackbar.LENGTH_LONG).show()
                    }).addTo(disposable)
        }

        fun person(v: View) {
            val intent = Intent(this@SummonActivity, PersonActivity::class.java)
            intent.putExtra(PersonActivity.PERSON, requestVM.person?.person)
            startActivity(intent)
        }
    }

    private fun updateView() {
        val reqBinding = DataBindingUtil.findBinding<SummonActivityBinding>(phone_view)
        reqBinding?.requestVM = requestVM
        reqBinding?.executePendingBindings()
    }
}
