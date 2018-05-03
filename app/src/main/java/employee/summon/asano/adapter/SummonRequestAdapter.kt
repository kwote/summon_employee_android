package employee.summon.asano.adapter

import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import employee.summon.asano.R
import employee.summon.asano.databinding.SummonRequestBinding
import employee.summon.asano.model.SummonRequest

class SummonRequestAdapter(private val requests: List<SummonRequest>, private val inflater: LayoutInflater) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var binding : SummonRequestBinding? = null
        if (convertView != null) {
            binding = DataBindingUtil.getBinding(convertView)
        }
        if (binding == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.summon_request, parent, false)
        }
        binding!!.request = requests[position]
        binding.executePendingBindings()
        return binding.root
    }

    override fun getItem(position: Int): Any {
        return requests[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return requests.size
    }
}