package employee.summon.asano.activity

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import employee.summon.asano.App
import employee.summon.asano.R
import employee.summon.asano.databinding.PersonActivityBinding
import employee.summon.asano.getStringTimeStampWithDate
import employee.summon.asano.model.Person
import employee.summon.asano.model.SummonRequest
import employee.summon.asano.rest.SummonRequestService
import kotlinx.android.synthetic.main.activity_person.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class PersonActivity : Activity() {
    private var person: Person? = null
    private val app: App
        get() = application as App
    private var pendingRequest: SummonRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<PersonActivityBinding>(this, R.layout.activity_person)

        person = intent.getParcelableExtra(PERSON)
        binding.person = person

        call_fab.setOnClickListener({ _ ->
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", person?.phone, null))
            startActivity(intent)
        })

        if (app.accessToken.userId != person?.id) {
            getLastOutgoingSummonRequests(app.accessToken.userId, person!!.id, 3, {r->
                if (r.isEmpty()) {
                    makeSummonButton()
                } else {
                    makeCancelButton()
                }
            }, {_, _ ->
                makeSummonButton()
                Snackbar.make(phone_view, R.string.error_unknown, Snackbar.LENGTH_SHORT).show()
            })
        } else {
            call_fab.visibility = View.GONE
            summon_fab.visibility = View.GONE
        }
    }

    private fun getLastOutgoingSummonRequests(callerId: Int, targetId: Int, count: Int,
                                              onSuccess: (r: List<SummonRequest>)->Unit,
                                              onFail: (r: ResponseBody?, t: Throwable?)->Unit) {
        val service = app.getService<SummonRequestService>()

        val call = service.listOutgoingRequests(callerId, targetId, app.accessToken.id)
        call.enqueue(object : Callback<List<SummonRequest>> {
            override fun onFailure(call: Call<List<SummonRequest>>, t: Throwable) {
                onFail(null, t)
            }

            override fun onResponse(call: Call<List<SummonRequest>>, response: Response<List<SummonRequest>>) {
                if (response.isSuccessful) {
                    onSuccess(response.body())
                } else {
                    onFail(response.errorBody(), null)
                }
            }
        })
    }

    private fun makeSummonButton() {
        summon_fab.setImageResource(R.drawable.horn)
        summon_fab.setOnClickListener({ _ ->
            val now = Calendar.getInstance().time.getStringTimeStampWithDate()
            val accessToken = app.accessToken
            val addRequest = SummonRequest(null, accessToken.userId, person!!.id, now)
            val service = app.getService<SummonRequestService>()
            val call = service.addSummonRequest(addRequest, accessToken.id)

            call.enqueue(object : Callback<SummonRequest> {
                override fun onFailure(call: Call<SummonRequest>, t: Throwable) {
                    Snackbar.make(phone_view, R.string.summon_failed, Snackbar.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<SummonRequest>, response: Response<SummonRequest>) {
                    if (!response.isSuccessful) {
                        Snackbar.make(phone_view, R.string.summon_failed, Snackbar.LENGTH_SHORT).show()
                    }
                }
            })
        })
    }

    private fun makeCancelButton() {
        summon_fab.setImageResource(R.drawable.cancel)
        summon_fab.setOnClickListener({ _ ->
            val service = app.getService<SummonRequestService>()
            val cancelCall = pendingRequest?.id?.let { it -> service.cancelRequest(it, app.accessToken.id) }
            cancelCall?.enqueue(object : Callback<SummonRequest> {
                override fun onFailure(call: Call<SummonRequest>, t: Throwable) {
                    Snackbar.make(phone_view, R.string.error_unknown, Snackbar.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<SummonRequest>, response: Response<SummonRequest>) {
                    if (response.isSuccessful) {
                        Snackbar.make(phone_view, R.string.cancel_successful, Snackbar.LENGTH_SHORT).show()
                    }
                }
            })
        })
    }

    companion object {
        const val PERSON = "person"
    }
}