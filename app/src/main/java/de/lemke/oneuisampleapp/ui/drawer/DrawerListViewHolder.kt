package de.lemke.oneuisampleapp.ui.drawer

import androidx.recyclerview.widget.RecyclerView
import android.graphics.Typeface
import androidx.appcompat.widget.AppCompatImageView
import android.widget.TextView
import de.lemke.oneuisampleapp.R
import androidx.annotation.DrawableRes
import android.text.TextUtils
import android.view.View

class DrawerListViewHolder(itemView: View, val isSeparator: Boolean) : RecyclerView.ViewHolder(itemView) {
    private var normalTypeface: Typeface? = null
    private var selectedTypeface: Typeface? = null
    private var iconView: AppCompatImageView? = null
    private var titleView: TextView? = null

    init {
        if (!isSeparator) {
            iconView = itemView.findViewById(R.id.drawer_item_icon)
            titleView = itemView.findViewById(R.id.drawer_item_title)
            normalTypeface = Typeface.create("sec-roboto-light", Typeface.NORMAL)
            selectedTypeface = Typeface.create("sec-roboto-light", Typeface.BOLD)
        }
    }

    fun setIcon(@DrawableRes resId: Int) {
        if (!isSeparator) {
            iconView!!.setImageResource(resId)
        }
    }

    fun setTitle(title: CharSequence?) {
        if (!isSeparator) {
            titleView!!.text = title
        }
    }

    fun setSelected(selected: Boolean) {
        if (!isSeparator) {
            itemView.isSelected = selected
            titleView!!.typeface = if (selected) selectedTypeface else normalTypeface
            titleView!!.ellipsize = if (selected) TextUtils.TruncateAt.MARQUEE else TextUtils.TruncateAt.END
        }
    }
}