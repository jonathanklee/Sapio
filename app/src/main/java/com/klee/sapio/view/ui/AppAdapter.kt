package com.klee.sapio.view.ui

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.klee.sapio.databinding.AppCardBinding
import com.klee.sapio.model.RemoteApplication
import com.klee.sapio.model.Label
import com.klee.sapio.model.Rating
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class AppAdapter(
    private val mContext: Context,
    private var mApps: List<RemoteApplication>
) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    companion object {
        const val DATE_FORMAT = "dd/MM/yyyy HH:MM"
    }

    inner class ViewHolder(val binding: AppCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AppCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = mApps[position]
        val element = holder.binding
        element.appName.text = app.name
        element.packageName.text = app.packageName

        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        element.updatedDate.text = "Updated at ${dateFormat.format(app.updatedAt)}"

        element.emoji.text = Rating.create(app.rating)?.text

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val microgLabel = Label.create(mContext, app.microg)
            val rootLabel = Label.create(mContext, app.rooted)

            element.microG.text = microgLabel.text
            element.microG.setBackgroundColor(microgLabel.color)

            element.rooted.text = rootLabel.text
            element.rooted.setBackgroundColor(rootLabel.color)
        }

        Glide.with(mContext).load(app.iconUrl).into(holder.binding.imageIcon)
    }

    override fun getItemCount(): Int {
        return mApps.size
    }
}
