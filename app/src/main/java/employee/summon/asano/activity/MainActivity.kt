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

        val fullToken: AccessToken?
        if (intent.hasExtra(App.ACCESS_TOKEN)) {
            fullToken = intent.getParcelableExtra(App.ACCESS_TOKEN)
            fullToken?.let { RequestListenerService.startActionListenRequest(this@MainActivity, it) }
            saveAccessToken(fullToken)

            reload()
        } else {
            val accessToken = readAccessToken()
            if (accessToken == null) {
                login()
                return
            }
            showProgress(true)
            ping(accessToken).observeOn(AndroidSchedulers.mainThread()).subscribe( {
                if (it) {
                    app.accessToken = accessToken
                    reload()
                } else {
                    login()
                }
            }, {
                login()
            }, {
                showProgress(false)
            }).addTo(disposable)
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
        app.accessToken = accessToken.id
        app.user = accessToken.user
        val sharedPref = getPreferences(Context.MODE_PRIVATE).edit()
        sharedPref.putString(App.ACCESS_TOKEN, accessToken.id)
        sharedPref.apply()
    }

    private fun readAccessToken(): String? {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val accessToken = sharedPref.getString(App.ACCESS_TOKEN, "")
        if (TextUtils.isEmpty(accessToken)) {
            return null
        }

        return accessToken
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

    private fun reloadPeople() {
        val service = app.getService<PeopleService>()
        service.listPeople(app.accessToken)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    recycler_view.adapter = PersonAdapter(it, { summonPerson(it) })
                }, {
                    refresher.isRefreshing = false
                    Snackbar.make(refresher, R.string.reload_failed, Snackbar.LENGTH_SHORT).show()
                }, {
                    refresher.isRefreshing = false
                })
                .addTo(disposable)
    }

    private fun reloadRequests(incoming: Boolean) {
        val peopleService = app.getService<PeopleService>()
        val accessToken = app.accessToken
        if (incoming)
            peopleService.listIncomingRequests(app.user.id, accessToken)
        else
            peopleService.listOutgoingRequests(app.user.id, accessToken)
                    .map {
                        return@map it.map { SummonRequestVM(it, incoming) }
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                recycler_view.adapter = SummonRequestAdapter(it, { openSummonRequest(it) })
                            },
                            {
                                Snackbar.make(recycler_view, R.string.error_unknown, Snackbar.LENGTH_LONG).show()
                            },
                            {
                                refresher.isRefreshing = false
                            })
                    .addTo(disposable)
    }
}

