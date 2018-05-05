package employee.summon.asano.activity

import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import employee.summon.asano.App
import employee.summon.asano.App.Companion.REQUEST
import employee.summon.asano.R
import employee.summon.asano.activity.PersonActivity.Companion.PERSON
import employee.summon.asano.databinding.SummonActivityBinding
import employee.summon.asano.model.Person
import employee.summon.asano.model.SummonRequest

class SummonActivity : AppCompatActivity() {
    private val app: App
        get() = application as App
    private var request: SummonRequest? = null
    private var caller: Person? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<SummonActivityBinding>(this, R.layout.activity_person)

        request = intent.getParcelableExtra(REQUEST)
        binding.request = request
        caller = intent.getParcelableExtra(PERSON)
        binding.caller = caller
    }
}
