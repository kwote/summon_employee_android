package employee.summon.asano.activity

import android.os.Bundle
import android.app.Activity
import android.databinding.DataBindingUtil
import employee.summon.asano.App
import employee.summon.asano.R
import employee.summon.asano.databinding.CallActivityBinding

class CallActivity : Activity() {
    private val app: App
        get() = application as App
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<CallActivityBinding>(this, R.layout.activity_call)
        val person = app.accessToken?.user
        binding.person = person
    }
}
