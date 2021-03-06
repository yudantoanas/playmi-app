package id.islaami.playmi.ui.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import id.islaami.playmi.R
import id.islaami.playmi.data.model.video.Video
import id.islaami.playmi.ui.channel.ChannelDetailActivity
import id.islaami.playmi.ui.video.VideoDetailActivity
import id.islaami.playmi.ui.video.VideoLabelActivity
import id.islaami.playmi.ui.video.VideoSubcategoryActivity
import id.islaami.playmi.util.fromDbFormatDateTimeToCustomFormat
import id.islaami.playmi.util.ui.loadExternalImage
import id.islaami.playmi.util.ui.loadImage
import id.islaami.playmi.util.ui.setVisibilityToGone
import id.islaami.playmi.util.ui.setVisibilityToVisible
import id.islaami.playmi.util.value
import kotlinx.android.synthetic.main.video_item.view.*
import java.text.SimpleDateFormat
import java.util.*

class VideoPagedAdapterOld(
    var context: Context? = null,
    var popMenu: (Context, View, Video) -> Unit
) : PagedListAdapter<Video, VideoPagedAdapterOld.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.video_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    fun addVideoList(list: PagedList<Video>?) {
        submitList(list)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(video: Video?) = with(itemView) {
            if (video != null) {
                videoTitle.text = video.title
                videoThumbnail.loadExternalImage("https://img.youtube.com/vi/${video.videoID}/sddefault.jpg")
                channelName.text = video.channel?.name
                channelIcon.loadImage(video.channel?.thumbnail)
                views.text = "${video.views ?: 0}x"

                subcategoryName.apply {
                    text = video.subcategory?.name
                    setOnClickListener {
                        VideoSubcategoryActivity.startActivity(
                            context,
                            video.category?.ID.value(),
                            video.subcategory?.ID.value(),
                            video.subcategory?.name.toString()
                        )
                    }
                }

                if (video.isUploadShown == false) {
                    layoutUploadTime.setVisibilityToGone()
                    dot.setVisibilityToGone()
                } else {
                    layoutUploadTime.setVisibilityToVisible()
                    dot.setVisibilityToVisible()
                }

                recyclerView.adapter =
                    LabelAdapter(video.labels ?: emptyList(),
                        itemClickListener = { labelId, labelName ->
                            VideoLabelActivity.startActivity(
                                context,
                                video.category?.ID.value(),
                                video.subcategory?.ID.value(),
                                labelId,
                                labelName
                            )
                        })
                recyclerView.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val locale = Locale.forLanguageTag("id")
                    val message = TimeAgoMessages.Builder().withLocale(locale).build()

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("id"))
                    val videoDate = dateFormat.parse(video.publishedAt.toString())

                    publishedDate.text = TimeAgo.using(videoDate?.time.value(), message)
                } else {
                    publishedDate.text =
                        video.publishedAt.fromDbFormatDateTimeToCustomFormat("dd MM yyyy")
                }

                videoThumbnail.setOnClickListener {
                    VideoDetailActivity.startActivity(context, video.ID.value())
                }

                menu.setOnClickListener { view ->
                    popMenu(context, view, video)
                }

                channelLayout.setOnClickListener {
                    ChannelDetailActivity.startActivity(
                        context,
                        channelName = video.channel?.name.toString(),
                        channelID = video.channel?.ID.value()
                    )
                }
            }
        }
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<Video> =
            object : DiffUtil.ItemCallback<Video>() {
                override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean =
                    oldItem.ID === newItem.ID

                override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean =
                    oldItem == newItem
            }
    }
}
