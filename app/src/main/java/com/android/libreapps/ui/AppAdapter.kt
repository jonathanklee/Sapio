package com.android.libreapps.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.libreapps.databinding.AppCardBinding
import com.parse.ParseObject

class AppAdapter(
    private var mApps: List<ParseObject>
) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: AppCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AppCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = mApps[position]
        holder.binding.appName.text = app.getString("name")
    }

    override fun getItemCount(): Int {
        return mApps.size
    }
}
