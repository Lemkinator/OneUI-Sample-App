package de.lemke.oneuisampleapp.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import de.lemke.oneuisampleapp.databinding.ActivityAboutBinding
import dev.oneuiproject.oneui.layout.AppInfoLayout
import dev.oneuiproject.oneui.widget.Toast

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.appInfoLayout.addOptionalText("Extra 1")
        binding.appInfoLayout.addOptionalText("Extra 2")
        binding.appInfoLayout.setMainButtonClickListener(object : AppInfoLayout.OnClickListener {
            override fun onUpdateClicked(v: View) {
                Toast.makeText(this@AboutActivity, "onUpdateClicked", Toast.LENGTH_SHORT).show()
            }
            override fun onRetryClicked(v: View) {
                Toast.makeText(this@AboutActivity, "onRetryClicked", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @SuppressLint("WrongConstant")
    fun changeStatus(v: View?) {
        binding.appInfoLayout.status = if (binding.appInfoLayout.status + 1 == 4) -1 else binding.appInfoLayout.status + 1
    }

    fun openGitHubPage(v: View?) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://github.com/OneUIProject/oneui-design")
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No suitable activity found", Toast.LENGTH_SHORT).show()
        }
    }
}