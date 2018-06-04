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
import employee.summon.asano.App
import employee.summon.asano.App.Companion.REQUEST
import employee.summon.asano.R
import employee.summon.asano.activity.PersonActivity.Companion.PERSON
import employee.summon.asano.databinding.SummonActivityBinding
import employee.summon.asano.model.Person
import employee.summon.asano.model.SummonRequest
import employee.summon.asano.rest.SummonRequestService
import kotlinx.android.synthetic.main.activity_summon.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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
            if (isWakeful) finish()
        })
        reject_request.setOnClickListener({
            rejectRequest(this.request!!)
            if (isWakeful) finish()
        })
        cancel_request.setOnClickListener({
            cancelRequest(this.request!!)
        })
    }

    private fun acceptRequest(request: SummonRequest) {
        val service = app.getService<SummonRequestService>()
        val call = service.acceptRequest(request.id!!, app.accessToken.id)
        call.enqueue(object : Callback<SummonRequest> {
            override fun onFailure(call: Call<SummonRequest>, t: Throwable) {
                Log.e(SummonActivity::class.java.simpleName, "Accept request failed", t)
            }

            override fun onResponse(call: Call<SummonRequest>, response: Response<SummonRequest>) {
                Snackbar.make(phone_view, R.string.request_accepted, Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun rejectRequest(request: SummonRequest) {
        val service = app.getService<SummonRequestService>()
        val call = service.rejectRequest(request.id!!, app.accessToken.id)
        call.enqueue(object : Callback<SummonRequest> {
            override fun onFailure(call: Call<SummonRequest>, t: Throwable) {
                Log.e(SummonActivity::class.java.simpleName, "Reject request failed", t)
            }

            override fun onResponse(call: Call<SummonRequest>, response: Response<SummonRequest>) {
                Snackbar.make(phone_view, R.string.request_rejected, Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun cancelRequest(request: SummonRequest) {
        val service = app.getService<SummonRequestService>()
        val call = service.cancelRequest(request.id!!, app.accessToken.id)
        call.enqueue(object : Callback<SummonRequest> {
            override fun onFailure(call: Call<SummonRequest>, t: Throwable) {
                Log.e(SummonActivity::class.java.simpleName, "Cancel request failed", t)
            }

            override fun onResponse(call: Call<SummonRequest>, response: Response<SummonRequest>) {
                Snackbar.make(phone_view, R.string.request_canceled, Snackbar.LENGTH_LONG).show()
            }
        })
    }
}
