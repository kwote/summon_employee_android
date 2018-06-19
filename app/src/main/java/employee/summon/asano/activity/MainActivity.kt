package employee.summon.asano.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import employee.summon.asano.*
import employee.summon.asano.adapter.PersonAdapter
import employee.summon.asano.adapter.SummonRequestAdapter
import employee.summon.asano.model.AccessToken
import employee.summon.asano.model.Person
import employee.summon.asano.rest.PeopleService
import employee.summon.asano.viewmodel.SummonRequestVM
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_main.*


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

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }

    private fun openSummonRequest(requestVM: SummonRequestVM) {
        val launchIntent = Intent(this, SummonActivity::class.java)
        launchIntent.putExtra(SummonActivity.IS_INCOMING, requestVM.incoming)
        launchIntent.putExtra(App.REQUEST, requestVM.request)
        startActivity(launchIntent)
    }

    private fun openPerson(person: Person) {
        val intent = Intent(this, PersonActivity::class.java)
        intent.putExtra(PersonActivity.PERSON, person)
        startActivity(intent)
    }

    private val app: App
        get() = application as App

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val accessToken: AccessToken?
        if (intent.hasExtra(App.ACCESS_TOKEN)) {
            accessToken = intent.getParcelableExtra(App.ACCESS_TOKEN)
            prepare(accessToken)
        } else {
            accessToken = readAccessToken()
            if (accessToken == null) {
                login()
                return
            }
            showProgress(true)
            val pingObs = ping(accessToken.id).observeOn(AndroidSchedulers.mainThread())
            pingObs.subscribe({
                if (it) {
                    showProgress(false)
                    prepare(accessToken)
                } else {
                    login()
                }
            }, {
                login()
            }).addTo(disposable)
        }
        recycler_view.layoutManager = LinearLayoutManager(this)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        refresher.setOnRefreshListener {
            reload()
        }
    }

    private fun prepare(accessToken: AccessToken) {
        RequestListenerService.startActionListenRequest(this@MainActivity, accessToken)
        saveAccessToken(accessToken)
        reload()
    }

    private var logout : MenuItem? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        logout = menu.findItem(R.id.logout)
        if (isLoading) {
            logout?.isEnabled = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.logout -> performLogout()
        }
        return super.onOptionsItemSelected(item)
    }

    private var isLoading = false

    private fun showProgress(progress: Boolean) {
        isLoading = progress
        navigation.visibility = if (progress) View.GONE else View.VISIBLE
        logout?.isEnabled = !progress
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
        app.accessToken = accessToken.id
        app.user = accessToken.user
        val sharedPref = getPreferences(Context.MODE_PRIVATE).edit()
        val accessTokenStr = Moshi.Builder().build()
                .adapter(AccessToken::class.java).toJson(accessToken)
        sharedPref.putString(App.ACCESS_TOKEN, accessTokenStr)
        sharedPref.apply()
    }

    private fun readAccessToken(): AccessToken? {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val accessTokenStr = sharedPref.getString(App.ACCESS_TOKEN, "")
        if (TextUtils.isEmpty(accessTokenStr)) {
            return null
        }
        return try {
            Moshi.Builder().build().adapter(AccessToken::class.java).fromJson(accessTokenStr)
        } catch (e: JsonEncodingException) {
            null
        }
    }

    private val disposable = AndroidDisposable()

    private fun performLogout() {
        val service = app.getService<PeopleService>()
        service.logout(app.accessToken).subscribe {
            RequestListenerService.cancelActionListenRequest(this@MainActivity)
            login()
        }.addTo(disposable)
    }

    private fun login() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        startActivity(intent)
        finish()
    }

    private fun ping(accessToken: String) = app.getService<PeopleService>().ping(accessToken)

    private fun reloadPeople() =
        app.getService<PeopleService>()
                .listSummonPeople(app.accessToken)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({people->
                    recycler_view.adapter = PersonAdapter(people) { p ->
                        if (p != null) {
                            openPerson(p)
                        }
                    }
                    refresher.isRefreshing = false
                }, {
                    refresher.isRefreshing = false
                    Snackbar.make(refresher, R.string.reload_failed, Snackbar.LENGTH_SHORT).show()
                })
                .addTo(disposable)

    private fun reloadRequests(incoming: Boolean) {
        val peopleService = app.getService<PeopleService>()
        val accessToken = app.accessToken
        (if (incoming)
            peopleService.listIncomingRequests(app.user.id, accessToken)
        else
            peopleService.listOutgoingRequests(app.user.id, accessToken))
                .map {requests->
                    return@map requests.map {request-> SummonRequestVM(request, incoming) }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {requestsVM->
                            recycler_view.adapter = SummonRequestAdapter(requestsVM) { request-> openSummonRequest(request) }
                            refresher.isRefreshing = false
                        },
                        {
                            refresher.isRefreshing = false
                            Snackbar.make(recycler_view, R.string.error_unknown, Snackbar.LENGTH_LONG).show()
                        })
                .addTo(disposable)
    }
}

