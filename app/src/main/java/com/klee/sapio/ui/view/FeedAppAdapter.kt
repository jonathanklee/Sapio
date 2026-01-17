package com.klee.sapio.ui.view

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.klee.sapio.R
import com.klee.sapio.domain.model.Evaluation
import com.klee.sapio.data.api.EvaluationService
import com.klee.sapio.data.system.Settings
import com.klee.sapio.data.system.UserType
import com.klee.sapio.ui.model.Label
import com.klee.sapio.ui.model.Rating
import com.klee.sapio.databinding.FeedAppCardBinding
import com.klee.sapio.domain.EvaluationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class FeedAppAdapter(
    private val mContext: Context,
    private var mApps: MutableList<Evaluation>,
    private var mEvaluationRepository: EvaluationRepository,
    private var mSettings: Settings
) : RecyclerView.Adapter<FeedAppAdapter.ViewHolder>() {

    companion object {
        const val DATE_FORMAT = "dd/MM/yyyy"
    }

    inner class ViewHolder(
        val binding: FeedAppCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        val viewHolderScope = CoroutineScope(Dispatchers.Main + Job())
        var imageLoadJob: Job? = null
    }

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
            mContext.getString(
                R.string.updated_on,
                app.updatedAt?.let { dateFormat.format(it) }
            )

        element.emoji.text = Rating.create(app.rating).text

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val microgLabel = Label.create(mContext, app.microg)
            val secureLabel = Label.create(mContext, app.secure)

            element.microG.text = microgLabel.text
            element.microG.setBackgroundColor(microgLabel.color)

            element.secure.text = secureLabel.text
            element.secure.setBackgroundColor(secureLabel.color)

            if (mSettings.getRootConfigurationLevel() == UserType.RISKY) {
                element.secure.visibility = View.VISIBLE
            } else {
                element.secure.visibility = View.GONE
            }
        }

        holder.imageLoadJob = holder.viewHolderScope.launch {
            val icons = mEvaluationRepository.existingIcon("${app.packageName}.png")
                .getOrDefault(emptyList())
            if (icons.isNotEmpty()) {
                Glide.with(mContext.applicationContext)
                    .load(EvaluationService.BASE_URL + icons[0].url)
                    .into(holder.binding.image)
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, EvaluationsActivity::class.java)
            intent.putExtra(EvaluationsActivity.EXTRA_PACKAGE_NAME, app.packageName)
            intent.putExtra(EvaluationsActivity.EXTRA_APP_NAME, app.name)

            mContext.startActivity(intent)
        }
    }

    fun addEvaluations(evaluations: List<Evaluation>) {
        val oldSize = mApps.size
        mApps.addAll(evaluations)
        notifyItemRangeInserted(oldSize, evaluations.size)
    }

    fun replaceEvaluations(evaluations: List<Evaluation>) {
        mApps.clear()
        mApps.addAll(evaluations)
        notifyDataSetChanged()
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        Glide.with(mContext.applicationContext).clear(holder.binding.image)
        holder.imageLoadJob?.cancel()
    }

    override fun getItemCount(): Int {
        return mApps.size
    }
}
