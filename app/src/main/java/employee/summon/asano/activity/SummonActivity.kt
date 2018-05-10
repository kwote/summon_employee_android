package employee.summon.asano.activity

import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
    }

    private val app: App
        get() = application as App
    private var request: SummonRequest? = null
    private var person: Person? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<SummonActivityBinding>(this, R.layout.activity_summon)

        request = intent.getParcelableExtra(REQUEST)
        binding.request = request
        person = intent.getParcelableExtra(PERSON)
        binding.person = person
        val isIncoming = intent.getBooleanExtra(IS_INCOMING, true)
        binding.incoming = isIncoming
        accept_request.setOnClickListener({
            acceptRequest(this.request!!)
        })
        reject_request.setOnClickListener({
            rejectRequest(this.request!!)
        })
    }

    private fun acceptRequest(request: SummonRequest) {
        val service = app.getService<SummonRequestService>()
        val call = service.acceptRequest(request.id)
        call.enqueue(object : Callback<SummonRequest> {
            override fun onFailure(call: Call<SummonRequest>, t: Throwable) {
                Log.e(SummonActivity::class.java.simpleName, "Accept request failed", t)
            }

            override fun onResponse(call: Call<SummonRequest>, response: Response<SummonRequest>) {
                Toast.makeText(this@SummonActivity, "Request accepted", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun rejectRequest(request: SummonRequest) {
        val service = app.getService<SummonRequestService>()
        val call = service.rejectRequest(request.id)
        call.enqueue(object : Callback<SummonRequest> {
            override fun onFailure(call: Call<SummonRequest>, t: Throwable) {
                Log.e(SummonActivity::class.java.simpleName, "Reject request failed", t)
            }

            override fun onResponse(call: Call<SummonRequest>, response: Response<SummonRequest>) {
                Toast.makeText(this@SummonActivity, "Request rejected", Toast.LENGTH_LONG).show()
            }
        })
    }
}
