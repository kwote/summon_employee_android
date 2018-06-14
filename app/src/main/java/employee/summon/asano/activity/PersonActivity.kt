package employee.summon.asano.activity

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import employee.summon.asano.*
import employee.summon.asano.databinding.PersonActivityBinding
import employee.summon.asano.model.AddSummonRequest
import employee.summon.asano.model.Person
import employee.summon.asano.model.SummonRequest
import employee.summon.asano.rest.SummonRequestService
import employee.summon.asano.viewmodel.PersonVM
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_person.*

class PersonActivity : Activity() {
    private lateinit var person: Person
    private var pendingRequest: SummonRequest? = null

    private val disposable = AndroidDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<PersonActivityBinding>(this, R.layout.activity_person)

        person = intent.getParcelableExtra(PERSON)
        binding.person = PersonVM(person)

        call_fab.setOnClickListener { _ ->
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", person.phone, null))
            startActivity(intent)
        }

        val userId = App.getApp(this).user.id
        if (userId != person.id) {
            getLastOutgoingSummonRequests(userId, person.id, 3)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        val active = it.filter { it.pending && it.enabled }
                        if (active.isEmpty()) {
                            makeSummonButton()
                        } else {
                            pendingRequest = active[0]
                            makeCancelButton()
                        }
                    }, {
                        makeSummonButton()
                        Snackbar.make(phone_view, R.string.error_unknown, Snackbar.LENGTH_SHORT).show()
                    }).addTo(disposable)
        } else {
            call_fab.visibility = View.GONE
            summon_fab.visibility = View.GONE
        }
        RequestListenerService.requestUpdateBus.observeOn(AndroidSchedulers.mainThread()).subscribe { update->
            if (pendingRequest?.id == update.request.id) {
                when (update.request.state) {
                    SummonRequest.RequestStatus.Accepted.code ->
                        Snackbar.make(phone_view, R.string.request_accepted, Snackbar.LENGTH_SHORT).show()
                    SummonRequest.RequestStatus.Rejected.code ->
                        Snackbar.make(phone_view, R.string.request_rejected, Snackbar.LENGTH_SHORT).show()
                }
            }
        }.addTo(disposable)
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }

    private fun getLastOutgoingSummonRequests(callerId: Int, targetId: Int, count: Int): Observable<List<SummonRequest>> {
        val app = App.getApp(this)
        return app.getService<SummonRequestService>()
                .listOutgoingRequests(callerId, targetId, count, app.accessToken)
    }

    private fun makeSummonButton() {
        summon_fab.setImageResource(R.drawable.horn)
        summon_fab.setOnClickListener {
            val app = App.getApp(this)
            val accessToken = app.accessToken
            val addRequest = AddSummonRequest(app.user.id, person.id)
            val service = app.getService<SummonRequestService>()
            service.addSummonRequest(addRequest, accessToken)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({request->
                        pendingRequest = request
                        makeCancelButton()
                        Snackbar.make(phone_view, R.string.summon_succeeded, Snackbar.LENGTH_SHORT).show()
                    }, {
                        Snackbar.make(phone_view, R.string.summon_failed, Snackbar.LENGTH_LONG).show()
                    }).addTo(disposable)
        }
    }

    private fun makeCancelButton() {
        summon_fab.setImageResource(R.drawable.cancel)
        summon_fab.setOnClickListener { _ ->
            val service = App.getApp(this).getService<SummonRequestService>()
            pendingRequest?.id?.let { requestId ->
                service.cancelRequest(requestId, App.getApp(this).accessToken)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            makeSummonButton()
                            Snackbar.make(phone_view, R.string.cancel_successful, Snackbar.LENGTH_SHORT).show()
                        }, {
                            Snackbar.make(phone_view, R.string.error_unknown, Snackbar.LENGTH_SHORT).show()
                        })
            }
        }
    }

    companion object {
        const val PERSON = "person"
    }
}