package com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.configuration.dump

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.navArgs
import com.kieronquinn.app.smartspacer.plugin.amazon.databinding.FragmentConfigurationTargetAmazonDumpBinding
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BoundFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesTitle
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.onApplyInsets
import com.kieronquinn.app.shared.R as SharedR

class AmazonTargetConfigurationDumpFragment: BoundFragment<FragmentConfigurationTargetAmazonDumpBinding>(FragmentConfigurationTargetAmazonDumpBinding::inflate), BackAvailable, ProvidesTitle {

    private val args by navArgs<AmazonTargetConfigurationDumpFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.content.text = args.deliveries.joinToString("\n\n")
        val bottomPadding = resources.getDimensionPixelSize(SharedR.dimen.margin_16)
        binding.content.onApplyInsets { v, insets ->
            v.updatePadding(bottom =
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom + bottomPadding
            )
        }
    }

    override fun getTitle(): CharSequence {
        return getString(args.title)
    }

}