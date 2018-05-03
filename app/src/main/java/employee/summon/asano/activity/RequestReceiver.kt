package employee.summon.asano.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import employee.summon.asano.App
import employee.summon.asano.model.SummonRequest

class RequestReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val request = intent.getParcelableExtra<SummonRequest>(App.MESSAGE)
        Toast.makeText(context, "Request received", Toast.LENGTH_LONG).show()
    }
}
