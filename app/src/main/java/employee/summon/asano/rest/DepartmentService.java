package employee.summon.asano.rest;

import java.util.List;

import employee.summon.asano.model.Department;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface DepartmentService {
    @GET("departments")
    Call<List<Department>> listDepartments();

    @POST("departments")
    Call<Department> addDepartment(@Body Department department);
}
