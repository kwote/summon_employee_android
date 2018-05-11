package employee.summon.asano.viewmodel

import employee.summon.asano.model.Person
import employee.summon.asano.model.RequestStatus
import employee.summon.asano.model.SummonRequest
import java.util.*

data class SummonRequestVM(val request: SummonRequest, val person: Person, val incoming: Boolean) {
    val enabled: Boolean
        get() = request.enabled
    val personName: String
        get() = person.fullName
    val requested: Date?
        get() = request.requested
    val responded: Date?
        get() = request.responded
    val accepted: Boolean
        get() = request.status == RequestStatus.Accepted.code
    val rejected: Boolean
        get() = request.status == RequestStatus.Rejected.code
}