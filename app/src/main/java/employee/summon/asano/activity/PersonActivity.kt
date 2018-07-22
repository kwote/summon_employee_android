package employee.summon.asano.activity

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import employee.summon.asano.*
import employee.summon.asano.databinding.PersonActivityBinding
import employee.summon.asano.model.AddSummonRequest
import employee.summon.asano.model.Person
import employee.summon.asano.model.SummonRequest
import employee.summon.asano.rest.SummonRequestService
import employee.summon.asano.viewmodel.PendingRequestVM
import employee.summon.asano.viewmodel.PersonVM
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_person.*
import java.util.*

class PersonActivity : AppCompatActivity() {
    private lateinit var personVM: PersonVM
    private lateinit var pendingRequest: PendingRequestVM
    private val handlers = ClickHandlers(this)

    private val disposable = AndroidDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<PersonActivityBinding>(this, R.layout.activity_person)

        val person = intent.getParcelableExtra<Person>(PERSON)
        pendingRequest = PendingRequestVM()
        personVM = PersonVM(person)
        binding.person = personVM
        binding.request = pendingRequest
        binding.handlers = handlers

        getPendingRequest()
        RequestListenerService.requestUpdateBus
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { update ->
            if (pendingRequest.request?.id == update.request.id) {
                when (update.request.state) {
                    SummonRequest.RequestState.Accepted.code -> {
                        getPendingRequest()
                        Snackbar.make(phone_view, R.string.request_accepted, Snackbar.LENGTH_SHORT).show()
                    }
                    SummonRequest.RequestState.Rejected.code -> {
                        getPendingRequest()
                        Snackbar.make(phone_view, R.string.request_rejected, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }.addTo(disposable)
    }

    private fun getPendingRequest() {
        if (!personVM.isMe(this)) {
            getLastOutgoingSummonRequests(
                    personVM.person.id, 3)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        val active = it.filter { it.pending && it.enabled }
                        pendingRequest.request = if (active.isEmpty()) {
                            null
                        } else {
                            active[0]
                        }
                        val binding = DataBindingUtil.findBinding<PersonActivityBinding>(summon_btn)
                        binding?.request = pendingRequest
                        binding?.executePendingBindings()
                    }, {
                        pendingRequest.request = null
                        val binding = DataBindingUtil.findBinding<PersonActivityBinding>(summon_btn)
                        binding?.request = pendingRequest
                        binding?.executePendingBindings()
                        Snackbar.make(phone_view, R.string.error_unknown, Snackbar.LENGTH_SHORT).show()
                    }).addTo(disposable)
        }
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }

    private fun getLastOutgoingSummonRequests(targetId: Int, count: Int):
            Observable<List<SummonRequest>> {
        val app = App.getApp(this)
        val callerId = app.user.id
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        val date = cal.time
        val filter = mutableMapOf(
                "where" to mutableMapOf(
                        "and" to mutableListOf(
                                mutableMapOf(
                                        "callerId" to callerId
                                ),
                                mutableMapOf(
                                        "targetId" to targetId
                                ),
                                mutableMapOf(
                                        "requested" to mutableMapOf(
                                                "gt" to date.getStringTimeStampWithDate()
                                        )
                                )
                        )
                ),
                "limit" to count,
                "order" to "requested DESC"
        )
        val filterStr = moshi.adapter(Map::class.java).toJson(filter)
        return app.getService<SummonRequestService>()
                .listRequests(filterStr, app.accessToken)
    }

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    companion object {
        const val PERSON = "person"
    }

    inner class ClickHandlers(var context: Context) {
        fun summon(v: View) {
            val app = App.getApp(this@PersonActivity)
            val accessToken = app.accessToken
            val addRequest = AddSummonRequest(app.user.id, personVM.person.id)
            val service = app.getService<SummonRequestService>()
            service.addSummonRequest(addRequest, accessToken)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ request ->
                        pendingRequest.request = request
                        val binding = DataBindingUtil.findBinding<PersonActivityBinding>(v)
                        binding?.request = pendingRequest
                        binding?.executePendingBindings()
                        Snackbar.make(phone_view, R.string.summon_succeeded, Snackbar.LENGTH_SHORT).show()
                    }, {
                        Snackbar.make(phone_view, R.string.summon_failed, Snackbar.LENGTH_LONG).show()
                    }).addTo(disposable)
        }

        fun cancel(v: View) {
            pendingRequest.request?.id?.let { requestId ->
                val app = App.getApp(this@PersonActivity)
                val service = app.getService<SummonRequestService>()
                service.cancelRequest(requestId, app.accessToken)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            getPendingRequest()
                            Snackbar.make(phone_view, R.string.cancel_successful, Snackbar.LENGTH_SHORT).show()
                        }, {
                            Snackbar.make(phone_view, R.string.error_unknown, Snackbar.LENGTH_SHORT).show()
                        })
            }
        }

        fun dial(v: View) {
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", personVM.person.phone, null))
            startActivity(intent)
        }
    }
}