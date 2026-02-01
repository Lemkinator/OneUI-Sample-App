package de.lemke.oneuisample.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.util.withContext
import de.lemke.oneuisample.R
import dev.oneuiproject.oneui.R as iconsR
import dev.oneuiproject.oneui.design.R as designR

class LibsActivity : AppCompatActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                if (isSystemInDarkTheme()) darkColorScheme(
                    primary = Color(colorResource(id = R.color.primary_color_themed).value),
                    secondary = Color(colorResource(id = R.color.secondary_text_icon_color_themed).value),
                    background = Color(colorResource(id = designR.color.oui_des_round_and_bgcolor).value)
                ) else lightColorScheme(
                    primary = Color(colorResource(id = R.color.primary_color_themed).value),
                    secondary = Color(colorResource(id = R.color.secondary_text_icon_color_themed).value),
                    background = Color(colorResource(id = designR.color.oui_des_round_and_bgcolor).value)
                )
            ) {
                Scaffold(topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.open_source_licenses)) },
                        colors = topAppBarColors(containerColor = Color(colorResource(designR.color.oui_des_round_and_bgcolor).value)),
                        navigationIcon = {
                            IconButton(onClick = { onBackPressedDispatcher.onBackPressed() }) {
                                Icon(imageVector = ImageVector.vectorResource(iconsR.drawable.ic_oui_back), contentDescription = null)
                            }
                        }
                    )
                }
                ) { padding ->
                    Column(modifier = Modifier.padding(padding)) {
                        LibrariesContainer(
                            Libs.Builder().withContext(this@LibsActivity).build(),
                            modifier = Modifier.fillMaxSize(),
                            showDescription = true,
                            showFundingBadges = true,
                        )
                    }
                }
            }
        }
    }
}