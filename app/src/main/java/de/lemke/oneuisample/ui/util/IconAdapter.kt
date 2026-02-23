package de.lemke.oneuisample.ui.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.lemke.oneuisample.R
import de.lemke.oneuisample.ui.util.IconAdapter.Icon
import de.lemke.oneuisample.ui.util.IconAdapter.Payload.HIGHLIGHT
import dev.oneuiproject.oneui.layout.ToolbarLayout.AllSelectorState
import dev.oneuiproject.oneui.recyclerview.adapter.IndexedSelectableListAdapter
import dev.oneuiproject.oneui.utils.SearchHighlighter
import dev.oneuiproject.oneui.widget.SelectableLinearLayout

class IconAdapter(
    context: Context,
    onAllSelectorStateChanged: ((AllSelectorState) -> Unit),
    onBlockActionMode: (() -> Unit),
) : IndexedSelectableListAdapter<Icon, IconAdapter.ViewHolder, Long>(
    indexLabelExtractor = { it: Icon -> it.name },
    onAllSelectorStateChanged = onAllSelectorStateChanged,
    onBlockActionMode = onBlockActionMode,
    selectableIdsProvider = { listItems: List<Icon> -> listItems.map<Icon, Long> { it.id } },
    selectionChangePayload = Payload.SELECTION_MODE,
    diffCallback = object : DiffUtil.ItemCallback<Icon>() {
        override fun areItemsTheSame(oldItem: Icon, newItem: Icon) = oldItem.name == newItem.name
        override fun areContentsTheSame(oldItem: Icon, newItem: Icon) = oldItem == newItem
    }
) {

    private val searchHighlighter = SearchHighlighter(context)

    init {
        setHasStableIds(true)
    }

    var onClickItem: ((Int, Icon, ViewHolder) -> Unit)? = null

    var onLongClickItem: (() -> Unit)? = null

    var highlight = ""
        set(value) {
            if (value != field) {
                field = value
                notifyItemRangeChanged(0, itemCount, HIGHLIGHT)
            }
        }

    fun getItemByPosition(position: Int): Icon = currentList[position]

    override fun getItemId(position: Int) = currentList[position].id

    override fun getItemViewType(position: Int): Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.icon_listview_item, parent, false)
    ).apply {
        itemView.setOnClickListener {
            bindingAdapterPosition.let { onClickItem?.invoke(it, currentList[it], this@apply) }
        }
        itemView.setOnLongClickListener {
            onLongClickItem?.invoke()
            true
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) onBindViewHolder(holder, position)
        else {
            for (payload in payloads.toSet()) {
                when (payload) {
                    Payload.SELECTION_MODE -> holder.bindActionModeAnimate(getItemId(position))
                    HIGHLIGHT -> holder.bindHighlight(currentList[position])
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val iconResId = currentList[position]
        holder.bindIcon(iconResId)
        holder.bindActionMode(getItemId(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var selectableLayout: SelectableLinearLayout? = itemView.findViewById(R.id.listItemSelectableLayout)
        var imageView: ImageView? = itemView.findViewById(R.id.listItemImage)
        var textView: TextView? = itemView.findViewById(R.id.listItemTitle)

        fun bindIcon(icon: Icon) {
            imageView?.setImageResource(icon.resId)
            textView?.text = searchHighlighter(icon.name, highlight)
        }

        fun bindActionMode(itemId: Long) {
            selectableLayout?.apply {
                isSelectionMode = isActionMode
                setSelected(isSelected(itemId))
            }
        }

        fun bindActionModeAnimate(itemId: Long) {
            selectableLayout?.apply {
                isSelectionMode = isActionMode
                setSelectedAnimate(isSelected(itemId))
            }
        }

        fun bindHighlight(icon: Icon) {
            textView?.text = searchHighlighter(icon.name, highlight)
        }
    }

    enum class Payload {
        SELECTION_MODE,
        HIGHLIGHT
    }

    data class Icon(
        val resId: Int,
        val resEntryName: String,
    ) {
        val id get() = resId.toLong()
        val name get() = resEntryName.removePrefix("ic_oui_")
        val beautifiedName get() = name.replace('_', ' ').replaceFirstChar { it.uppercase() }
        val indexChar get() = name.first().uppercaseChar()

        fun containsKeywords(keywords: Set<String>): Boolean {
            return keywords.any { name.contains(it, ignoreCase = true) }
        }
    }
}