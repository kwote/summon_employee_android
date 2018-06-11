package employee.summon.asano.model

data class AddSummonRequest(val callerId: Int, val targetId: Int,
                         val requested: String, val responded: String? = null,
                         val state: Int = 0, val enabled: Boolean = false)