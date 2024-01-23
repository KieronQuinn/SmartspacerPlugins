package com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.info

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.util.Linkify
import android.view.View
import com.kieronquinn.app.smartspacer.plugin.amazon.R
import com.kieronquinn.app.smartspacer.plugin.amazon.databinding.FragmentInfoBinding
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BoundFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.LockCollapsed
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesTitle
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onClicked
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import org.koin.android.ext.android.inject

class InfoFragment: BoundFragment<FragmentInfoBinding>(FragmentInfoBinding::inflate), BackAvailable, LockCollapsed, ProvidesTitle {

    private val navigation by inject<ContainerNavigation>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSignIn()
        setupLinkedParagraph()
        binding.root.isNestedScrollingEnabled = false
    }

    private fun setupLinkedParagraph() = with(binding.infoP3) {
        text = Html.fromHtml(
            getString(R.string.target_configuration_settings_info_p3),
            Html.FROM_HTML_MODE_LEGACY
        )
        Linkify.addLinks(this, Linkify.WEB_URLS)
        movementMethod = BetterLinkMovementMethod.newInstance().apply {
            setOnLinkClickListener { _, url ->
                startActivity(Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                })
                true
            }
        }
    }

    private fun setupSignIn() = with(binding.infoSignIn) {
        whenResumed {
            onClicked().collect {
                navigation.navigate(InfoFragmentDirections.actionInfoFragmentToPackagesFragment())
            }
        }
    }

    override fun getTitle() = ""

}