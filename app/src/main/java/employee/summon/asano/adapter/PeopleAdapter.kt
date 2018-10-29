package employee.summon.asano.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.databinding.DataBindingUtil
import employee.summon.asano.databinding.PersonBinding
import employee.summon.asano.viewmodel.PersonVM

class PeopleAdapter(private val people: List<PersonVM>, private val click: (p: PersonVM?) -> Unit) :
        FilterableAdapter<PersonViewHolder>() {
    var filteredPeople = people

    inner class ClickHandlers(var context: Context) {
        fun clickAction(v: View) {
            val binding = DataBindingUtil.findBinding<PersonBinding>(v)
            val p = binding?.person
            click(p)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PersonBinding.inflate(inflater, parent, false)
        binding.handlers = ClickHandlers(parent.context)
        return PersonViewHolder(binding)
    }

    override fun getItemCount() = filteredPeople.size

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val person = filteredPeople[position]
        holder.bind(person)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val result = FilterResults()
                if (constraint.isNullOrEmpty()) {
                    result.values = people
                } else {
                    result.values = filterPeople(constraint.toString())
                }
                return result
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                @Suppress("UNCHECKED_CAST")
                filteredPeople = results?.values as List<PersonVM>
                this@PeopleAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun filterPeople(constraint: String): List<PersonVM> {
        val filtered = mutableListOf<PersonVM>()
        for (person in people) {
            if (person.conforms(constraint)) {
                filtered.add(person)
            }
        }
        return filtered
    }
}

fun PersonVM.conforms(constraint: String): Boolean {
    return person.firstname.contains(constraint, true) ||
            person.lastname.contains(constraint, true) ||
            fullName().contains(constraint, true)
}
