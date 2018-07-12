package employee.summon.asano.model

data class LoginCredentials(val email: String, val password: String, val ttl: Int=60*60*24*14)
