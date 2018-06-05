package employee.summon.asano.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import employee.summon.asano.databinding.SummonRequestBinding
import employee.summon.asano.viewmodel.SummonRequestVM

class SummonRequestAdapter(private val requests: List<SummonRequestVM>, private val click: (r: SummonRequestVM)->Unit) :
        RecyclerView.Adapter<RequestViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SummonRequestBinding.inflate(inflater, parent, false)
        return RequestViewHolder(binding, click)
    }

    override fun getItemCount() = requests.size

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]
        holder.bind(request)
    }
}