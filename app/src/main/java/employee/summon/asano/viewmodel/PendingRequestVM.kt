package employee.summon.asano.viewmodel

import employee.summon.asano.model.SummonRequest

class PendingRequestVM {
    var request: SummonRequest? = null
        set(value) { initialized = true; field = value }
    var initialized = false
    fun showSummon() = initialized && request == null
    fun showCancel() = initialized && request != null
}