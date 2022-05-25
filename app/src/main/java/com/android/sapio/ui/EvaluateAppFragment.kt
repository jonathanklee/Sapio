package com.android.sapio.ui

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
import com.android.sapio.databinding.FragmentEvaluateAppBinding
import com.android.sapio.model.App
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.parse.ParseFile
import com.parse.ParseObject
import com.parse.ParseQuery
import com.scottyab.rootbeer.RootBeer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class EvaluateAppFragment : Fragment() {

    private lateinit var mBinding: FragmentEvaluateAppBinding
    private lateinit var mApp: App

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentEvaluateAppBinding.inflate(layoutInflater)
        mBinding.chooseAppButton.setOnClickListener {
            val chooseApp = ChooseAppDialog() { app ->
                mBinding.appName.text = app.name
                mApp = app
            }
            chooseApp.show(parentFragmentManager, "")
        }


        mBinding.validateButton.setOnClickListener { onValidateClicked() }
        return mBinding.root
    }

    private fun onValidateClicked() {
        runBlocking {
            if (mBinding.appName.text.isEmpty()) {
                Toast.makeText(context, "Please select an app.", Toast.LENGTH_LONG).show()
                return@runBlocking
            }

            if (mBinding.note.checkedRadioButtonId == -1) {
                Toast.makeText(context, "Please select an evaluation.", Toast.LENGTH_LONG).show()
                return@runBlocking
            }

            evaluateApp(mApp, requireView())
            findNavController().navigate(R.id.action_evaluation_done)
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

        if (existingEvaluation == null) {
            parseApp.saveInBackground()
        } else if (existingEvaluation.getInt("rating") != rate) {
            parseApp.saveInBackground()
        }
    }

    private suspend fun fetchExistingEvaluation(app: App) : ParseObject? {
        return withContext(Dispatchers.IO) {
            val query = ParseQuery.getQuery<ParseObject>("LibreApps")
            query.whereContains("package", app.packageName)
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
            "Works properly" -> 1
            "Works partially" -> 2
            "Does not work at all" -> 3
            else -> 0
        }
    }
}
