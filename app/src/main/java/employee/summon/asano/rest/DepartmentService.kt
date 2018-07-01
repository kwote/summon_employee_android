package employee.summon.asano.rest

import employee.summon.asano.model.Department
import retrofit2.Call
import retrofit2.http.GET

interface DepartmentService {
    @GET("departments")
    fun listDepartments(): Call<List<Department>>
}
