package employee.summon.asano.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import employee.summon.asano.databinding.PersonBinding
import employee.summon.asano.model.Person
import employee.summon.asano.viewmodel.PersonVM


class PersonViewHolder(private val binding: PersonBinding, private val click: (p: Person?) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {

    fun bind(person: Person) {
        if (binding.person == null) {
            binding.person = PersonVM(person)
        } else {
            binding.person?.person = person
        }
        binding.root.setOnClickListener {v ->
            val binding = DataBindingUtil.findBinding<PersonBinding>(v)
            click(binding?.person?.person)
        }
        binding.executePendingBindings()
    }
}