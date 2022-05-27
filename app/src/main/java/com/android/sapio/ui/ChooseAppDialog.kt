package com.android.sapio.ui

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.sapio.databinding.DialogChooseAppBinding
import com.android.sapio.model.App

class ChooseAppDialog(private val mListener: Listener) : DialogFragment() {

    private lateinit var mBinding: DialogChooseAppBinding

    fun interface Listener {
        fun onAppSelected(app: App)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogChooseAppBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val list: List<App> = getAppList()
        val recyclerView = mBinding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        recyclerView.adapter = ChooseAppAdapter(list) { app ->
            dismiss()
            mListener.onAppSelected(app)
        }
    }

    private fun getAppList(): List<App> {
        val mainIntent = Intent(Intent.ACTION_MAIN)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = requireContext().packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            apps.removeIf { x -> isSystemApp(x) }
        }

        val results: MutableList<App> = arrayListOf()
        for (app in apps) {
            results.add(buildApp(app))
        }

        return results.sortedBy { app -> app.name.lowercase() }
    }

    private fun buildApp(info: ApplicationInfo): App {
        val packageManager = requireContext().packageManager
        return App(
            packageManager.getApplicationLabel(info).toString(),
            info.packageName,
            info.loadIcon(packageManager)
        )
    }

    private fun isSystemApp(info: ApplicationInfo): Boolean {
        return info.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }
}
