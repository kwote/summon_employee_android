package employee.summon.asano.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import employee.summon.asano.*
import employee.summon.asano.adapter.FilterableAdapter
import employee.summon.asano.adapter.PeopleAdapter
import employee.summon.asano.adapter.SummonRequestsAdapter
import employee.summon.asano.model.AccessToken
import employee.summon.asano.model.Person
import employee.summon.asano.rest.PeopleService
import employee.summon.asano.viewmodel.PersonVM
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val accessToken: AccessToken?
        if (intent.hasExtra(App.ACCESS_TOKEN)) {
            accessToken = intent.getParcelableExtra(App.ACCESS_TOKEN)
            initialized = true
            prepare(accessToken)
            reload()
        } else {
            accessToken = readAccessToken()
            if (accessToken == null) {
                login()
                return
            }
            initialized = false
            val pingObs = ping(accessToken.id).observeOn(AndroidSchedulers.mainThread())
            pingObs.subscribe({
                if (it) {
                    initialized = true
                    prepare(accessToken)
                    reload()
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

    override fun onResume() {
        super.onResume()
        reload()
    }

    private fun prepare(accessToken: AccessToken) {
        if (!listening) {
            RequestListenerService.startActionListenRequest(this, accessToken.id, accessToken.userId)
            listening = true
            connect?.isVisible = false
            disconnect?.isVisible = true
        }
        saveAccessToken(accessToken)
    }

    private var logout: MenuItem? = null
    private var connect: MenuItem? = null
    private var disconnect: MenuItem? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        logout = menu.findItem(R.id.logout)
        connect = menu.findItem(R.id.connect)
        disconnect = menu.findItem(R.id.disconnect)
        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (recycler_view.adapter is FilterableAdapter) {
                    (recycler_view.adapter as FilterableAdapter).filter.filter(newText)
                }
                return true
            }
        })
        if (!initialized) {
            logout?.isEnabled = false
            connect?.isEnabled = false
            disconnect?.isEnabled = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.logout -> performLogout()
            R.id.connect -> toggleConnection(true)
            R.id.disconnect -> toggleConnection(false)
        }
        return super.onOptionsItemSelected(item)
    }

    private var listening = false

    private fun toggleConnection(toggle: Boolean) {
        if (toggle == listening) return
        if (!listening) {
            val app = App.getApp(this)
            val accessToken = app.accessToken
            val userId = app.user.id
            RequestListenerService.startActionListenRequest(this, accessToken, userId)
        } else {
            RequestListenerService.cancelActionListenRequest(this)
        }
        connect?.isVisible = !toggle
        disconnect?.isVisible = toggle

        listening = toggle
    }

    private var initialized = false
        set (value) {
            field = value
            navigation.visibility = if (value) View.VISIBLE else View.GONE
            logout?.isEnabled = value
            connect?.isEnabled = value
            disconnect?.isEnabled = value
        }

    private fun reload() {
        if (!initialized) return
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
        val app = App.getApp(this)
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
        val app = App.getApp(this)
        val service = app.getService<PeopleService>()
        RequestListenerService.cancelActionListenRequest(this)
        service.logout(app.accessToken).subscribe {
            login()
        }.addTo(disposable)
    }

    private fun login() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        startActivity(intent)
        finish()
    }

    private fun ping(accessToken: String) = App.getApp(this).getService<PeopleService>().ping(accessToken)

    private fun reloadPeople() {
        val app = App.getApp(this)
        app.getService<PeopleService>()
                .listSummonPeople(app.accessToken)
                .map { people ->
                    return@map people.map { PersonVM(it) }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ people ->
                    recycler_view.adapter = PeopleAdapter(people) { p ->
                        p?.person?.let { openPerson(it) }
                    }
                    refresher.isRefreshing = false
                }, {
                    refresher.isRefreshing = false
                    Snackbar.make(refresher, R.string.reload_failed, Snackbar.LENGTH_SHORT).show()
                })
                .addTo(disposable)
    }

    private fun reloadRequests(incoming: Boolean) {
        val app = App.getApp(this)
        val peopleService = app.getService<PeopleService>()
        val accessToken = app.accessToken
        (if (incoming)
            peopleService.listIncomingRequests(app.user.id, accessToken)
        else
            peopleService.listOutgoingRequests(app.user.id, accessToken))
                .map { requests ->
                    return@map requests.map { request -> SummonRequestVM(request, incoming) }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { requestsVM ->
                            recycler_view.adapter = SummonRequestsAdapter(requestsVM) { request ->
                                request?.let { openSummonRequest(it) }
                            }
                            refresher.isRefreshing = false
                        },
                        {
                            refresher.isRefreshing = false
                            Snackbar.make(recycler_view, R.string.error_unknown, Snackbar.LENGTH_LONG).show()
                        })
                .addTo(disposable)
    }
}

