package employee.summon.asano.activity

import android.app.KeyguardManager
import android.content.Context
import android.databinding.DataBindingUtil
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.WindowManager
import employee.summon.asano.AndroidDisposable
import employee.summon.asano.App
import employee.summon.asano.App.Companion.REQUEST
import employee.summon.asano.R
import employee.summon.asano.activity.PersonActivity.Companion.PERSON
import employee.summon.asano.addTo
import employee.summon.asano.databinding.SummonActivityBinding
import employee.summon.asano.model.Person
import employee.summon.asano.model.SummonRequest
import employee.summon.asano.rest.SummonRequestService
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_summon.*


class SummonActivity : AppCompatActivity() {
    companion object {
        const val IS_INCOMING = "is_incoming"
        const val IS_WAKEFUL = "is_wakeful"
    }

    private val app: App
        get() = application as App
    private var request: SummonRequest? = null
    private var person: Person? = null

    private var isWakeful: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<SummonActivityBinding>(this, R.layout.activity_summon)

        request = intent.getParcelableExtra(REQUEST)
        binding.request = request
        person = intent.getParcelableExtra(PERSON)
        binding.person = person
        val isIncoming = intent.getBooleanExtra(IS_INCOMING, true)
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
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(applicationContext, notification)
            r.play()
        }
        binding.incoming = isIncoming
        accept_request.setOnClickListener({
            acceptRequest(this.request!!)
                    .subscribe({
                        if (!isWakeful)
                            Snackbar.make(phone_view, R.string.request_accepted, Snackbar.LENGTH_LONG).show()
                        else finish()
                    }, {
                        Snackbar.make(phone_view, R.string.request_accept_failed, Snackbar.LENGTH_LONG).show()
                    }).addTo(disposable)
        })
        reject_request.setOnClickListener({
            rejectRequest(this.request!!)
                    .subscribe({
                        if (!isWakeful)
                        Snackbar.make(phone_view, R.string.request_rejected, Snackbar.LENGTH_LONG).show()
                        else                             finish()
                    }, {
                        Snackbar.make(phone_view, R.string.request_reject_failed, Snackbar.LENGTH_LONG).show()
                    }).addTo(disposable)
            if (isWakeful) finish()
        })
        cancel_request.setOnClickListener({
            cancelRequest(this.request!!)
        })
    }

    private val disposable = AndroidDisposable()
    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }

    private fun acceptRequest(request: SummonRequest) =
            app.getService<SummonRequestService>()
            .acceptRequest(request.id!!)
            .observeOn(AndroidSchedulers.mainThread())

    private fun rejectRequest(request: SummonRequest) = app.getService<SummonRequestService>()
            .rejectRequest(request.id!!)
            .observeOn(AndroidSchedulers.mainThread())

    private fun cancelRequest(request: SummonRequest) = app.getService<SummonRequestService>()
            .cancelRequest(request.id!!, app.accessToken)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Snackbar.make(phone_view, R.string.request_canceled, Snackbar.LENGTH_LONG).show()
            }, {
                Snackbar.make(phone_view, R.string.request_cancel_failed, Snackbar.LENGTH_LONG).show()
            }).addTo(disposable)
}
