package id.islaami.playmi.ui.video

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import id.islaami.playmi.R
import id.islaami.playmi.data.model.video.Video
import id.islaami.playmi.ui.adapter.VideoPagedAdapter
import id.islaami.playmi.ui.base.BaseFragment
import id.islaami.playmi.ui.home.HomeFragment
import id.islaami.playmi.ui.home.HomeViewModel
import id.islaami.playmi.util.*
import id.islaami.playmi.util.ResourceStatus.*
import id.islaami.playmi.util.ui.*
import kotlinx.android.synthetic.main.video_category_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class VideoCategoryFragment(var categoryID: Int = 0) : BaseFragment() {
    private val viewModel: HomeViewModel by viewModel()

    private var adapter = VideoPagedAdapter(context,
        popMenu = { context, menuView, video ->
            PopupMenu(context, menuView, Gravity.END).apply {
                inflate(R.menu.menu_popup_home)

                if (video.channel?.isFollowed != true) menu.getItem(1).title = "Mulai Mengikuti"
                else menu.getItem(1).title = "Berhenti Mengikuti"

                menu.getItem(0).title = "Simpan ke Tonton Nanti"

                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.popWatchLater -> {
                            PlaymiDialogFragment.show(
                                fragmentManager = childFragmentManager,
                                text = "Simpan ke daftar Tonton Nanti?",
                                okCallback = { viewModel.watchLater(video.ID.value()) }
                            )

                            true
                        }
                        R.id.popFollow -> {
                            if (video.channel?.isFollowed != true) {
                                PlaymiDialogFragment.show(
                                    fragmentManager = childFragmentManager,
                                    text = getString(R.string.channel_follow, video.channel?.name),
                                    okCallback = { viewModel.followChannel(video.channel?.ID.value()) }
                                )
                            } else {
                                PlaymiDialogFragment.show(
                                    fragmentManager = childFragmentManager,
                                    text = getString(
                                        R.string.channel_unfollow,
                                        video.channel?.name
                                    ),
                                    okCallback = { viewModel.unfollowChannel(video.channel?.ID.value()) }
                                )
                            }

                            true
                        }
                        R.id.popHide -> {
                            PlaymiDialogFragment.show(
                                fragmentManager = childFragmentManager,
                                text = getString(R.string.channel_hide, video.channel?.name),
                                okCallback = { viewModel.hideChannel(video.channel?.ID.value()) }
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
    ): View? = inflater.inflate(R.layout.video_category_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        swipeRefreshLayout.apply {
            startRefreshing()
            setColorSchemeResources(R.color.accent)
            setOnRefreshListener { refresh() }
        }

        arguments?.let { bundle ->
            categoryID = bundle.getInt(EXTRA_CATEGORY, 0)
        }

        viewModel.initVideoCategoryFragment(categoryID)
        observeWatchLaterResult()
        observeFollowResult()
        observeUnfollowResult()
        observeHideResult()
    }

    override fun onResume() {
        super.onResume()

        observeGetAllVideoResult()

        val position =
            PreferenceManager.getDefaultSharedPreferences(context).getInt("HOME_SCROLL", 0)
        recyclerView.scrollToPosition(position)
    }

    override fun onPause() {
        super.onPause()

        val layoutManager = recyclerView.layoutManager
        if (layoutManager != null) {
            val position =
                (layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()

            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt("HOME_SCROLL", position)
                .apply()
        }
    }

    private fun refresh() {
        if (categoryID > 0) {
            viewModel.refreshAllVideoByCategory()
        } else {
            viewModel.refreshAllVideo()
        }

        if (this.parentFragment != null) {
            (this.parentFragment as HomeFragment).refresh()
        }
    }

    private fun setupRecyclerView(result: PagedList<Video>?) {
        recyclerView.adapter = adapter.apply { addVideoList(result) }
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    companion object {
        private const val EXTRA_CATEGORY = "EXTRA_CATEGORY"

        fun newInstance(id: Int) =
            VideoCategoryFragment().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_CATEGORY, id)
                }
            }
    }

    /* OBSERVERS */
    private fun observeGetAllVideoResult() {
        val dialog = context?.createMaterialAlertDialog(
            "Coba Lagi",
            positiveCallback = { refresh() },
            dismissCallback = { refresh() }
        )

        viewModel.videoPagedListResultLd.observe(viewLifecycleOwner, Observer { result ->
            if (result.isNullOrEmpty()) {
                emptyText.setVisibilityToVisible()
                recyclerView.setVisibilityToGone()
            } else {
                recyclerView.setVisibilityToVisible()
                emptyText.setVisibilityToGone()

                setupRecyclerView(result)
            }
        })

        viewModel.networkStatusLd.observe(viewLifecycleOwner, Observer { result ->
            when (result?.status) {
                LOADING -> {
                    dialog?.dismiss()
                }
                SUCCESS -> {
                    swipeRefreshLayout.stopRefreshing()
                }
                ERROR -> {
                    swipeRefreshLayout.stopRefreshing()

                    when (result.message) {
                        ERROR_EMPTY_LIST -> showLongToast(
                            context,
                            "Anda belum mengikuti kanal manapun"
                        )
                        ERROR_CONNECTION -> {
                            dialog?.let {
                                context?.showMaterialAlertDialog(
                                    it,
                                    getString(R.string.error_connection)
                                )
                            }
                        }
                        ERROR_CONNECTION_TIMEOUT -> {
                            dialog?.let {
                                context?.showMaterialAlertDialog(
                                    it,
                                    getString(R.string.error_connection_timeout)
                                )
                            }
                        }
                        else -> {
                            handleApiError(errorMessage = result.message) { message ->
                                showLongToast(context, message)
                            }
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

    private fun observeFollowResult() {
        viewModel.followChannelResultLd.observe(viewLifecycleOwner, Observer { result ->
            when (result?.status) {
                LOADING -> {
                }
                SUCCESS -> {
                    showLongToast(context, "Berhasil mengikuti")
                    refresh()
                }
                ERROR -> {
                    handleApiError(errorMessage = result.message) { message ->
                        showLongToast(context, message)
                    }
                }
            }
        })
    }

    private fun observeUnfollowResult() {
        viewModel.unfollowChannelResultLd.observe(viewLifecycleOwner, Observer { result ->
            when (result?.status) {
                LOADING -> {
                }
                SUCCESS -> {
                    showLongToast(context, "Berhenti mengikuti")
                    refresh()
                }
                ERROR -> {
                    handleApiError(errorMessage = result.message) { message ->
                        showLongToast(context, message)
                    }
                }
            }
        })
    }

    private fun observeHideResult() {
        viewModel.hideChannelResultLd.observe(viewLifecycleOwner, Observer { result ->
            when (result?.status) {
                LOADING -> {
                }
                SUCCESS -> {
                    showLongToast(context, getString(R.string.message_channel_hide))
                    refresh()
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
