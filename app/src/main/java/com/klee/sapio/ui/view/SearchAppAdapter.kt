package com.klee.sapio.ui.view

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.klee.sapio.databinding.SearchAppCardBinding
import com.bumptech.glide.Glide
import com.klee.sapio.data.Evaluation
import com.klee.sapio.data.EvaluationService
import com.klee.sapio.domain.EvaluationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchAppAdapter(
    private val mContext: Context,
    private var mApps: List<Evaluation>,
    private var mEvaluationRepository: EvaluationRepository,
) : RecyclerView.Adapter<SearchAppAdapter.ViewHolder>() {

    inner class ViewHolder(
        val binding: SearchAppCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        val viewHolderScope = CoroutineScope(Dispatchers.Main + Job())
        var imageLoadJob: Job? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SearchAppCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = mApps[position]
        val element = holder.binding
        element.appName.text = app.name
        element.packageName.text = app.packageName

        holder.imageLoadJob = holder.viewHolderScope.launch {
            val icons = mEvaluationRepository.existingIcon("${app.packageName}.png")
            if (icons.isNotEmpty()) {
                Glide.with(mContext.applicationContext)
                    .load(EvaluationService.BASE_URL + icons[0].url)
                    .into(holder.binding.image)
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, EvaluationsActivity::class.java)
            intent.putExtra(EvaluationsActivity.EXTRA_PACKAGE_NAME, app.packageName)
            intent.putExtra(EvaluationsActivity.EXTRA_APP_NAME, app.name)

            mContext.startActivity(intent)
        }
    }

    override fun onViewRecycled(holder: SearchAppAdapter.ViewHolder) {
        super.onViewRecycled(holder)
        Glide.with(mContext.applicationContext).clear(holder.binding.image)
        holder.imageLoadJob?.cancel()
    }

    override fun getItemCount(): Int {
        return mApps.size
    }
}
