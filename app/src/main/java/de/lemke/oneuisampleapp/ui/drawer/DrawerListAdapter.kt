package de.lemke.oneuisampleapp.ui.drawer

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import de.lemke.oneuisampleapp.R
import de.lemke.oneuisampleapp.ui.FragmentInfo

class DrawerListAdapter(private val context: Context, private val fragments: List<Fragment?>, private val listener: DrawerListener?) :
    RecyclerView.Adapter<DrawerListViewHolder>() {
    private var selectedPos = 0

    interface DrawerListener {
        fun onDrawerItemSelected(position: Int): Boolean
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawerListViewHolder {
        val inflater = LayoutInflater.from(context)
        val isSeparator = viewType == 0
        val view: View = if (isSeparator) inflater.inflate(R.layout.view_drawer_list_separator, parent, false)
        else inflater.inflate(R.layout.view_drawer_list_item, parent, false)
        return DrawerListViewHolder(view, isSeparator)
    }

    override fun onBindViewHolder(holder: DrawerListViewHolder, position: Int) {
        if (!holder.isSeparator) {
            val fragment = fragments[position]
            if (fragment is FragmentInfo) {
                holder.setIcon((fragment as FragmentInfo).iconResId)
                holder.setTitle((fragment as FragmentInfo).title)
            }
            holder.setSelected(position == selectedPos)
            holder.itemView.setOnClickListener {
                val itemPos = holder.bindingAdapterPosition
                var result = false
                if (listener != null) result = listener.onDrawerItemSelected(itemPos)
                if (result) setSelectedItem(itemPos)
            }
        }
    }

    override fun getItemCount(): Int = fragments.size

    override fun getItemViewType(position: Int): Int = if (fragments[position] == null) 0 else 1

    private fun setSelectedItem(position: Int) {
        selectedPos = position
        notifyItemRangeChanged(0, itemCount)
    }
}