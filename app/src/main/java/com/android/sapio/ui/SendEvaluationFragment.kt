package com.android.sapio.ui

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.sapio.R
import com.android.sapio.databinding.FragmentSendEvaluationBinding
import com.android.sapio.model.App
import com.parse.ParseFile
import com.parse.ParseObject
import com.parse.ParseQuery
import com.scottyab.rootbeer.RootBeer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.properties.Delegates

class SendEvaluationFragment : Fragment() {

    companion object {
        const val HAS_MICRO_G = 1;
        const val NO_MICRO_G = 2;
        const val MICRO_G_APP_LABEL = "microG Services Core"
    }

    private lateinit var mBinding: FragmentSendEvaluationBinding
    private lateinit var mApp: App
    private var mIsMicroGInstalled by Delegates.notNull<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentSendEvaluationBinding.inflate(layoutInflater)
        mBinding.chooseAppButton.setOnClickListener {
            val chooseApp = ChooseAppDialog { app ->
                mBinding.appName.text = app.name
                mApp = app
            }
            chooseApp.show(parentFragmentManager, "")
        }

        mIsMicroGInstalled = isMicroGInstalled()

        mBinding.rootBeerWarning.text = "\u26A0\uFE0F"
        if (!isRooted()) {
            mBinding.rootBeerWarning.visibility = View.INVISIBLE
            mBinding.emoji.visibility = View.INVISIBLE
        }

        mBinding.validateButton.setOnClickListener { onValidateClicked() }
        mBinding.backButton.setOnClickListener { findNavController().navigate(R.id.action_to_warning) }
        return mBinding.root
    }

    private fun onValidateClicked() {
        runBlocking {
            if (mBinding.appName.text.isEmpty()) {
                Toast.makeText(context, "Please select an app.", Toast.LENGTH_SHORT).show()
                return@runBlocking
            }

            if (mBinding.note.checkedRadioButtonId == -1) {
                Toast.makeText(context, "Please select an evaluation.", Toast.LENGTH_SHORT).show()
                return@runBlocking
            }

            evaluateApp(mApp, requireView())
            findNavController().navigate(R.id.action_to_thankyou)
        }
    }

    private suspend fun evaluateApp(app: App, view: View) {
        val parseApp = ParseObject("LibreApps")
        val existingEvaluation = fetchExistingEvaluation(app)
        if (existingEvaluation != null) {
            parseApp.objectId = existingEvaluation.objectId
        }

        val parseFile = ParseFile("icon.png", fromDrawableToByArray(app.icon))
        parseFile.saveInBackground()

        parseApp.put("name", app.name)
        parseApp.put("package", app.packageName)
        parseApp.put("icon", parseFile)

        val rate = getRateFromId(mBinding.note.checkedRadioButtonId, view)
        parseApp.put("rating", rate)

        parseApp.put("microg", mIsMicroGInstalled)

        if (existingEvaluation == null) {
            parseApp.saveInBackground()
        } else if (existingEvaluation.getInt("rating") != rate) {
            parseApp.saveInBackground()
        }
    }

    private fun isMicroGInstalled() : Int {
        val packageManager = requireContext().packageManager
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        for (app in apps) {
            if (app.packageName == "com.google.android.gms" &&
                packageManager.getApplicationLabel(app).toString() == MICRO_G_APP_LABEL
            ) {
                return HAS_MICRO_G
            }
        }

        return NO_MICRO_G
    }

    private suspend fun fetchExistingEvaluation(app: App) : ParseObject? {
        return withContext(Dispatchers.IO) {
            val query = ParseQuery.getQuery<ParseObject>("LibreApps")
            query.whereEqualTo("package", app.packageName)
            query.whereEqualTo("microg", mIsMicroGInstalled)
            val answers = query.find()
            if (answers.size == 1) {
                return@withContext answers[0]
            } else {
                return@withContext null
            }
        }
    }

    private fun fromDrawableToByArray(drawable: Drawable) : ByteArray {
        val bitmap = drawable.toBitmap()
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun getRateFromId(id: Int, view: View) : Int {
        val radioButton: RadioButton = view.findViewById(id)
        return when(radioButton.text) {
            getString(R.string.works_perfectly) -> 1
            getString(R.string.works_partially) -> 2
            getString(R.string.dont_work) -> 3
            else -> 0
        }
    }

    private fun isRooted() = RootBeer(context).isRooted
}
