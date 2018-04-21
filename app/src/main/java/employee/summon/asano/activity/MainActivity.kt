package employee.summon.asano.activity

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity

import employee.summon.asano.App
import employee.summon.asano.PersonAdapter
import employee.summon.asano.R
import employee.summon.asano.model.Person
import employee.summon.asano.rest.PeopleService
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                message.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                message.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private val app: App
        get() = application as App

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        access_token.text = app.accessToken!!.id
        reloadPeople()

        refresh.setOnClickListener { reloadPeople() }

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private fun reloadPeople() {
        val peopleService = app.retrofit!!.create<PeopleService>(PeopleService::class.java)
        val call = peopleService.listPeople()
        call.enqueue(object : Callback<List<Person>> {
            override fun onResponse(call: Call<List<Person>>, response: Response<List<Person>>) {
                val people = response.body()
                people_view.adapter = PersonAdapter(people, layoutInflater)
            }

            override fun onFailure(call: Call<List<Person>>, t: Throwable) {
                message.error = getString(R.string.error_unknown)
            }
        })
    }
}

