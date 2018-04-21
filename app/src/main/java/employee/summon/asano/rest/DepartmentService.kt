package employee.summon.asano.rest

import employee.summon.asano.model.Department
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface DepartmentService {
    @GET("departments")
    fun listDepartments(): Call<List<Department>>

    @POST("departments")
    fun addDepartment(@Body department: Department): Call<Department>
}
