package employee.summon.asano.adapter

import android.support.v7.widget.RecyclerView
import employee.summon.asano.databinding.PersonBinding
import employee.summon.asano.model.Person


class PersonViewHolder(private val binding: PersonBinding, private val click: (p: Person) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {

    fun bind(person: Person) {
        binding.person = person
        binding.root.setOnClickListener { click(person) }
        binding.executePendingBindings()
    }
}