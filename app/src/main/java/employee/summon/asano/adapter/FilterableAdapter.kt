package employee.summon.asano.adapter

import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView

abstract class FilterableAdapter<T : RecyclerView.ViewHolder> : RecyclerView.Adapter<T>(), Filterable