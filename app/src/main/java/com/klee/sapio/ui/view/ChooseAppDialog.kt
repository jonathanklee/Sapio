package com.klee.sapio.ui.view

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.klee.sapio.databinding.DialogChooseAppBinding
import com.klee.sapio.domain.model.InstalledApplication
import com.klee.sapio.ui.state.ChooseAppUiState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChooseAppDialog(
    private val uiState: StateFlow<ChooseAppUiState>,
    private val onAppSelected: (InstalledApplication) -> Unit,
    private val onDismissed: (() -> Unit)? = null
) : DialogFragment() {

    private lateinit var mBinding: DialogChooseAppBinding
    private var hasSelection = false

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

        val adapter = ChooseAppAdapter { app ->
            hasSelection = true
            dismiss()
            onAppSelected(app)
        }
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                uiState.collect { state ->
                    if (state.apps.isNotEmpty()) {
                        mBinding.progressBar.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                    adapter.submitList(state.apps)
                }
            }
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
        private const val DIALOG_WIDTH_RATIO = 0.75
    }
}
