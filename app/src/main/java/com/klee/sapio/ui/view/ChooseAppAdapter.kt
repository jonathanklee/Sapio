package com.klee.sapio.ui.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.klee.sapio.data.InstalledApplication
import com.klee.sapio.databinding.ChooseAppCardBinding

class ChooseAppAdapter(
    private var mApps: List<InstalledApplication>,
    private val mListener: Listener
) : RecyclerView.Adapter<ChooseAppAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ChooseAppCardBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        private var mApp: InstalledApplication? = null

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            mApp?.let {
                mListener.onAppClicked(mApp!!)
            }
        }

        fun bind(app: InstalledApplication) {
            mApp = app
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ChooseAppCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        binding.appName.text = mApps[position].name
        binding.appIcon.setImageDrawable(mApps[position].icon)
        binding.appIcon.setOnClickListener {
            mListener.onAppClicked(mApps[position])
        }

        holder.bind(mApps[position])
    }

    override fun getItemCount(): Int {
        return mApps.size
    }

    fun interface Listener {
        fun onAppClicked(app: InstalledApplication)
    }
}
