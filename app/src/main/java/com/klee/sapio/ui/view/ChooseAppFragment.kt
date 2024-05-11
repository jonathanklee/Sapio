package com.klee.sapio.ui.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.klee.sapio.R
import com.klee.sapio.databinding.FragmentChooseAppBinding
import com.klee.sapio.data.InstalledApplication
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.HttpTimeout
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ChooseAppFragment : Fragment() {

    private lateinit var mBinding: FragmentChooseAppBinding
    private var mApp: InstalledApplication? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentChooseAppBinding.inflate(layoutInflater)
        mBinding.chooseAppButton.setOnClickListener {
            mBinding.nextButton.isEnabled = false
            val chooseApp = ChooseAppDialog { app ->
                mBinding.appName.text = app.name
                mApp = app
                runBlocking {
                    if (isOnFdroid(app)) {
                        Toast.makeText(
                            context,
                            getString(R.string.select_application_error),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        mBinding.nextButton.isEnabled = true
                    }
                }
            }
            chooseApp.show(parentFragmentManager, "")
        }

        mBinding.nextButton.isEnabled = false
        mBinding.nextButton.setOnClickListener { onNextClicked() }
        mBinding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_chooseAppFragment_to_warningFragment)
        }

        return mBinding.root
    }

    private fun onNextClicked() {
        val bundle = bundleOf("package" to mApp?.packageName)
        findNavController().navigate(R.id.action_chooseAppFragment_to_evaluateFragment, bundle)
    }

    private suspend fun isOnFdroid(app: InstalledApplication): Boolean {
        val client = HttpClient(Android) {
            install(HttpTimeout)
        }

        return withContext(Dispatchers.IO) {
            try {
                client.get<String>("https://f-droid.org/api/v1/packages/${app.packageName}")
                true
            } catch (_: Exception) {
                false
            }
        }
    }
}
