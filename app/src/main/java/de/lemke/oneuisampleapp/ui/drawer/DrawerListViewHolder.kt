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
    private var mNormalTypeface: Typeface? = null
    private var mSelectedTypeface: Typeface? = null
    private var mIconView: AppCompatImageView? = null
    private var mTitleView: TextView? = null

    init {
        if (!isSeparator) {
            mIconView = itemView.findViewById(R.id.drawer_item_icon)
            mTitleView = itemView.findViewById(R.id.drawer_item_title)
            mNormalTypeface = Typeface.create("sec-roboto-light", Typeface.NORMAL)
            mSelectedTypeface = Typeface.create("sec-roboto-light", Typeface.BOLD)
        }
    }

    fun setIcon(@DrawableRes resId: Int) {
        if (!isSeparator) {
            mIconView!!.setImageResource(resId)
        }
    }

    fun setTitle(title: CharSequence?) {
        if (!isSeparator) {
            mTitleView!!.text = title
        }
    }

    fun setSelected(selected: Boolean) {
        if (!isSeparator) {
            itemView.isSelected = selected
            mTitleView!!.typeface = if (selected) mSelectedTypeface else mNormalTypeface
            mTitleView!!.ellipsize = if (selected) TextUtils.TruncateAt.MARQUEE else TextUtils.TruncateAt.END
        }
    }
}