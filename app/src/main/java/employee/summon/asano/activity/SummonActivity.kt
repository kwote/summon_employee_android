package employee.summon.asano.activity

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
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
            r?.play()
        }
        RequestListenerService.requestUpdateBus.observeOn(AndroidSchedulers.mainThread()).subscribe { update ->
            if (request.id == update.request.id) {
                requestVM.request = update.request
                val binding = DataBindingUtil.findBinding<SummonActivityBinding>(phone_view)
                binding?.requestVM = requestVM
                binding?.executePendingBindings()
                when (update.type) {
                    SummonRequestUpdate.UpdateType.Cancel ->
                        if (isWakeful)
                            Snackbar.make(phone_view, R.string.request_canceled, Snackbar.LENGTH_SHORT).show()
                        else finish()
                    SummonRequestUpdate.UpdateType.Accept ->
                        Snackbar.make(phone_view, R.string.request_accepted, Snackbar.LENGTH_SHORT).show()
                    SummonRequestUpdate.UpdateType.Reject ->
                        Snackbar.make(phone_view, R.string.request_rejected, Snackbar.LENGTH_SHORT).show()
                }
            }
        }.addTo(disposable)
    }

    override fun onResume() {
        super.onResume()
        if (isWakeful) {
            r?.play()
        }
    }

    override fun onPause() {
        if (isWakeful) {
            r?.stop()
        }
        super.onPause()
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

    private fun cancelRequest(request: SummonRequest) =
            App.getApp(this).getService<SummonRequestService>()
                    .cancelRequest(request.id, App.getApp(this).accessToken)
                    .observeOn(AndroidSchedulers.mainThread())

    inner class ClickHandlers(var context: Context) {
        fun accept(v: View) {
            if (isWakeful) {
                r?.stop()
                r = null
            }
            acceptRequest(requestVM.request)
                    .subscribe({
                        requestVM.request = requestVM.request.acceptedRequest()
                        val binding = DataBindingUtil.findBinding<SummonActivityBinding>(phone_view)
                        binding?.requestVM = requestVM
                        binding?.executePendingBindings()
                        if (!isWakeful)
                            Snackbar.make(phone_view, R.string.request_accepted, Snackbar.LENGTH_LONG).show()
                        else finish()
                    }, {
                        Snackbar.make(phone_view, R.string.request_accept_failed, Snackbar.LENGTH_LONG).show()
                    }).addTo(disposable)
        }

        fun reject(v: View) {
            if (isWakeful) {
                r?.stop()
                r = null
            }
            rejectRequest(requestVM.request)
                    .subscribe({
                        requestVM.request = requestVM.request.rejectedRequest()
                        val binding = DataBindingUtil.findBinding<SummonActivityBinding>(phone_view)
                        binding?.requestVM = requestVM
                        binding?.executePendingBindings()
                        if (!isWakeful)
                            Snackbar.make(phone_view, R.string.request_rejected, Snackbar.LENGTH_LONG).show()
                        else finish()
                    }, {
                        Snackbar.make(phone_view, R.string.request_reject_failed, Snackbar.LENGTH_LONG).show()
                    }).addTo(disposable)
        }
        fun cancel(v: View) {
            cancelRequest(requestVM.request)
                    .subscribe({
                        requestVM.request = requestVM.request.canceledRequest()
                        val binding = DataBindingUtil.findBinding<SummonActivityBinding>(phone_view)
                        binding?.requestVM = requestVM
                        binding?.executePendingBindings()
                        Snackbar.make(phone_view, R.string.request_canceled, Snackbar.LENGTH_LONG).show()
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
}
