package employee.summon.asano.activity

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.widget.AdapterView
import android.widget.Toast
import com.google.gson.Gson

import employee.summon.asano.App
import employee.summon.asano.adapter.PersonAdapter
import employee.summon.asano.R
import employee.summon.asano.RequestListenerService
import employee.summon.asano.adapter.SummonRequestAdapter
import employee.summon.asano.model.AccessToken
import employee.summon.asano.model.Person
import employee.summon.asano.model.SummonRequest
import employee.summon.asano.rest.PeopleService
import employee.summon.asano.rest.SummonRequestService
import employee.summon.asano.viewmodel.SummonRequestVM
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_employees -> {
                reloadPeople()

                refresh.setOnClickListener { reloadPeople() }
                clear.text = getString(R.string.clear_tokens)
                clear.setOnClickListener { clearTokens() }
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_incoming -> {
                reloadRequests(true)

                refresh.setOnClickListener { reloadRequests(true) }
                clear.text = getString(R.string.clear)
                clear.setOnClickListener { }
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_outgoing -> {
                reloadRequests(false)

                refresh.setOnClickListener { reloadRequests(false) }
                clear.text = getString(R.string.clear)
                clear.setOnClickListener { }
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private val mOnPersonClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
        val person = parent.adapter.getItem(position) as Person
        summonPerson(person)
    }

    private val mOnSummonRequestClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
        val requestVM = parent.adapter.getItem(position) as SummonRequestVM
        val launchIntent = Intent(this, SummonActivity::class.java)
        launchIntent.putExtra(SummonActivity.IS_INCOMING, requestVM.incoming)
        launchIntent.putExtra(App.REQUEST, requestVM.request)
        launchIntent.putExtra(PersonActivity.PERSON, requestVM.person)
        startActivity(launchIntent)
        //acceptRequest(request)
    }

    private fun summonPerson(person: Person) {
        val intent = Intent(this@MainActivity, PersonActivity::class.java)
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
        } else {
            accessToken = readAccessToken()
            if (accessToken == null) {
                login()
                return
            }
        }
        accessToken?.let { RequestListenerService.startActionListenRequest(this@MainActivity, it) }
        saveAccessToken(accessToken)

        reloadPeople()

        refresh.setOnClickListener { reloadPeople() }
        clear.text = getString(R.string.clear_tokens)
        clear.setOnClickListener { clearTokens() }

        logout.setOnClickListener { performLogout() }

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private fun saveAccessToken(accessToken: AccessToken?) {
        app.accessToken = accessToken
        val sharedPref = getPreferences(Context.MODE_PRIVATE).edit()
        if (accessToken == null) {
            sharedPref.remove(App.ACCESS_TOKEN)
        } else {
            val accessTokenStr = Gson().toJson(accessToken)
            sharedPref.putString(App.ACCESS_TOKEN, accessTokenStr)
        }
        sharedPref.apply()
    }

    private fun readAccessToken(): AccessToken? {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val accessTokenStr = sharedPref.getString(App.ACCESS_TOKEN, "")
        if (TextUtils.isEmpty(accessTokenStr)) {
            return null
        }

        val accessToken = Gson().fromJson<AccessToken>(accessTokenStr, AccessToken::class.java)
        if (accessToken.expired()) {
            return null
        }
        return accessToken
    }

    private fun clearTokens() {
        val service = app.getService<PeopleService>()
        val accessToken = app.accessToken!!
        val call = service.clearTokens(accessToken.userId, app.accessToken!!.id)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                Log.e("MainActivity", "Clear tokens failed", t)
            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                login()
            }
        })
    }

    private fun performLogout() {
        val service = app.getService<PeopleService>()
        val call = service.logout(app.accessToken!!.id)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                saveAccessToken(null)
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

    private fun reloadPeople() {
        val service = app.getService<PeopleService>()
        val call = service.listPeople(1)
        call.enqueue(object : Callback<List<Person>> {
            override fun onResponse(call: Call<List<Person>>, response: Response<List<Person>>) {
                val people = response.body()
                list_view.adapter = PersonAdapter(people, layoutInflater)
                list_view.onItemClickListener = mOnPersonClickListener
            }

            override fun onFailure(call: Call<List<Person>>, t: Throwable) {
            }
        })
    }

    private fun reloadRequests(incoming: Boolean) {
        AsyncTask.execute {
            val requestService = app.getService<SummonRequestService>()
            val call = if (incoming)
                requestService.listIncomingRequests(app.accessToken?.userId)
            else
                requestService.listOutgoingRequests(app.accessToken?.userId)
            val response = call.execute()
            if (response.isSuccessful) {
                val peopleService = app.getService<PeopleService>()
                val requests = response.body()
                val requestVMs : MutableList<SummonRequestVM?> = MutableList(requests.size, { null })
                for ((index, request) in requests.withIndex()) {
                    val pCall = peopleService.getPerson(if (incoming) request.callerId else request.targetId)
                    val pResponse = pCall.execute()
                    if (pResponse.isSuccessful) {
                        val person = pResponse.body()
                        val requestVM = SummonRequestVM(request, person, incoming)
                        requestVMs[index] = requestVM
                    }
                }
                runOnUiThread {
                    list_view.adapter = SummonRequestAdapter(requestVMs, layoutInflater)
                    list_view.onItemClickListener = mOnSummonRequestClickListener
                }
            }
        }
    }

    private fun acceptRequest(request: SummonRequest) {
        val service = app.getService<SummonRequestService>()
        val call = service.acceptRequest(request.id)
        call.enqueue(object : Callback<SummonRequest> {
            override fun onFailure(call: Call<SummonRequest>, t: Throwable) {
                Log.e("MainActivity", "Accept request failed", t)
            }

            override fun onResponse(call: Call<SummonRequest>, response: Response<SummonRequest>) {
                Toast.makeText(this@MainActivity, "Request accepted", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun deleteRequest(request: SummonRequest) {
        val service = app.getService<SummonRequestService>()
        val call = service.deleteRequest(request.id)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                Log.e("MainActivity", "Delete request failed", t)
            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
            }
        })
    }
}

