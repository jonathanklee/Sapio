package com.android.sapio.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.sapio.R
import com.android.sapio.databinding.FragmentEvaluateAppBinding
import com.android.sapio.model.App
import com.parse.ParseFile
import com.parse.ParseObject
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

        mBinding.validateButton.setOnClickListener {
            evaluateApp(mApp, requireView())
            findNavController().navigate(R.id.action_evaluation_done)
        }
        return mBinding.root
    }

    private fun evaluateApp(app: App, view: View) {
        val bitmap = app.icon.toBitmap()
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val parseFile = ParseFile("icon.png", stream.toByteArray())
        parseFile.saveInBackground()
        val parseApp = ParseObject("LibreApps")
        parseApp.put("name", app.name)
        parseApp.put("package", app.packageName)
        parseApp.put("icon", parseFile)

        parseApp.put("rating", getRadioButtonFromId(mBinding.note.checkedRadioButtonId, view))
        parseApp.saveInBackground()
    }

    private fun getRadioButtonFromId(id: Int, view: View) : Int {
        val radioButton: RadioButton = view.findViewById(id)
        return when(radioButton.text) {
            "Works properly" -> 1
            "Works partialy" -> 2
            "Does not work at all" -> 3
            else -> 0
        }
    }

}
