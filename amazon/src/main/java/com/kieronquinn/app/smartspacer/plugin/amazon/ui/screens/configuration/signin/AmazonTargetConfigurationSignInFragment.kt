package com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.configuration.signin

import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.kieronquinn.app.smartspacer.plugin.amazon.databinding.FragmentConfigurationTargetAmazonSignInBinding
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.getCookies
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BoundFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.LockCollapsed
import com.kieronquinn.app.smartspacer.plugin.shared.utils.whenResumed

class AmazonTargetConfigurationSignInFragment: BoundFragment<FragmentConfigurationTargetAmazonSignInBinding>(FragmentConfigurationTargetAmazonSignInBinding::inflate), BackAvailable, LockCollapsed {

    companion object {
        private const val REQUEST_KEY = "sign_in"
        private const val KEY_RESULT = "result"

        fun Fragment.registerSignInReceiver(onResult: (Boolean) -> Unit) {
            setFragmentResultListener(REQUEST_KEY) { _, result ->
                onResult.invoke(result.getBoolean(KEY_RESULT, false))
            }
        }
    }

    private val cookieManager = CookieManager.getInstance()
    private val args by navArgs<AmazonTargetConfigurationSignInFragmentArgs>()

    private val webViewClient = object: WebViewClient() {
        override fun onPageFinished(view: WebView, url: String?) {
            super.onPageFinished(view, url)
            checkCookies()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWebView(savedInstanceState)
    }

    private fun setupWebView(state: Bundle?) = with(binding.configurationAmazonSignInWebview) {
        webViewClient = this@AmazonTargetConfigurationSignInFragment.webViewClient
        settings.javaScriptEnabled = true
        cookieManager.setAcceptThirdPartyCookies(this, true)
        if(state != null){
            restoreState(state)
        }else{
            loadUrl(args.url)
        }
    }

    private fun checkCookies() {
        val cookies = cookieManager.getCookies(args.url)
        if(cookies.none { it.key.startsWith("x-acb") }) return
        whenResumed {
            setFragmentResult(REQUEST_KEY, bundleOf(KEY_RESULT to true))
            findNavController().navigateUp()
        }
    }

}