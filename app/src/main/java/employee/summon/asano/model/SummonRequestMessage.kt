package employee.summon.asano.model

class SummonRequestMessage(val target: Int, private val data: SummonRequest, private val caller: Person,
                           private val callee: Person, val type: String) {
    fun request() = SummonRequest(data.id, data.callerId, data.targetId, data.requested, data.responded,
                data.state, data.enabled, caller, callee)
}