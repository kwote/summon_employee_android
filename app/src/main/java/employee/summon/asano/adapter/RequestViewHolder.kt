package employee.summon.asano.adapter

import android.support.v7.widget.RecyclerView
import employee.summon.asano.databinding.SummonRequestBinding
import employee.summon.asano.viewmodel.SummonRequestVM


class RequestViewHolder(private val binding: SummonRequestBinding, private val click: (r: SummonRequestVM)->Unit) :
        RecyclerView.ViewHolder(binding.root) {

    fun bind(requestVM: SummonRequestVM) {
        binding.requestVM = requestVM
        binding.root.setOnClickListener { click(requestVM) }
        binding.executePendingBindings()
    }
}