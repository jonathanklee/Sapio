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
import kotlinx.coroutines.launch

class SearchAppAdapter(
    private val mContext: Context,
    private var mApps: List<Evaluation>,
    private var mEvaluationRepository: EvaluationRepository,
    private var mCoroutineScope: CoroutineScope
) : RecyclerView.Adapter<SearchAppAdapter.ViewHolder>() {

    inner class ViewHolder(
        val binding: SearchAppCardBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SearchAppCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.image.setImageBitmap(null)

        val app = mApps[position]
        val element = holder.binding
        element.appName.text = app.name
        element.packageName.text = app.packageName

        mCoroutineScope.launch {
            val icons = mEvaluationRepository.existingIcon("${app.packageName}.png")
            if (icons.isNotEmpty()) {
                Glide.with(mContext)
                    .load(EvaluationService.BASE_URL + icons[0].url)
                    .into(holder.binding.image)
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, EvaluationsActivity::class.java)
            intent.putExtra("packageName", app.packageName)
            intent.putExtra("appName", app.name)

            mContext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return mApps.size
    }
}
