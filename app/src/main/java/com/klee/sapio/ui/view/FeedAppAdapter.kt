package com.klee.sapio.ui.view

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.klee.sapio.databinding.FeedAppCardBinding
import com.bumptech.glide.Glide
import com.klee.sapio.R
import com.klee.sapio.data.Evaluation
import com.klee.sapio.data.EvaluationService
import com.klee.sapio.data.Label
import com.klee.sapio.data.Rating
import java.text.SimpleDateFormat
import java.util.Locale

class FeedAppAdapter(
    private val mContext: Context,
    private var mApps: List<Evaluation>
) : RecyclerView.Adapter<FeedAppAdapter.ViewHolder>() {

    companion object {
        const val DATE_FORMAT = "dd/MM/yyyy"
    }

    inner class ViewHolder(val binding: FeedAppCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FeedAppCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = mApps[position]
        val element = holder.binding
        element.appName.text = app.name
        element.packageName.text = app.packageName

        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        element.updatedDate.text =
            "${mContext.getString(R.string.updated_on)} ${dateFormat.format(app.updatedAt)}"

        element.emoji.text = Rating.create(app.rating).text

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val microgLabel = Label.create(mContext, app.microg)
            val rootLabel = Label.create(mContext, app.rooted)

            element.microG.text = microgLabel.text
            element.microG.setBackgroundColor(microgLabel.color)

            element.rooted.text = rootLabel.text
            element.rooted.setBackgroundColor(rootLabel.color)
        }

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
