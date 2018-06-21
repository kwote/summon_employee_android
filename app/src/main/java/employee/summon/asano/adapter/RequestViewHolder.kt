package employee.summon.asano.adapter

import android.support.v7.widget.RecyclerView
import employee.summon.asano.databinding.SummonRequestBinding
import employee.summon.asano.viewmodel.SummonRequestVM


class RequestViewHolder(private val binding: SummonRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

    fun bind(requestVM: SummonRequestVM) {
        binding.request = requestVM
        binding.executePendingBindings()
    }
}