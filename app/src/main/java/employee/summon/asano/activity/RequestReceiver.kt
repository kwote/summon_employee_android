package employee.summon.asano.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import employee.summon.asano.App
import employee.summon.asano.R
import employee.summon.asano.model.Person
import employee.summon.asano.model.RequestStatus
import employee.summon.asano.model.SummonRequest
import employee.summon.asano.rest.IPeopleService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RequestReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val request = intent.getParcelableExtra<SummonRequest>(App.REQUEST)
        val isIncoming = intent.getBooleanExtra(SummonActivity.IS_INCOMING, false)
        if (isIncoming) {
            if (request.enabled && request.pending) {
                val callerId = request.callerId
                val app = context.applicationContext as App
                val service = app.getService<IPeopleService>()
                val call = service.getPerson(callerId, app.accessToken.id)
                call.enqueue(object : Callback<Person> {
                    override fun onFailure(call: Call<Person>, t: Throwable) {
                        Log.e(RequestReceiver::class.java.simpleName, "request error", t)
                    }

                    override fun onResponse(call: Call<Person>, response: Response<Person>) {
                        if (response.isSuccessful) {
                            val caller = response.body()
                            val launchIntent = Intent(context, SummonActivity::class.java)
                            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            launchIntent.putExtra(SummonActivity.IS_INCOMING, true)
                            launchIntent.putExtra(SummonActivity.IS_WAKEFUL, true)
                            launchIntent.putExtra(App.REQUEST, request)
                            launchIntent.putExtra(PersonActivity.PERSON, caller)
                            context.startActivity(launchIntent)
                        } else {
                            Log.e(RequestReceiver::class.java.simpleName, "request failed")
                        }
                    }
                })
            } else if (!request.enabled) {
                // TODO notification
                Toast.makeText(context, R.string.request_canceled, Toast.LENGTH_LONG).show()
            }
        } else {
            if (request.state == RequestStatus.Accepted.code) {
                Toast.makeText(context, R.string.request_accepted, Toast.LENGTH_LONG).show()
            } else if (request.state == RequestStatus.Rejected.code) {
                Toast.makeText(context, R.string.request_rejected, Toast.LENGTH_LONG).show()
            }
        }
    }
}
