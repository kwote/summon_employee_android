package employee.summon.asano.adapter

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import employee.summon.asano.R
import employee.summon.asano.databinding.PersonBinding
import employee.summon.asano.model.Person

class PersonAdapter(private val people: List<Person>, private val inflater: LayoutInflater) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var binding : PersonBinding? = null
        if (convertView != null) {
            binding = DataBindingUtil.getBinding(convertView)
        }
        if (binding == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.person, parent, false)
        }
        binding!!.person = people[position]
        binding.executePendingBindings()
        return binding.root
    }

    override fun getItem(position: Int): Any {
        return people[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return people.size
    }
}