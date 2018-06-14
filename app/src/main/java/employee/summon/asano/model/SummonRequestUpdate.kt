package employee.summon.asano.model

data class SummonRequestUpdate(val request: SummonRequest, val type: UpdateType) {
    enum class UpdateType {
        Accept, Reject, Cancel
    }
}
