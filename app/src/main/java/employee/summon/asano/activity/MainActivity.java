package employee.summon.asano.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import employee.summon.asano.App;
import employee.summon.asano.BR;
import employee.summon.asano.R;
import employee.summon.asano.databinding.PersonBinding;
import employee.summon.asano.model.Person;
import employee.summon.asano.rest.PeopleService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private TextView mAccessToken;
    private ListView mPeople;
    private Button mRefresh;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = findViewById(R.id.message);
        mAccessToken = findViewById(R.id.access_token);
        mAccessToken.setText(getApp().getAccessToken().getId());
        mPeople = findViewById(R.id.people);
        reloadPeople();

        mRefresh = findViewById(R.id.refresh);
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadPeople();
            }
        });

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void reloadPeople() {
        PeopleService peopleService = getApp().getRetrofit().create(PeopleService.class);
        Call<List<Person>> call = peopleService.listPeople();
        call.enqueue(new Callback<List<Person>>() {
            @Override
            public void onResponse(Call<List<Person>> call, Response<List<Person>> response) {
                final List<Person> people = response.body();
                mPeople.setAdapter(new BaseAdapter() {
                    @Override
                    public int getCount() {
                        return people.size();
                    }

                    @Override
                    public Object getItem(int position) {
                        return people.get(position);
                    }

                    @Override
                    public long getItemId(int position) {
                        return position;
                    }

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        PersonBinding binding = DataBindingUtil.getBinding(convertView);
                        if (binding == null) {
                            binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.person, parent, false);
                        }
                        binding.setPerson(people.get(position));
                        binding.executePendingBindings();

                        return binding.getRoot();
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Person>> call, Throwable t) {
                mTextMessage.setError(getString(R.string.error_unknown));
            }
        });
    }

    private App getApp() {
        return (App) getApplication();
    }
}

