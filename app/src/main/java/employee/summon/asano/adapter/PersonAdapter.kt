package employee.summon.asano.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import employee.summon.asano.databinding.PersonBinding
import employee.summon.asano.model.Person

class PersonAdapter(private val people: List<Person>, private val click: (p: Person) -> Unit) :
        RecyclerView.Adapter<PersonViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PersonBinding.inflate(inflater, parent, false)
        return PersonViewHolder(binding, click)
    }

    override fun getItemCount() = people.size

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val person = people[position]
        holder.bind(person)
    }
}