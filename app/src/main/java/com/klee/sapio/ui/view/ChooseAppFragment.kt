package com.klee.sapio.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.klee.sapio.R
import com.klee.sapio.databinding.FragmentChooseAppBinding
import com.klee.sapio.domain.model.InstalledApplication
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ChooseAppFragment : Fragment() {

    private lateinit var mBinding: FragmentChooseAppBinding
    private var mApp: InstalledApplication? = null

    private companion object {
        const val HTTP_200_SUCCESS = 200
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentChooseAppBinding.inflate(inflater, container, false)
        mBinding.chooseAppButton.setOnClickListener { onChooseButtonClicked() }

        mBinding.nextButton.isEnabled = false
        mBinding.nextButton.setOnClickListener { onNextButtonClicked() }
        mBinding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_chooseAppFragment_to_warningFragment)
        }

        return mBinding.root
    }

    private fun onChooseButtonClicked() {
        mBinding.chooseAppButton.isEnabled = false
        mBinding.nextButton.isEnabled = false
        val dialog = ChooseAppDialog { chosenApp ->
            mBinding.appName.text = chosenApp.name
            mApp = chosenApp
            runBlocking {
                if (isOnFdroid(chosenApp)) {
                    Toast.makeText(
                        context,
                        getString(R.string.select_application_error),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    mBinding.nextButton.isEnabled = true
                }
                mBinding.chooseAppButton.isEnabled = true
            }
        }
        dialog.show(parentFragmentManager, "")
    }

    private fun onNextButtonClicked() {
        val bundle = bundleOf(
            "package" to mApp?.packageName,
            "name" to mApp?.name
        )

        findNavController().navigate(R.id.action_chooseAppFragment_to_evaluateFragment, bundle)
    }

    private suspend fun isOnFdroid(app: InstalledApplication): Boolean {
        val client = HttpClient(Android) {
            install(HttpTimeout)
        }

        return withContext(Dispatchers.IO) {
            client.get(
                "https://f-droid.org/api/v1/packages/${app.packageName}"
            ).status.value == HTTP_200_SUCCESS
        }
    }
}
