package com.klee.sapio.ui.view

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.klee.sapio.data.repository.InstalledApplicationsRepository
import com.klee.sapio.databinding.DialogChooseAppBinding
import com.klee.sapio.domain.CheckFdroidAvailabilityUseCase
import com.klee.sapio.domain.model.InstalledApplication
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ChooseAppDialog(
    private val onAppSelected: (InstalledApplication) -> Unit,
    private val onDismissed: (() -> Unit)? = null
) : DialogFragment() {

    private lateinit var mBinding: DialogChooseAppBinding
    private var hasSelection = false

    @Inject lateinit var mInstalledApplicationsRepository: InstalledApplicationsRepository

    @Inject lateinit var checkFdroidAvailabilityUseCase: CheckFdroidAvailabilityUseCase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogChooseAppBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * DIALOG_WIDTH_RATIO).toInt()
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val recyclerView = mBinding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)

        lifecycleScope.launch {
            val allApps = withContext(Dispatchers.IO) {
                mInstalledApplicationsRepository.getAppList(requireContext())
            }
            val filtered = filterFdroidApps(allApps)
            mBinding.progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            recyclerView.adapter = ChooseAppAdapter(filtered) { app ->
                hasSelection = true
                dismiss()
                onAppSelected(app)
            }
        }
    }

    private suspend fun filterFdroidApps(apps: List<InstalledApplication>): List<InstalledApplication> {
        val semaphore = Semaphore(PARALLEL_REQUESTS)
        return coroutineScope {
            apps.map { app ->
                async(Dispatchers.IO) {
                    semaphore.withPermit {
                        if (checkFdroidAvailabilityUseCase(app.packageName)) null else app
                    }
                }
            }.awaitAll().filterNotNull()
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

    companion object {
        private const val PARALLEL_REQUESTS = 10
        private const val DIALOG_WIDTH_RATIO = 0.75
    }
}
