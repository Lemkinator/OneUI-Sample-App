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
import dagger.hilt.android.AndroidEntryPoint
import dev.oneuiproject.oneui.layout.DrawerLayout
import dev.oneuiproject.oneui.oneuisampleapp.R
import dev.oneuiproject.oneui.oneuisampleapp.databinding.FragmentSearchBinding
import dev.oneuiproject.oneui.oneuisampleapp.domain.GetUserSettingsUseCase
import dev.oneuiproject.oneui.utils.internal.ReflectUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class MainActivitySearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var searchList: MutableList<Any>
    private lateinit var search: String
    private var initListJob: Job? = null

    @Inject
    lateinit var getUserSettings: GetUserSettingsUseCase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListJob?.cancel()
        initListJob = lifecycleScope.launch {
            search = getUserSettings().search
            initList()
        }
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout_main).appBarLayout.addOnOffsetChangedListener { layout: AppBarLayout, verticalOffset: Int ->
            val totalScrollRange = layout.totalScrollRange
            val inputMethodWindowVisibleHeight = ReflectUtils.genericInvokeMethod(
                InputMethodManager::class.java,
                requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE),
                "getInputMethodWindowVisibleHeight"
            ) as Int
            if (totalScrollRange != 0) binding.noEntryView.translationY = (abs(verticalOffset) - totalScrollRange).toFloat() / 2.0f
            else binding.noEntryView.translationY = (abs(verticalOffset) - inputMethodWindowVisibleHeight).toFloat() / 2.0f
        }
    }

    private fun initList() {
        searchList = mutableListOf() //empty list, apply logic here
        if (searchList.isEmpty()) {
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
        binding.searchList.seslSetFastScrollerEnabled(true)
        binding.searchList.seslSetFillBottomEnabled(true)
        binding.searchList.seslSetGoToTopEnabled(true)
        binding.searchList.seslSetLastRoundedCorner(true)
    }

    inner class SearchAdapter : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
        override fun getItemCount(): Int = searchList.size

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getItemViewType(position: Int): Int = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.icon_listview_item, parent, false), viewType)

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            //apply logic here
        }

        inner class ViewHolder internal constructor(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView) {
            var isItem: Boolean = viewType == 0

            init {
                if (isItem) {
                    //apply logic here
                }
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