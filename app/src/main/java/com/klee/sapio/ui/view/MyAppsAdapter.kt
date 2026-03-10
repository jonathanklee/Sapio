package com.klee.sapio.ui.view

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.klee.sapio.databinding.MyAppCardBinding
import com.klee.sapio.ui.model.InstalledAppWithRating
import com.klee.sapio.ui.model.Rating

class MyAppsAdapter(
    private val mContext: Context,
    private val onContribute: () -> Unit
) : ListAdapter<InstalledAppWithRating, MyAppsAdapter.ViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<InstalledAppWithRating>() {
            override fun areItemsTheSame(
                oldItem: InstalledAppWithRating,
                newItem: InstalledAppWithRating
            ): Boolean {
                return oldItem.installedApp.packageName == newItem.installedApp.packageName
            }

            override fun areContentsTheSame(
                oldItem: InstalledAppWithRating,
                newItem: InstalledAppWithRating
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(val binding: MyAppCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MyAppCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val element = holder.binding

        element.appName.text = item.installedApp.name
        element.packageName.text = item.installedApp.packageName
        try {
            element.image.setImageDrawable(
                holder.itemView.context.packageManager.getApplicationIcon(item.installedApp.packageName)
            )
        } catch (e: PackageManager.NameNotFoundException) {
            // leave default
        }

        val rating = item.evaluation?.rating
        element.infoIcon.visibility = View.VISIBLE
        if (rating != null) {
            element.emoji.text = Rating.create(rating).text
            element.emoji.visibility = View.VISIBLE
            element.noRatingIcon.visibility = View.GONE
        } else {
            element.emoji.visibility = View.GONE
            element.noRatingIcon.visibility = View.VISIBLE
        }

        holder.itemView.setOnClickListener {
            if (item.evaluation != null) {
                val intent = Intent(mContext, EvaluationsActivity::class.java)
                intent.putExtra(EvaluationsActivity.EXTRA_PACKAGE_NAME, item.installedApp.packageName)
                intent.putExtra(EvaluationsActivity.EXTRA_APP_NAME, item.installedApp.name)
                mContext.startActivity(intent)
            } else {
                onContribute()
            }
        }
    }
}
