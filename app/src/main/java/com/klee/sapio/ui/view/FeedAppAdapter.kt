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
import com.klee.sapio.domain.EvaluationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class FeedAppAdapter(
    private val mContext: Context,
    private var mApps: List<Evaluation>,
    private var mEvaluationRepository: EvaluationRepository,
    private var mCoroutineScope: CoroutineScope
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
        holder.binding.image.setImageBitmap(null)

        val app = mApps[position]
        val element = holder.binding
        element.appName.text = app.name
        element.packageName.text = app.packageName

        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        element.updatedDate.text =
            mContext.getString(
                R.string.updated_on,
                app.updatedAt?.let { dateFormat.format(it) }
            )
        
        element.emoji.text = Rating.create(app.rating).text

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val microgLabel = Label.create(mContext, app.microg)
            val rootLabel = Label.create(mContext, app.rooted)

            element.microG.text = microgLabel.text
            element.microG.setBackgroundColor(microgLabel.color)

            element.rooted.text = rootLabel.text
            element.rooted.setBackgroundColor(rootLabel.color)
        }

        mCoroutineScope.launch {
            val icons = mEvaluationRepository.existingIcon("${app.packageName}.png")
            if (icons.isNotEmpty()) {
                Glide.with(mContext.applicationContext)
                    .load(EvaluationService.BASE_URL + icons[0].url)
                    .placeholder(R.drawable.ic_android)
                    .error(R.drawable.ic_android)
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
