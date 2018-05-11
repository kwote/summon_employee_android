package employee.summon.asano.activity

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import employee.summon.asano.App
import employee.summon.asano.R
import employee.summon.asano.databinding.PersonActivityBinding
import employee.summon.asano.getStringTimeStampWithDate
import employee.summon.asano.model.Person
import employee.summon.asano.model.SummonRequest
import employee.summon.asano.rest.SummonRequestService
import kotlinx.android.synthetic.main.activity_person.*
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

        call_employee.setOnClickListener({ _ ->
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", person?.phone, null))
            startActivity(intent)
        })

        summon_employee.setOnClickListener({ _ ->
            val now = Calendar.getInstance().time.getStringTimeStampWithDate()
            val addRequest = SummonRequest(null, app.accessToken?.userId!!, person!!.id, now, null)
            val service = app.getService<SummonRequestService>()
            val call = service.addSummonRequest(addRequest)

            call.enqueue(object : Callback<SummonRequest> {
                override fun onFailure(call: Call<SummonRequest>, t: Throwable) {
                    Toast.makeText(this@PersonActivity, R.string.summon_failed, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<SummonRequest>, response: Response<SummonRequest>) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@PersonActivity, R.string.summon_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            })
        })

        cancel_summon_request.setOnClickListener({ _ ->
            val service = app.getService<SummonRequestService>()
            val call = service.cancelRequest(pendingRequest!!.id)
            call.enqueue(object : Callback<SummonRequest> {
                override fun onFailure(call: Call<SummonRequest>, t: Throwable) {
                    Toast.makeText(this@PersonActivity, R.string.error_unknown, Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<SummonRequest>, response: Response<SummonRequest>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@PersonActivity, R.string.cancel_successful, Toast.LENGTH_SHORT).show()
                    }
                }
            })
        })

        if (app.accessToken?.userId != person?.id) {
            val service = app.getService<SummonRequestService>()
            val call = service.getSummonRequest(app.accessToken?.userId!!, person!!.id, true)
            call.enqueue(object : Callback<SummonRequest> {
                override fun onFailure(call: Call<SummonRequest>, t: Throwable) {
                    Toast.makeText(this@PersonActivity, R.string.error_unknown, Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<SummonRequest>, response: Response<SummonRequest>) {
                    if (response.isSuccessful) {
                        pendingRequest = response.body()
                        summon_employee.isEnabled = false
                        cancel_summon_request.isEnabled = true
                    }
                }
            })
        } else {
            call_employee.visibility = View.GONE
            summon_employee.visibility = View.GONE
            cancel_summon_request.visibility = View.GONE
        }
    }

    companion object {
        const val PERSON = "person"
    }
}