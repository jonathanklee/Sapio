package com.klee.sapio.ui.view

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.klee.sapio.BuildConfig
import com.klee.sapio.R
import com.klee.sapio.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    companion object {
        const val RATING_RULES = "https://github.com/jonathanklee/Sapio?tab=readme-ov-file#rating"
        const val GITHUB_URL = "https://github.com/jonathanklee/Sapio"
    }

    private var _binding: FragmentAboutBinding? = null
    private val mBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.version.text = "v${BuildConfig.VERSION_NAME}"
        mBinding.ratingRules.text = Html.fromHtml(
            getString(R.string.rating_rules, RATING_RULES),
            Html.FROM_HTML_MODE_COMPACT
        )
        mBinding.ratingRules.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
