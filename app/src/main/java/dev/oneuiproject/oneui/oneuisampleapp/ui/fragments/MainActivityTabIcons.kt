package dev.oneuiproject.oneui.oneuisampleapp.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SectionIndexer
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import dev.oneuiproject.oneui.layout.DrawerLayout
import dev.oneuiproject.oneui.oneuisampleapp.R
import dev.oneuiproject.oneui.oneuisampleapp.databinding.FragmentTabIconsBinding
import dev.oneuiproject.oneui.oneuisampleapp.domain.setCustomOnBackPressedLogic
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale

@AndroidEntryPoint
class MainActivityTabIcons : Fragment() {
    private lateinit var binding: FragmentTabIconsBinding
    private val iconsId: MutableList<Int> = mutableListOf()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var listView: RecyclerView
    private val backPressEnabled = MutableStateFlow(false)
    private var selected = HashMap<Int, Boolean>()
    private var selecting = false
    private var checkAllListening = true

    init {
        val rClass = dev.oneuiproject.oneui.R.drawable::class.java
        for (field in rClass.declaredFields) {
            try {
                iconsId.add(field.getInt(null))
            } catch (e: IllegalAccessException) {
                throw RuntimeException(e)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabIconsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeRefreshLayout.seslSetRefreshOnce(true)
        listView = binding.iconsRecyclerView
        drawerLayout = requireActivity().findViewById(R.id.drawer_layout_main)
        selected = HashMap()
        iconsId.indices.forEach { i -> selected[i] = false }
        listView.layoutManager = LinearLayoutManager(context)
        imageAdapter = ImageAdapter()
        listView.adapter = imageAdapter
        listView.addItemDecoration(ItemDecoration(requireContext()))
        listView.itemAnimator = null
        listView.seslSetIndexTipEnabled(true)
        listView.seslSetFillBottomEnabled(true)
        listView.seslSetLastRoundedCorner(true)
        listView.seslSetFastScrollerEnabled(true)
        listView.seslSetGoToTopEnabled(true)
        listView.seslSetSmoothScrollEnabled(true)
        listView.seslSetLongPressMultiSelectionListener(object : RecyclerView.SeslLongPressMultiSelectionListener {
            override fun onItemSelected(view: RecyclerView, child: View, position: Int, id: Long) {
                if (imageAdapter.getItemViewType(position) == 0) toggleItemSelected(position)
            }

            override fun onLongPressMultiSelectionStarted(x: Int, y: Int) {}
            override fun onLongPressMultiSelectionEnded(x: Int, y: Int) {}
        })
        setCustomOnBackPressedLogic(triggerStateFlow = backPressEnabled, onBackPressedLogic = { if (selecting) setSelecting(false) })
    }

    fun setSelecting(enabled: Boolean) {
        if (enabled) {
            selecting = true
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.itemCount)
            drawerLayout.actionModeBottomMenu.clear()
            drawerLayout.setActionModeMenu(R.menu.menu_select)
            drawerLayout.setActionModeMenuListener { item: MenuItem ->
                Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                setSelecting(false)
                true
            }
            drawerLayout.showActionMode()
            drawerLayout.setActionModeCheckboxListener { _, isChecked ->
                if (checkAllListening) {
                    selected.replaceAll { _, _ -> isChecked }
                    imageAdapter.notifyItemRangeChanged(0, imageAdapter.itemCount)
                }
                val count = selected.values.count { it }
                drawerLayout.setActionModeAllSelector(count, true, count == iconsId.size)
            }
            backPressEnabled.value = true
        } else {
            selecting = false
            for (i in 0 until imageAdapter.itemCount) selected[i] = false
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.itemCount)
            drawerLayout.setActionModeAllSelector(0, true, false)
            drawerLayout.dismissActionMode()
            backPressEnabled.value = false
        }
    }

    fun toggleItemSelected(position: Int) {
        selected[position] = !selected[position]!!
        imageAdapter.notifyItemChanged(position)
        checkAllListening = false
        val count = selected.values.count { it }
        drawerLayout.setActionModeAllSelector(count, true, count == iconsId.size)
        checkAllListening = true
    }

    inner class ImageAdapter internal constructor() : RecyclerView.Adapter<ImageAdapter.ViewHolder>(), SectionIndexer {
        private var sections: MutableList<String> = ArrayList()
        private var positionForSection: MutableList<Int> = ArrayList()
        private var sectionForPosition: MutableList<Int> = ArrayList()

        init {
            for (i in iconsId.indices) {
                var letter = resources.getResourceEntryName(iconsId[i])
                    .replace("ic_oui_", "").substring(0, 1).uppercase(Locale.getDefault())
                if (Character.isDigit(letter[0])) letter = "#"
                if (i == 0 || sections[sections.size - 1] != letter) {
                    sections.add(letter)
                    positionForSection.add(i)
                }
                sectionForPosition.add(sections.size - 1)
            }
        }

        override fun getItemCount(): Int = iconsId.size
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(context).inflate(R.layout.icon_listview_item, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.checkBox.visibility = if (selecting) View.VISIBLE else View.GONE
            holder.checkBox.isChecked = selected[position]!!
            holder.imageView.setImageResource(iconsId[position])
            holder.textView.text = resources.getResourceEntryName(iconsId[position])
            holder.parentView.setOnClickListener {
                if (selecting) toggleItemSelected(position)
                else {
                    Toast.makeText(context, holder.textView.text, Toast.LENGTH_SHORT).show()
                }
            }
            holder.parentView.setOnLongClickListener {
                if (!selecting) setSelecting(true)
                toggleItemSelected(position)
                listView.seslStartLongPressMultiSelection()
                true
            }
        }

        override fun getSections(): Array<Any> = sections.toTypedArray()
        override fun getPositionForSection(sectionIndex: Int): Int = positionForSection[sectionIndex]
        override fun getSectionForPosition(position: Int): Int = sectionForPosition[position]

        inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var imageView: ImageView = itemView.findViewById(R.id.item_icon)
            var textView: TextView = itemView.findViewById(R.id.item_text)
            var checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
            var parentView: LinearLayout = itemView as LinearLayout
        }
    }

    @SuppressLint("PrivateResource")
    private inner class ItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
        private val divider: Drawable

        init {
            val outValue = TypedValue()
            context.theme.resolveAttribute(androidx.appcompat.R.attr.isLightTheme, outValue, true)
            divider = AppCompatResources.getDrawable(context,
                if (outValue.data == 0) androidx.appcompat.R.drawable.sesl_list_divider_dark
                else androidx.appcompat.R.drawable.sesl_list_divider_light
            )!!
        }

        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            super.onDraw(c, parent, state)
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                val top = (child.bottom + (child.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin)
                val bottom = divider.intrinsicHeight + top
                divider.setBounds(parent.left, top, parent.right, bottom)
                divider.draw(c)
            }
        }
    }
}