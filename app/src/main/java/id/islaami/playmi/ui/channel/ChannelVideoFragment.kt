package id.islaami.playmi.ui.channel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import id.islaami.playmi.R
import id.islaami.playmi.ui.adapter.VideoAdapter
import id.islaami.playmi.ui.adapter.VideoPagedAdapter
import id.islaami.playmi.ui.adapter.VideoPagedAdapterOld
import id.islaami.playmi.ui.base.BaseFragment
import id.islaami.playmi.util.ERROR_EMPTY_LIST
import id.islaami.playmi.util.ResourceStatus.*
import id.islaami.playmi.util.handleApiError
import id.islaami.playmi.util.ui.PlaymiDialogFragment
import id.islaami.playmi.util.ui.showLongToast
import id.islaami.playmi.util.ui.showSnackbar
import id.islaami.playmi.util.value
import kotlinx.android.synthetic.main.channel_video_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChannelVideoFragment(var channelID: Int) : BaseFragment() {
    val viewModel: ChannelViewModel by viewModel()

    private var videoPagedAdapter = VideoPagedAdapterOld(context,
        popMenu = { context, menuView, video ->
            PopupMenu(context, menuView).apply {
                inflate(R.menu.menu_popup_channel_detail)

                menu.getItem(0).title = "Simpan ke Tonton Nanti"

                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.popSaveLater -> {
                            PlaymiDialogFragment.show(
                                fragmentManager = childFragmentManager,
                                text = "Simpan ke daftar Tonton Nanti?",
                                okCallback = { viewModel.watchLater(video.ID.value()) }
                            )

                            true
                        }
                        else -> false
                    }
                }

                show()
            }
        })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.channel_video_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.initChannelVideo(channelID)
        observeGetAllVideoResult()
        observeWatchLaterResult()
    }

    fun refresh() {
        viewModel.refreshAllVideo()
    }

    companion object {
        fun newInstance(channelID: Int): Fragment = ChannelVideoFragment(channelID)
    }

    private fun observeGetAllVideoResult() {
        viewModel.videoPagedListResultLd.observe(viewLifecycleOwner, Observer { result ->
            recyclerView.adapter = videoPagedAdapter.apply { addVideoList(result) }
            recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        })

        viewModel.pagedListNetworkStatusLd.observe(viewLifecycleOwner, Observer { result ->
            when (result?.status) {
                LOADING -> {
                }
                SUCCESS -> {
                }
                ERROR -> {
                    when (result.message) {
                        ERROR_EMPTY_LIST -> showSnackbar("Berhasil")
                        else -> {
                            handleApiError(errorMessage = result.message)
                            { message -> showSnackbar(message) }
                        }
                    }
                }
            }
        })
    }

    private fun observeWatchLaterResult() {
        viewModel.watchLaterResultLd.observe(viewLifecycleOwner, Observer { result ->
            when (result?.status) {
                LOADING -> {
                }
                SUCCESS -> {
                    showLongToast(context, "Berhasil disimpan")
                }
                ERROR -> {
                    handleApiError(errorMessage = result.message) { message ->
                        showLongToast(context, message)
                    }
                }
            }
        })
    }
}
