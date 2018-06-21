package employee.summon.asano.adapter

import android.support.v7.widget.RecyclerView
import android.widget.Filterable

abstract class FilterableAdapter<T : RecyclerView.ViewHolder> : RecyclerView.Adapter<T>(), Filterable