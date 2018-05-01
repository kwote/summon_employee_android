package employee.summon.asano.activity

import android.os.Bundle
import android.app.Activity
import android.databinding.DataBindingUtil
import employee.summon.asano.App
import employee.summon.asano.R
import employee.summon.asano.databinding.CallActivityBinding
import kotlinx.android.synthetic.main.activity_call.*
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import employee.summon.asano.model.Person
import employee.summon.asano.model.SummonRequest
import employee.summon.asano.rest.SummonRequestService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class CallActivity : Activity() {
    private var person: Person? = null
    private val app: App
        get() = application as App

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<CallActivityBinding>(this, R.layout.activity_call)

        person = intent.getParcelableExtra(PERSON)

        binding.person = person

        call_employee.setOnClickListener({ _ ->
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", person?.phone, null))
            startActivity(intent)
        })

        summon_employee.setOnClickListener({ _ ->
            val now = Calendar.getInstance().time.getStringTimeStampWithDate()
            val addRequest = SummonRequest(null, app.accessToken?.userId!!, person!!.id, now, null, null)
            val summonRequestService = app.retrofit!!.create<SummonRequestService>(SummonRequestService::class.java)
            val call = summonRequestService.addSummonRequest(addRequest)

            call.enqueue(object : Callback<SummonRequest> {
                override fun onFailure(call: Call<SummonRequest>, t: Throwable) {
                    Toast.makeText(this@CallActivity, R.string.summon_failed, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<SummonRequest>, response: Response<SummonRequest>) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@CallActivity, R.string.summon_failed, Toast.LENGTH_SHORT).show()
                    } else {
                    }
                }
            })
        })
    }

    /** Converting from Date to String**/
    private fun Date.getStringTimeStampWithDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        return dateFormat.format(this)
    }

    companion object {
        const val PERSON = "person"
    }
}
