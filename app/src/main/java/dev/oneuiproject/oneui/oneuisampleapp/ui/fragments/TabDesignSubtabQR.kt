package dev.oneuiproject.oneui.oneuisampleapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import dev.oneuiproject.oneui.oneuisampleapp.R
import dev.oneuiproject.oneui.oneuisampleapp.databinding.FragmentTabDesignSubtabQrBinding
import dev.oneuiproject.oneui.qr.QREncoder

@AndroidEntryPoint
class TabDesignSubtabQR : Fragment() {
    private lateinit var binding: FragmentTabDesignSubtabQrBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabDesignSubtabQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*binding.qrImage1.setImageBitmap(
            QREncoder(
                context,
                "The One UI Sample app has been made to showcase the components from both our oneui-core libraries and oneui-design module."
            )
                .setIcon(R.drawable.ic_launcher)
                .generate()
        )

        binding.qrImage2.setImageBitmap(
            QREncoder(context, "https://github.com/OneUIProject/oneui-design/raw/main/sample-app/release/sample-app-release.apk")
                .setIcon(dev.oneuiproject.oneui.R.drawable.ic_oui_file_type_apk)
                .setFGColor(Color.parseColor("#ff6ebe64"), true, true)
                .generate()
        )

        binding.qrImage3.setImageBitmap(
            QREncoder(context, "custom colors and size")
                .setIcon(dev.oneuiproject.oneui.R.drawable.ic_oui_file_type_txt)
                .setSize(350)
                .setBGColor(Color.BLACK)
                .setFGColor(Color.RED, true, true)
                .generate()
        )

        binding.qrImage4.setImageBitmap(
            QREncoder(context, "without frame and icon")
                .setFrame(false)
                .generate()
        )*/
    }
}