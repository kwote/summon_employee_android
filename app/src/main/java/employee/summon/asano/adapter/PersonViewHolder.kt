package employee.summon.asano.adapter

import android.support.v7.widget.RecyclerView
import employee.summon.asano.databinding.PersonBinding
import employee.summon.asano.viewmodel.PersonVM

class PersonViewHolder(private val binding: PersonBinding) :
        RecyclerView.ViewHolder(binding.root) {

    fun bind(person: PersonVM) {
        binding.person = person
        binding.executePendingBindings()
    }
}