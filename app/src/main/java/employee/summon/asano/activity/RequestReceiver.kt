package employee.summon.asano.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import employee.summon.asano.App
import employee.summon.asano.R
import employee.summon.asano.model.RequestStatus
import employee.summon.asano.model.SummonRequest
import employee.summon.asano.rest.PeopleService

class RequestReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val request = intent.getParcelableExtra<SummonRequest>(App.REQUEST)
        val isIncoming = intent.getBooleanExtra(SummonActivity.IS_INCOMING, false)
        if (isIncoming) {
            if (request.enabled && request.pending) {
                val callerId = request.callerId
                val app = context.applicationContext as App
                val service = app.getService<PeopleService>()
                service.getPerson(callerId, app.accessToken).subscribe({
                    val launchIntent = Intent(context, SummonActivity::class.java)
                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    launchIntent.putExtra(SummonActivity.IS_INCOMING, true)
                    launchIntent.putExtra(SummonActivity.IS_WAKEFUL, true)
                    launchIntent.putExtra(App.REQUEST, request)
                    launchIntent.putExtra(PersonActivity.PERSON, it)
                    context.startActivity(launchIntent)
                }, {
                    Log.e(RequestReceiver::class.java.simpleName, "request error", it)
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
