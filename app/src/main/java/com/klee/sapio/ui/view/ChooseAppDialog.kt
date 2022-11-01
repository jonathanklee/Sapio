package com.klee.sapio.ui.view

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.klee.sapio.databinding.DialogChooseAppBinding
import com.klee.sapio.data.InstalledApplication
import com.klee.sapio.data.InstalledApplicationsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChooseAppDialog(private val mListener: Listener) : DialogFragment() {

    private lateinit var mBinding: DialogChooseAppBinding

    @Inject lateinit var mInstalledApplicationsRepository: InstalledApplicationsRepository

    fun interface Listener {
        fun onAppSelected(app: InstalledApplication)
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
        val list: List<InstalledApplication> = mInstalledApplicationsRepository.getAppList(requireContext())
        val recyclerView = mBinding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        recyclerView.adapter = ChooseAppAdapter(list) { app ->
            dismiss()
            mListener.onAppSelected(app)
        }
    }
}
