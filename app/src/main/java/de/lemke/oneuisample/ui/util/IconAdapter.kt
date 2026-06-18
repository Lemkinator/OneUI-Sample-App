/*
 * Copyright 2024-2026 Leonard Lemke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.lemke.oneuisample.ui.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.lemke.oneuisample.NoCoverage
import de.lemke.oneuisample.R
import de.lemke.oneuisample.domain.Icon
import de.lemke.oneuisample.ui.util.IconAdapter.Payload.HIGHLIGHT
import dev.oneuiproject.oneui.layout.ToolbarLayout.AllSelectorState
import dev.oneuiproject.oneui.recyclerview.adapter.IndexedSelectableListAdapter
import dev.oneuiproject.oneui.utils.SearchHighlighter
import dev.oneuiproject.oneui.widget.SelectableLinearLayout

@Suppress("IncorrectFormatting") // ktlint indents super-constructor args at 8 sp; IDE expects 4
class IconAdapter(
    context: Context,
    onAllSelectorStateChanged: ((AllSelectorState) -> Unit),
    onBlockActionMode: (() -> Unit),
) : IndexedSelectableListAdapter<Icon, IconAdapter.ViewHolder, Long>(
        indexLabelExtractor = { icon: Icon -> icon.name },
        onAllSelectorStateChanged = onAllSelectorStateChanged,
        onBlockActionMode = onBlockActionMode,
        selectableIdsProvider = { listItems: List<Icon> -> listItems.map { icon -> icon.id } },
        selectionChangePayload = Payload.SELECTION_MODE,
        diffCallback =
            object : DiffUtil.ItemCallback<Icon>() {
                override fun areItemsTheSame(
                    oldItem: Icon,
                    newItem: Icon,
                ) = oldItem.name == newItem.name

                override fun areContentsTheSame(
                    oldItem: Icon,
                    newItem: Icon,
                ) = oldItem == newItem
            },
    ) {
    private val searchHighlighter = SearchHighlighter(context)

    init {
        setHasStableIds(true)
    }

    var onClickItem: (Int, Icon, ViewHolder) -> Unit = { _, _, _ -> }

    var onLongClickItem: () -> Unit = {}

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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.icon_listview_item, parent, false),
        ).apply {
            itemView.setOnClickListener(
                object : View.OnClickListener {
                    override fun onClick(v: View) {
                        val position = bindingAdapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            onClickItem(position, currentList[position], this@apply)
                        }
                    }
                },
            )
            itemView.setOnLongClickListener(
                object : View.OnLongClickListener {
                    override fun onLongClick(v: View): Boolean {
                        if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                            onLongClickItem()
                        }
                        return true
                    }
                },
            )
        }

    @NoCoverage
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            for (payload in payloads.toSet()) {
                when (payload) {
                    Payload.SELECTION_MODE -> {
                        holder.bindActionModeAnimate(getItemId(position))
                    }

                    HIGHLIGHT -> {
                        holder.bindHighlight(currentList[position])
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val iconResId = currentList[position]
        holder.bindIcon(iconResId)
        holder.bindActionMode(getItemId(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val selectableLayout: SelectableLinearLayout = itemView.findViewById(R.id.listItemSelectableLayout)
        val imageView: ImageView = itemView.findViewById(R.id.listItemImage)
        val textView: TextView = itemView.findViewById(R.id.listItemTitle)

        fun bindIcon(icon: Icon) {
            imageView.setImageResource(icon.resId)
            textView.text = searchHighlighter(icon.name, highlight)
        }

        fun bindActionMode(itemId: Long) {
            selectableLayout.apply {
                isSelectionMode = isActionMode
                setSelected(isSelected(itemId))
            }
        }

        fun bindActionModeAnimate(itemId: Long) {
            selectableLayout.apply {
                isSelectionMode = isActionMode
                setSelectedAnimate(isSelected(itemId))
            }
        }

        fun bindHighlight(icon: Icon) {
            textView.text = searchHighlighter(icon.name, highlight)
        }
    }

    enum class Payload {
        SELECTION_MODE,
        HIGHLIGHT,
    }
}
