package employee.summon.asano.model

data class AddSummonRequest(val callerId: Int, val targetId: Int, val comment: String? = null)