package de.lemke.oneuisampleapp.ui.fragment

import android.content.Context
import android.graphics.Canvas
import de.lemke.oneuisampleapp.ui.BaseFragment
import dev.oneuiproject.oneui.R.drawable
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import de.lemke.oneuisampleapp.R
import android.widget.SectionIndexer
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.TextView
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import java.lang.RuntimeException
import java.util.*

class IconsFragment : BaseFragment() {
    /*todo search*/
    private val mIconsId: MutableList<Int> = ArrayList()

    init {
        val rClass = drawable::class.java
        for (field in rClass.declaredFields) {
            try {
                mIconsId.add(field.getInt(null))
            } catch (e: IllegalAccessException) {
                throw RuntimeException(e)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val iconListView = getView() as RecyclerView?
        iconListView!!.layoutManager = LinearLayoutManager(context)
        iconListView.adapter = ImageAdapter()
        iconListView.addItemDecoration(ItemDecoration(requireContext()))
        iconListView.itemAnimator = null
        iconListView.seslSetFillBottomEnabled(true)
        iconListView.seslSetLastRoundedCorner(true)
        iconListView.seslSetFastScrollerEnabled(true)
        iconListView.seslSetGoToTopEnabled(true)
        iconListView.seslSetSmoothScrollEnabled(true)
    }

    override val layoutResId: Int = R.layout.fragment_icons
    override val iconResId: Int = drawable.ic_oui_emoticon
    override val title: CharSequence = "Icons"

    inner class ImageAdapter internal constructor() : RecyclerView.Adapter<ImageAdapter.ViewHolder>(), SectionIndexer {
        private var sections: MutableList<String> = ArrayList()
        private var positionForSection: MutableList<Int> = ArrayList()
        private var sectionForPosition: MutableList<Int> = ArrayList()

        init {
            for (i in mIconsId.indices) {
                var letter = resources.getResourceEntryName(mIconsId[i])
                    .replace("ic_oui_", "").substring(0, 1).uppercase(Locale.getDefault())
                if (Character.isDigit(letter[0])) {
                    letter = "#"
                }
                if (i == 0 || sections[sections.size - 1] != letter) {
                    sections.add(letter)
                    positionForSection.add(i)
                }
                sectionForPosition.add(sections.size - 1)
            }
        }

        override fun getItemCount(): Int = mIconsId.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(
                R.layout.view_icon_listview_item, parent, false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.imageView.setImageResource(mIconsId[position])
            holder.textView.text = resources.getResourceEntryName(mIconsId[position])
        }

        override fun getSections(): Array<Any> = sections.toTypedArray()
        override fun getPositionForSection(sectionIndex: Int): Int = positionForSection[sectionIndex]
        override fun getSectionForPosition(position: Int): Int = sectionForPosition[position]

        inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var imageView: ImageView
            var textView: TextView

            init {
                imageView = itemView.findViewById(R.id.icon_list_item_icon)
                textView = itemView.findViewById(R.id.icon_list_item_text)
            }
        }
    }

    private inner class ItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
        private val divider: Drawable

        init {
            val outValue = TypedValue()
            context.theme.resolveAttribute(androidx.appcompat.R.attr.isLightTheme, outValue, true)
            divider = context.getDrawable(
                if (outValue.data == 0) androidx.appcompat.R.drawable.sesl_list_divider_dark
                else androidx.appcompat.R.drawable.sesl_list_divider_light
            )!!
        }

        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            super.onDraw(c, parent, state)
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                val top = (child.bottom
                        + (child.layoutParams as MarginLayoutParams).bottomMargin)
                val bottom = divider.intrinsicHeight + top
                divider.setBounds(parent.left, top, parent.right, bottom)
                divider.draw(c)
            }
        }
    }
}