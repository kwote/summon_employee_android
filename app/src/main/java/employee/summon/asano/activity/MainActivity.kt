package employee.summon.asano.activity

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.AdapterView

import employee.summon.asano.App
import employee.summon.asano.adapters.PersonAdapter
import employee.summon.asano.R
import employee.summon.asano.adapters.SummonRequestAdapter
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
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_summon_requests -> {
                reloadSummonRequests()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private val mOnPersonClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
        val person = parent.adapter.getItem(position)
        summonPerson(person as Person?)
    }

    private val mOnSummonRequestClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
    }

    private fun summonPerson(person: Person?) {
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
        clear_tokens.setOnClickListener { clearTokens() }
        logout.setOnClickListener { performLogout() }

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private fun clearTokens() {
        val peopleService = app.retrofit!!.create<PeopleService>(PeopleService::class.java)
        val accessToken = app.accessToken!!
        val call = peopleService.clearTokens(accessToken.userId, accessToken.id)
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
        val peopleService = app.retrofit!!.create<PeopleService>(PeopleService::class.java)
        val accessToken = app.accessToken!!
        val call = peopleService.logout(accessToken.id)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
                startActivity(intent)
                finish()
            }
        })
    }

    private fun reloadPeople() {
        val peopleService = app.retrofit!!.create<PeopleService>(PeopleService::class.java)
        val call = peopleService.listPeople(1)
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

    private fun reloadSummonRequests() {
        val peopleService = app.retrofit!!.create<SummonRequestService>(SummonRequestService::class.java)
        val call = peopleService.listRequests(app.accessToken?.userId)
        call.enqueue(object : Callback<List<SummonRequest>> {
            override fun onResponse(call: Call<List<SummonRequest>>, response: Response<List<SummonRequest>>) {
                val requests = response.body()
                list_view.adapter = SummonRequestAdapter(requests, layoutInflater)
                list_view.onItemClickListener = mOnSummonRequestClickListener
            }

            override fun onFailure(call: Call<List<SummonRequest>>, t: Throwable) {
                access_token.error = getString(R.string.error_unknown)
            }
        })
    }
}

