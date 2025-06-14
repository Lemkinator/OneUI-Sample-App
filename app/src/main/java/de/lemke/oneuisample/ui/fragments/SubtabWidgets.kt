package de.lemke.oneuisample.ui.fragments

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context.SEARCH_SERVICE
import android.graphics.ColorFilter
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieProperty.COLOR_FILTER
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import dagger.hilt.android.AndroidEntryPoint
import dev.oneuiproject.oneui.ktx.setEntries
import de.lemke.oneuisample.R
import de.lemke.oneuisample.databinding.FragmentTabDesignSubtabWidgetsBinding
import de.lemke.oneuisample.databinding.FragmentTabDesignSubtabWidgetsBinding.inflate
import de.lemke.oneuisample.ui.MainActivity
import de.lemke.oneuisample.ui.util.suggestiveSnackBar

@AndroidEntryPoint
class SubtabWidgets : Fragment() {
    private lateinit var binding: FragmentTabDesignSubtabWidgetsBinding
    private val faceJsons = listOf("great_face.json", "good_face.json", "checking_face.json", "sad_face.json")
    private val faceJsonNames = listOf("Great Face", "Good Face", "Checking Face", "Sad Face")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonColored.setOnClickListener { suggestiveSnackBar("Colored") }
        binding.buttonFilled.setOnClickListener { suggestiveSnackBar("Filled") }
        binding.buttonTransparent.setOnClickListener { suggestiveSnackBar("Transparent") }
        binding.buttonTransparentColored.setOnClickListener { suggestiveSnackBar("Transparent Colored") }
        binding.buttonTransparentThemed.setOnClickListener { suggestiveSnackBar("Transparent Themed") }
        binding.cardItemView.setOnClickListener { suggestiveSnackBar("Card Item View Clicked") }
        binding.bottomTipView.setOnClickListener { suggestiveSnackBar("Bottom Tip View Clicked") }
        binding.bottomTipView.setOnLinkClickListener { suggestiveSnackBar("Bottom Tip View Link Clicked") }
        binding.relativeLink1.setOnClickListener { suggestiveSnackBar("Relative Link 1 Clicked") }
        binding.relativeLink2.setOnClickListener { suggestiveSnackBar("Relative Link 2 Clicked") }
        binding.switchBar.apply {
            addOnSwitchChangeListener { switchCompat, isChecked ->
                setProgressBarVisible(true)
                postDelayed({ setProgressBarVisible(false) }, 1_000)
            }
        }
        binding.fragmentSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, faceJsonNames).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.fragmentSpinner.setEntries(faceJsonNames) { position, _ ->
            position?.let {
                binding.faceIconLottie.setAnimation(faceJsons[position])
                val callback = LottieValueCallback<ColorFilter>(SimpleColorFilter(requireContext().getColor(R.color.primary_color_themed)))
                binding.faceIconLottie.addValueCallback(KeyPath("**"), COLOR_FILTER, callback)
                binding.faceIconLottie.playAnimation()
            }
        }
        binding.searchView.apply {
            val searchManager = requireContext().getSystemService(SEARCH_SERVICE) as SearchManager
            setSearchableInfo(searchManager.getSearchableInfo(ComponentName(requireContext(), MainActivity::class.java)))
            seslSetUpButtonVisibility(VISIBLE)
            seslSetOnUpButtonClickListener { suggestiveSnackBar("Search Up Button Clicked") }
        }
        if (SDK_INT >= Q) binding.root.seslSetGoToTopEnabled(true)

    }
}