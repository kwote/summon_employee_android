package employee.summon.asano.rest;

import java.util.List;

import employee.summon.asano.model.AccessToken;
import employee.summon.asano.model.LoginCredentials;
import employee.summon.asano.model.Person;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface PeopleService {
    @GET("people")
    Call<List<Person>> listPeople();

    @POST("people")
    Call<Person> addPerson(@Body Person person);

    @POST("people/login")
    Call<AccessToken> login(@Body LoginCredentials credentials);
}
