package com.android.sapio.ui

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.sapio.R
import com.android.sapio.databinding.AppCardBinding
import com.bumptech.glide.Glide
import com.parse.ParseObject
import java.text.SimpleDateFormat
import java.util.*

class AppAdapter(
    private val mContext: Context,
    private var mApps: List<ParseObject>
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
        element.appName.text = app.getString("name")
        element.packageName.text = app.getString("package")

        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        element.updatedDate.text = "Updated at ${dateFormat.format(app.updatedAt)}"

        element.emoji.text = getTextFromRate(app.getInt("rating"))

        val microG = app.getInt("microg")
        if (microG == 1) {
            element.microG.text = mContext.getString(R.string.microg_label)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                element.microG.setBackgroundColor(mContext.getColor(R.color.teal_200))
            }
        } else {
            element.microG.text = mContext.getString(R.string.bare_aosp_label)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                element.microG.setBackgroundColor(mContext.getColor(R.color.purple_200))
            }
        }

        val image = app.getParseFile("icon")
        Glide.with(mContext).load(image?.url).into(holder.binding.imageIcon)
    }

    private fun getTextFromRate(rate: Int): String {
        return when (rate) {
            1 -> "\uD83D\uDFE2 \uD83E\uDD47"
            2 -> "\uD83D\uDFE0 \uD83D\uDE10"
            3 -> "\uD83D\uDD34 \uD83D\uDC4E"
            else -> ""
        }
    }

    override fun getItemCount(): Int {
        return mApps.size
    }
}
