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

class SearchAppAdapter(
    private val mContext: Context,
    private var mApps: List<Evaluation>
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
        val app = mApps[position]
        val element = holder.binding
        element.appName.text = app.name
        element.packageName.text = app.packageName

        val url = EvaluationService.BASE_URL + app.icon?.data?.attributes?.url
        Glide.with(mContext).load(url).into(holder.binding.image)

        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, EvaluationsActivity::class.java)
            intent.putExtra("packageName", app.packageName)
            intent.putExtra("appName", app.name)
            intent.putExtra("iconUrl", url)

            mContext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return mApps.size
    }
}
