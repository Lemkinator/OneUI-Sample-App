package de.lemke.oneuisampleapp.ui.fragment

import de.lemke.oneuisampleapp.ui.BaseFragment
import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import de.lemke.oneuisampleapp.R
import androidx.appcompat.widget.AppCompatButton
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import dev.oneuiproject.oneui.widget.Toast

class SwipeRefreshFragment : BaseFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val srl = view.findViewById<SwipeRefreshLayout>(R.id.swiperefresh_view)
        val button = view.findViewById<AppCompatButton>(R.id.swiperefresh_button)
        srl.setDistanceToTriggerSync(500)
        srl.setProgressViewOffset(true, 130, 131)
        srl.setOnRefreshListener {
            Toast.makeText(context, "onRefresh", Toast.LENGTH_SHORT).show()
            button.isEnabled = true
        }
        button.seslSetButtonShapeEnabled(true)
        button.backgroundTintList = buttonColor
        button.setOnClickListener {
            button.isEnabled = false
            srl.isRefreshing = false
        }
    }

    override val layoutResId: Int = R.layout.fragment_swiperefresh
    override val iconResId: Int = dev.oneuiproject.oneui.R.drawable.ic_oui_refresh
    override val title: CharSequence = "SwipeRefreshLayout"
    override val isAppBarEnabled: Boolean = false

    private val buttonColor: ColorStateList
        get() {
            val colorPrimaryDark = TypedValue()
            context!!.theme.resolveAttribute(
                androidx.appcompat.R.attr.colorPrimaryDark, colorPrimaryDark, true
            )
            val states = arrayOf(intArrayOf(android.R.attr.state_enabled), intArrayOf(-android.R.attr.state_enabled))
            val colors = intArrayOf(
                Color.argb(
                    0xff,
                    Color.red(colorPrimaryDark.data),
                    Color.green(colorPrimaryDark.data),
                    Color.blue(colorPrimaryDark.data)
                ),
                Color.argb(
                    0x4d,
                    Color.red(colorPrimaryDark.data),
                    Color.green(colorPrimaryDark.data),
                    Color.blue(colorPrimaryDark.data)
                )
            )
            return ColorStateList(states, colors)
        }
}