package employee.summon.asano.model

import java.util.Date

data class AccessToken(val id: String, val ttl: Int?, val created: Date, val personId: Int?)
