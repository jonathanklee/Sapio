package com.klee.sapio.ui.view

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.klee.sapio.data.api.EvaluationService
import com.klee.sapio.databinding.SearchAppCardBinding
import com.klee.sapio.domain.EvaluationRepository
import com.klee.sapio.domain.model.Evaluation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchAppAdapter(
    private val mContext: Context,
    private var mEvaluationRepository: EvaluationRepository,
) : ListAdapter<Evaluation, SearchAppAdapter.ViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Evaluation>() {
            override fun areItemsTheSame(oldItem: Evaluation, newItem: Evaluation): Boolean {
                return oldItem.packageName == newItem.packageName &&
                    oldItem.microg == newItem.microg &&
                    oldItem.secure == newItem.secure
            }

            override fun areContentsTheSame(oldItem: Evaluation, newItem: Evaluation): Boolean {
                return oldItem == newItem
            }
        }
    }

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
        val app = getItem(position)
        val element = holder.binding
        element.appName.text = app.name
        element.packageName.text = app.packageName

        holder.imageLoadJob?.cancel()
        Glide.with(mContext.applicationContext).clear(holder.binding.image)
        holder.imageLoadJob = holder.viewHolderScope.launch {
            val icons = mEvaluationRepository.existingIcon("${app.packageName}.png")
                .getOrDefault(emptyList())
            if (icons.isNotEmpty()) {
                Glide.with(mContext.applicationContext)
                    .load(EvaluationService.BASE_URL + icons[0].url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
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
}
