package employee.summon.asano.activity

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import com.google.gson.Gson
import employee.summon.asano.App
import employee.summon.asano.R
import employee.summon.asano.RequestListenerService
import employee.summon.asano.adapter.PersonAdapter
import employee.summon.asano.adapter.SummonRequestAdapter
import employee.summon.asano.api.PeopleService
import employee.summon.asano.model.AccessToken
import employee.summon.asano.model.Person
import employee.summon.asano.rest.IPeopleService
import employee.summon.asano.rest.SummonRequestService
import employee.summon.asano.rest.UtilService
import employee.summon.asano.viewmodel.SummonRequestVM
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private enum class SelectedItem {
        People,
        IncomingRequests,
        OutgoingRequests
    }

    private var selectedItem: SelectedItem = SelectedItem.People

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_employees -> {
                selectedItem = SelectedItem.People
                reload()

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_incoming -> {
                selectedItem = SelectedItem.IncomingRequests
                reload()

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_outgoing -> {
                selectedItem = SelectedItem.OutgoingRequests
                reload()

                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun openSummonRequest(requestVM: SummonRequestVM) {
        val launchIntent = Intent(this, SummonActivity::class.java)
        launchIntent.putExtra(SummonActivity.IS_INCOMING, requestVM.incoming)
        launchIntent.putExtra(App.REQUEST, requestVM.request)
        launchIntent.putExtra(PersonActivity.PERSON, requestVM.person)
        startActivity(launchIntent)
    }

    private fun summonPerson(person: Person) {
        val intent = Intent(this, PersonActivity::class.java)
        intent.putExtra(PersonActivity.PERSON, person)
        startActivity(intent)
    }

    private val app: App
        get() = application as App

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val accessToken : AccessToken?
        if (intent.hasExtra(App.ACCESS_TOKEN)) {
            accessToken = intent.getParcelableExtra(App.ACCESS_TOKEN)
            accessToken?.let { RequestListenerService.startActionListenRequest(this@MainActivity, it) }
            saveAccessToken(accessToken)

            reload()
        } else {
            accessToken = readAccessToken()
            if (accessToken == null) {
                login()
                return
            }
            showProgress(true)
            ping(accessToken, {
                app.accessToken = accessToken
                showProgress(false)
                reload()
            }, {_, _->
                login()
            })
        }
        recycler_view.layoutManager = LinearLayoutManager(this)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        refresher.setOnRefreshListener {
            reload()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.logout -> performLogout()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showProgress(progress: Boolean) {

    }

    private fun reload() {
        if (!refresher.isRefreshing) {
            refresher.isRefreshing = true
        }
        when (selectedItem) {
            SelectedItem.People -> reloadPeople()
            SelectedItem.IncomingRequests -> reloadRequests(true)
            SelectedItem.OutgoingRequests -> reloadRequests(false)
        }
    }

    private fun saveAccessToken(accessToken: AccessToken) {
        app.accessToken = accessToken
        val sharedPref = getPreferences(Context.MODE_PRIVATE).edit()
        val accessTokenStr = Gson().toJson(accessToken)
        sharedPref.putString(App.ACCESS_TOKEN, accessTokenStr)
        sharedPref.apply()
    }

    private fun readAccessToken(): AccessToken? {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val accessTokenStr = sharedPref.getString(App.ACCESS_TOKEN, "")
        if (TextUtils.isEmpty(accessTokenStr)) {
            return null
        }

        return Gson().fromJson(accessTokenStr, AccessToken::class.java)
    }

    private fun performLogout() {
        val service = app.getService<IPeopleService>()
        val call = service.logout(app.accessToken.id)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                RequestListenerService.cancelActionListenRequest(this@MainActivity)
                login()
            }
        })
    }

    private fun login() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        startActivity(intent)
        finish()
    }

    private fun ping(accessToken: AccessToken?, onSuccess: (r: Boolean)->Unit,
                     onFail: (r: Response<Boolean>?, t: Throwable?)->Unit) {
        val service = app.getService<UtilService>()
        val call = service.ping(accessToken!!.id)
        call.enqueue(object : Callback<Boolean> {
            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                onFail(null, t)
            }

            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if (response.isSuccessful) {
                    onSuccess(response.body())
                } else {
                    onFail(response, null)
                }
            }
        })
    }

    private fun reloadPeople() {
        val service = PeopleService(app.getService())
        service.listPeople(app.accessToken.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( {
                    recycler_view.adapter = PersonAdapter(it, { summonPerson(it) })
                }, {
                    refresher.isRefreshing = false
                    Snackbar.make(refresher, R.string.reload_failed, Snackbar.LENGTH_SHORT).show()
                }, {
                    refresher.isRefreshing = false
                })
    }

    private fun reloadRequests(incoming: Boolean) {
        AsyncTask.execute {
            val requestService = app.getService<SummonRequestService>()
            val accessToken = app.accessToken
            val call = if (incoming)
                requestService.listIncomingRequests(accessToken.userId, accessToken.id)
            else
                requestService.listOutgoingRequests(accessToken.userId, accessToken.id)
            try {
                val response = call.execute()
                val success : Boolean
                val requestVMs: MutableList<SummonRequestVM?>
                if (response.isSuccessful) {
                    success = true
                    val peopleService = app.getService<IPeopleService>()
                    val requests = response.body()
                    requestVMs = MutableList(requests.size, { null })
                    for ((index, request) in requests.withIndex()) {
                        val pCall = peopleService.getPerson(
                                if (incoming) request.callerId else request.targetId,
                                accessToken.id
                        )
                        val pResponse = pCall.execute()
                        if (pResponse.isSuccessful) {
                            val person = pResponse.body()
                            val requestVM = SummonRequestVM(request, person, incoming)
                            requestVMs[index] = requestVM
                        }
                    }
                } else {
                    success = false
                    requestVMs = MutableList(0, { null })
                }
                runOnUiThread {
                    refresher.isRefreshing = false
                    recycler_view.adapter = SummonRequestAdapter(requestVMs.filterNotNull(), { openSummonRequest(it) })
                    if (!success)
                        Snackbar.make(recycler_view, R.string.error_unknown, Snackbar.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                Snackbar.make(recycler_view, R.string.error_unknown, Snackbar.LENGTH_LONG).show()
                runOnUiThread {
                    refresher.isRefreshing = false
                }
            }
        }
    }
}

