/*
 * Copyright (c) 2018. $user.name. All rights reserved.
 */

package employee.summon.asano.model

class SummonRequestMessage(val target: Int, private val data: SummonRequest, private val caller: Person,
                           private val callee: Person, val type: String) {
    fun request() = data.copy(caller = caller, target = callee)
}