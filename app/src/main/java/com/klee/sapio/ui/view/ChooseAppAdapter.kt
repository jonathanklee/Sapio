package com.klee.sapio.ui.view

import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.klee.sapio.databinding.ChooseAppCardBinding
import com.klee.sapio.domain.model.InstalledApplication

class ChooseAppAdapter(
    private val onAppClicked: (InstalledApplication) -> Unit
) : ListAdapter<InstalledApplication, ChooseAppAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(val binding: ChooseAppCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(app: InstalledApplication) {
            binding.appName.text = app.name
            try {
                binding.appIcon.setImageDrawable(
                    binding.root.context.packageManager
                        .getApplicationInfo(app.packageName, 0)
                        .loadUnbadgedIcon(binding.root.context.packageManager)
                )
            } catch (e: PackageManager.NameNotFoundException) {
                // leave default icon
            }
            binding.root.setOnClickListener { onAppClicked(app) }
            binding.appIcon.setOnClickListener { onAppClicked(app) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ChooseAppCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<InstalledApplication>() {
            override fun areItemsTheSame(old: InstalledApplication, new: InstalledApplication) =
                old.packageName == new.packageName
            override fun areContentsTheSame(old: InstalledApplication, new: InstalledApplication) =
                old == new
        }
    }
}
