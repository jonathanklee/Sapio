package com.klee.sapio.ui.view

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.klee.sapio.data.repository.InstalledApplicationsRepository
import com.klee.sapio.databinding.DialogChooseAppBinding
import com.klee.sapio.domain.model.InstalledApplication
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChooseAppDialog(
    private val onAppSelected: (InstalledApplication) -> Unit,
    private val onDismissed: (() -> Unit)? = null
) : DialogFragment() {

    private lateinit var mBinding: DialogChooseAppBinding
    private var hasSelection = false

    @Inject lateinit var mInstalledApplicationsRepository: InstalledApplicationsRepository

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
            hasSelection = true
            dismiss()
            onAppSelected(app)
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        if (!hasSelection) {
            onDismissed?.invoke()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!hasSelection) {
            onDismissed?.invoke()
        }
    }
}
