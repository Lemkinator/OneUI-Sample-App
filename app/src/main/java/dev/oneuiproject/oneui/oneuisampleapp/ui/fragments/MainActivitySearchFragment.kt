package dev.oneuiproject.oneui.oneuisampleapp.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SectionIndexer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint
import dev.oneuiproject.oneui.layout.DrawerLayout
import dev.oneuiproject.oneui.oneuisampleapp.R
import dev.oneuiproject.oneui.oneuisampleapp.databinding.FragmentSearchBinding
import dev.oneuiproject.oneui.oneuisampleapp.domain.GetUserSettingsUseCase
import dev.oneuiproject.oneui.oneuisampleapp.domain.MakeSectionOfTextBoldUseCase
import dev.oneuiproject.oneui.oneuisampleapp.ui.OnDataChangedListener
import dev.oneuiproject.oneui.utils.internal.ReflectUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class MainActivitySearchFragment : Fragment(), OnDataChangedListener {
    private lateinit var binding: FragmentSearchBinding
    private val iconsId: MutableList<Int> = mutableListOf()
    private lateinit var searchIconList: MutableList<Int>
    private lateinit var search: String
    private lateinit var searchKeyWords: Set<String>
    private var initListJob: Job? = null
    private var onOffsetChangedListener: AppBarLayout.OnOffsetChangedListener? = null

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    @Inject
    lateinit var makeSectionOfTextBold: MakeSectionOfTextBoldUseCase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initIcons()
        onDataChanged()
        onOffsetChangedListener = AppBarLayout.OnOffsetChangedListener { layout: AppBarLayout, verticalOffset: Int ->
            val totalScrollRange = layout.totalScrollRange
            val inputMethodWindowVisibleHeight = ReflectUtils.genericInvokeMethod(
                InputMethodManager::class.java,
                requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE),
                "getInputMethodWindowVisibleHeight"
            ) as Int
            if (totalScrollRange != 0) binding.noEntryView.translationY = (abs(verticalOffset) - totalScrollRange).toFloat() / 2.0f
            else binding.noEntryView.translationY = (abs(verticalOffset) - inputMethodWindowVisibleHeight).toFloat() / 2.0f
        }
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout_main).appBarLayout
            .addOnOffsetChangedListener(onOffsetChangedListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout_main).appBarLayout
            .removeOnOffsetChangedListener(onOffsetChangedListener)
    }

    private fun initIcons() {
        val rClass = dev.oneuiproject.oneui.R.drawable::class.java
        for (field in rClass.declaredFields) {
            try {
                iconsId.add(field.getInt(null))
            } catch (e: IllegalAccessException) {
                throw RuntimeException(e)
            }
        }
    }

    override fun onDataChanged() {
        initListJob?.cancel()
        initListJob = lifecycleScope.launch {
            search = getUserSettings().search
            searchKeyWords = search.trim().split(" ").toSet()
            initList()
        }
    }

    private fun iconContainsKeywords(iconId: Int): Boolean {
        val name = resources.getResourceEntryName(iconId)
        for (keyword in searchKeyWords) {
            if (!name.contains(keyword, true)) return false
        }
        return true
    }

    private fun initList() {
        searchIconList = mutableListOf()
        if (search.isNotBlank()) {
            searchIconList = iconsId.filter { iconContainsKeywords(it) }.toMutableList()
        }
        if (searchIconList.isEmpty()) {
            binding.searchList.visibility = View.GONE
            binding.noEntryLottie.cancelAnimation()
            binding.noEntryLottie.progress = 0f
            binding.noEntryScrollView.visibility = View.VISIBLE
            binding.noEntryLottie.addValueCallback(
                KeyPath("**"),
                LottieProperty.COLOR_FILTER,
                LottieValueCallback(SimpleColorFilter(requireContext().getColor(R.color.primary_color_themed)))
            )
            binding.noEntryLottie.postDelayed({ binding.noEntryLottie.playAnimation() }, 400)
        } else {
            binding.noEntryScrollView.visibility = View.GONE
            binding.searchList.visibility = View.VISIBLE
        }
        binding.searchList.adapter = SearchAdapter()
        binding.searchList.layoutManager = LinearLayoutManager(context)
        binding.searchList.addItemDecoration(ItemDecoration(requireContext()))
        binding.searchList.itemAnimator = null
        binding.searchList.seslSetIndexTipEnabled(true)
        binding.searchList.seslSetFastScrollerEnabled(true)
        binding.searchList.seslSetFillBottomEnabled(true)
        binding.searchList.seslSetGoToTopEnabled(true)
        binding.searchList.seslSetLastRoundedCorner(true)
        binding.searchList.seslSetSmoothScrollEnabled(true)
    }

    inner class SearchAdapter : RecyclerView.Adapter<SearchAdapter.ViewHolder>(), SectionIndexer {
        private var sections: MutableList<String> = ArrayList()
        private var positionForSection: MutableList<Int> = ArrayList()
        private var sectionForPosition: MutableList<Int> = ArrayList()

        init {
            for (i in searchIconList.indices) {
                var letter = resources.getResourceEntryName(searchIconList[i])
                    .replace("ic_oui_", "").substring(0, 1).uppercase(Locale.getDefault())
                if (Character.isDigit(letter[0])) letter = "#"
                if (i == 0 || sections[sections.size - 1] != letter) {
                    sections.add(letter)
                    positionForSection.add(i)
                }
                sectionForPosition.add(sections.size - 1)
            }
        }

        override fun getSections(): Array<Any> = sections.toTypedArray()
        override fun getPositionForSection(sectionIndex: Int): Int = positionForSection.getOrNull(sectionIndex) ?: 0
        override fun getSectionForPosition(position: Int): Int = sectionForPosition.getOrNull(position) ?: 0
        override fun getItemCount(): Int = searchIconList.size

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getItemViewType(position: Int): Int = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.icon_listview_item, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.imageView.setImageResource(searchIconList[position])
            val color = MaterialColors.getColor(
                requireContext(),
                androidx.appcompat.R.attr.colorPrimary,
                requireContext().getColor(R.color.primary_color_themed)
            )
            holder.textView.text = makeSectionOfTextBold(resources.getResourceEntryName(searchIconList[position]), search, color)
        }

        inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var imageView: ImageView
            var textView: TextView
            private var parentView: LinearLayout

            init {
                parentView = itemView as LinearLayout
                imageView = itemView.findViewById(R.id.item_icon)
                textView = itemView.findViewById(R.id.item_text)
            }
        }
    }

    private class ItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
        private val divider: Drawable
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

        init {
            val outValue = TypedValue()
            context.theme.resolveAttribute(androidx.appcompat.R.attr.isLightTheme, outValue, true)
            divider = context.getDrawable(
                if (outValue.data == 0) androidx.appcompat.R.drawable.sesl_list_divider_dark
                else androidx.appcompat.R.drawable.sesl_list_divider_light
            )!!
        }
    }
}