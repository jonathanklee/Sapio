package com.klee.sapio.view.ui

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
import com.klee.sapio.R
import com.klee.sapio.databinding.FragmentEvaluateBinding
import com.klee.sapio.model.InstalledApplication
import com.klee.sapio.model.Label
import com.klee.sapio.model.InstalledApplicationsRepository
import com.parse.ParseFile
import com.parse.ParseObject
import com.parse.ParseQuery
import com.scottyab.rootbeer.RootBeer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class EvaluateFragment : Fragment() {

    companion object {
        const val MICRO_G_APP_LABEL = "microG Services Core"
    }

    @Inject lateinit var mInstalledApplicationsRepository: InstalledApplicationsRepository
    private lateinit var mBinding: FragmentEvaluateBinding
    private lateinit var mPackageName: String
    private var mIsMicroGInstalled by Delegates.notNull<Int>()
    private var mIsRooted by Delegates.notNull<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentEvaluateBinding.inflate(layoutInflater)

        mIsMicroGInstalled = isMicroGInstalled()
        mIsRooted = isRooted()

        val microgLabel = Label.create(requireContext(), mIsMicroGInstalled)
        mBinding.microgConfiguration.text = microgLabel?.text
        mBinding.microgConfiguration.setBackgroundColor(microgLabel?.color!!)

        val isRootedLabel = Label.create(requireContext(), mIsRooted)
        mBinding.rootConfiguration.text = isRootedLabel?.text
        mBinding.rootConfiguration.setBackgroundColor(isRootedLabel?.color!!)

        mPackageName = arguments?.getString("package")!!

        mBinding.validateButton.setOnClickListener { onValidateClicked() }
        mBinding.backButton.setOnClickListener { findNavController().navigate(R.id.action_evaluateFragment_to_chooseAppFragment) }
        return mBinding.root
    }

    private fun onValidateClicked() {
        runBlocking {

            if (mBinding.note.checkedRadioButtonId == -1) {
                Toast.makeText(context, "Please select an evaluation.", Toast.LENGTH_SHORT).show()
                return@runBlocking
            }

            val app = mInstalledApplicationsRepository.getApplicationFromPackageName(requireContext(), mPackageName)
            evaluateApp(app!!, requireView())
            findNavController().navigate(R.id.action_evaluateFragment_to_successFragment)
        }
    }

    private suspend fun evaluateApp(app: InstalledApplication, view: View) {
        val parseApp = ParseObject("LibreApps")
        val existingEvaluation = fetchExistingEvaluation(app)
        if (existingEvaluation != null) {
            parseApp.objectId = existingEvaluation.objectId
        }

        val parseFile = ParseFile("icon.png", app.icon?.let { fromDrawableToByArray(it) })
        parseFile.saveInBackground()

        parseApp.put("name", app.name)
        parseApp.put("package", app.packageName)
        parseApp.put("icon", parseFile)

        val rate = getRateFromId(mBinding.note.checkedRadioButtonId, view)
        parseApp.put("rating", rate)

        parseApp.put("microg", mIsMicroGInstalled)
        parseApp.put("rooted", mIsRooted)

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
                return Label.MICROG
            }
        }

        return Label.BARE_AOSP
    }

    private suspend fun fetchExistingEvaluation(app: InstalledApplication) : ParseObject? {
        return withContext(Dispatchers.IO) {
            val query = ParseQuery.getQuery<ParseObject>("LibreApps")
            query.whereEqualTo("package", app.packageName)
            query.whereEqualTo("microg", mIsMicroGInstalled)
            query.whereEqualTo("rooted", mIsRooted)
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

    private fun isRooted(): Int {
        return if (RootBeer(context).isRooted) {
            Label.ROOTED
        } else {
            Label.USER
        }
    }
}
