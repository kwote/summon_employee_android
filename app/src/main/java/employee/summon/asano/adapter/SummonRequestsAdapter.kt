package employee.summon.asano.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.databinding.DataBindingUtil
import employee.summon.asano.databinding.SummonRequestBinding
import employee.summon.asano.viewmodel.SummonRequestVM

class SummonRequestsAdapter(private val requests: List<SummonRequestVM>, private val click: (r: SummonRequestVM?)->Unit) :
        FilterableAdapter<RequestViewHolder>() {
    var filteredRequests = requests
    inner class ClickHandlers(var context: Context) {
        fun clickAction(v: View) {
            val binding = DataBindingUtil.findBinding<SummonRequestBinding>(v)
            val r = binding?.request
            click(r)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SummonRequestBinding.inflate(inflater, parent, false)
        binding.handlers = ClickHandlers(parent.context)
        return RequestViewHolder(binding)
    }

    override fun getItemCount() = filteredRequests.size

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = filteredRequests[position]
        holder.bind(request)
    }

    private fun filterRequests(constraint: String): List<SummonRequestVM> {
        val filtered = mutableListOf<SummonRequestVM>()
        for (request in requests) {
            val p = request.person
            if (p != null && p.conforms(constraint)) {
                filtered.add(request)
            }
        }
        return filtered
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val result = FilterResults()
                if (constraint.isNullOrEmpty()) {
                    result.values = requests
                } else {
                    result.values = filterRequests(constraint.toString())
                }
                return result
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                @Suppress("UNCHECKED_CAST")
                filteredRequests = results?.values as List<SummonRequestVM>
                this@SummonRequestsAdapter.notifyDataSetChanged()
            }
        }
    }
}