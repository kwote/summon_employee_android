package employee.summon.asano.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import employee.summon.asano.App
import employee.summon.asano.model.Person
import employee.summon.asano.model.SummonRequest
import employee.summon.asano.rest.PeopleService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RequestReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val request = intent.getParcelableExtra<SummonRequest>(App.REQUEST)
        val callerId = request.callerId
        val service = (context.applicationContext as App).getService<PeopleService>()
        val call = service.getPerson(callerId)
        call.enqueue(object : Callback<Person> {
            override fun onFailure(call: Call<Person>, t: Throwable) {
                Log.e(RequestReceiver::class.java.simpleName, "request error", t)
            }

            override fun onResponse(call: Call<Person>, response: Response<Person>) {
                if (response.isSuccessful) {
                    val caller = response.body()
                    val launchIntent = Intent(context, SummonActivity::class.java)
                    launchIntent.putExtra(SummonActivity.IS_INCOMING, true)
                    launchIntent.putExtra(App.REQUEST, request)
                    launchIntent.putExtra(PersonActivity.PERSON, caller)
                    context.startActivity(launchIntent)
                } else {
                    Log.e(RequestReceiver::class.java.simpleName, "request failed")
                }
            }
        })
    }
}
