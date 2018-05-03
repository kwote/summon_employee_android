package employee.summon.asano.activity

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.AdapterView
import android.widget.Toast

import employee.summon.asano.App
import employee.summon.asano.adapter.PersonAdapter
import employee.summon.asano.R
import employee.summon.asano.adapter.SummonRequestAdapter
import employee.summon.asano.model.Person
import employee.summon.asano.model.SummonRequest
import employee.summon.asano.rest.PeopleService
import employee.summon.asano.rest.SummonRequestService
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
                reloadIncomingRequests()

                refresh.setOnClickListener { reloadIncomingRequests() }
                clear.text = getString(R.string.clear)
                clear.setOnClickListener { }
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_outgoing -> {
                reloadOutgoingRequests()

                refresh.setOnClickListener { reloadOutgoingRequests() }
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
        val request = parent.adapter.getItem(position) as SummonRequest
        acceptRequest(request)
    }

    private fun summonPerson(person: Person) {
        val intent = Intent(this@MainActivity, CallActivity::class.java)
        intent.putExtra(CallActivity.PERSON, person)
        startActivity(intent)
    }

    private val app: App
        get() = application as App

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        access_token.text = app.accessToken!!.id
        reloadPeople()

        refresh.setOnClickListener { reloadPeople() }
        clear.text = getString(R.string.clear_tokens)
        clear.setOnClickListener { clearTokens() }

        logout.setOnClickListener { performLogout() }

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
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
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
                startActivity(intent)
                finish()
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
                app.stopEventSource()
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
                startActivity(intent)
                finish()
            }
        })
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
                access_token.error = getString(R.string.error_unknown)
            }
        })
    }

    private val requestsCallback = object : Callback<List<SummonRequest>> {
        override fun onResponse(call: Call<List<SummonRequest>>, response: Response<List<SummonRequest>>) {
            val requests = response.body()
            list_view.adapter = SummonRequestAdapter(requests, layoutInflater)
            list_view.onItemClickListener = mOnSummonRequestClickListener
        }

        override fun onFailure(call: Call<List<SummonRequest>>, t: Throwable) {
            access_token.error = getString(R.string.error_unknown)
        }
    }

    private fun reloadIncomingRequests() {
        val service = app.getService<SummonRequestService>()
        val call = service.listIncomingRequests(app.accessToken?.userId)
        call.enqueue(requestsCallback)
    }

    private fun reloadOutgoingRequests() {
        val service = app.getService<SummonRequestService>()
        val call = service.listOutgoingRequests(app.accessToken?.userId)
        call.enqueue(requestsCallback)
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

