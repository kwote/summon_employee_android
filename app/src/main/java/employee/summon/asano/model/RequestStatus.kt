package employee.summon.asano.model

enum class RequestStatus(val code: Int) {
    Pending(0),
    Accepted(1),
    Rejected(2)
}