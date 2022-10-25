package com.noob.apps.mvvmcountries.ui.details

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.mediarouter.media.MediaRouter
import androidx.recyclerview.widget.GridLayoutManager
import com.noob.apps.mvvmcountries.R
import com.noob.apps.mvvmcountries.adapters.FolderAdapter
import com.noob.apps.mvvmcountries.adapters.RecyclerViewClickListener
import com.noob.apps.mvvmcountries.data.DatabaseBuilder
import com.noob.apps.mvvmcountries.data.DatabaseHelperImpl
import com.noob.apps.mvvmcountries.databinding.ActivityLectureFolderBinding
import com.noob.apps.mvvmcountries.models.Course
import com.noob.apps.mvvmcountries.ui.base.BaseActivity
import com.noob.apps.mvvmcountries.ui.dialog.ConnectionDialogFragment
import com.noob.apps.mvvmcountries.utils.Constant
import com.noob.apps.mvvmcountries.utils.ViewModelFactory
import com.noob.apps.mvvmcountries.viewmodels.CourseViewModel

class LectureFolderActivity : BaseActivity(), RecyclerViewClickListener {
    private lateinit var mActivityBinding: ActivityLectureFolderBinding
    private lateinit var course: Course
    private lateinit var token: String
    private lateinit var mAdapter: FolderAdapter
    private lateinit var courseViewModel: CourseViewModel
    private val TAG = "LectureFolderActivity"
    private var mMediaRouter: MediaRouter? = null
    private val DISCOVERY_FRAGMENT_TAG = "DiscoveryFragment"
    private val mMediaRouterCB: MediaRouter.Callback = object : MediaRouter.Callback() {
        override fun onRouteAdded(router: MediaRouter, route: MediaRouter.RouteInfo) {
            Log.d(
                TAG,
                "onRouteAdded: route=$route"
            )

        }

        override fun onRouteChanged(router: MediaRouter, route: MediaRouter.RouteInfo) {
            Log.d(
                TAG,
                "onRouteChanged: route=$route"
            )

        }


        override fun onRouteRemoved(router: MediaRouter, route: MediaRouter.RouteInfo) {
            Log.d(
                TAG,
                "onRouteRemoved: route=$route"
            )

        }

        override fun onRouteSelected(router: MediaRouter, route: MediaRouter.RouteInfo) {
            Log.d(
                TAG,
                "onRouteSelected: route=$route"
            )
            if (route.connectionState == 2)
                showBlockDialog("You Cannot run App on Screen Mirroring")


        }

        override fun onRouteUnselected(router: MediaRouter, route: MediaRouter.RouteInfo) {
            Log.d(
                TAG,
                "onRouteUnselected: route=$route"
            )

            hideBlockDialog()
        }

        override fun onRouteVolumeChanged(router: MediaRouter, route: MediaRouter.RouteInfo) {
        }

        override fun onRoutePresentationDisplayChanged(
            router: MediaRouter,
            route: MediaRouter.RouteInfo
        ) {
            Log.d(
                TAG,
                "onRoutePresentationDisplayChanged: route=$route"
            )
            if (route.connectionState == 2)
                showBlockDialog("You Cannot run App on Screen Mirroring")

        }

        override fun onProviderAdded(router: MediaRouter, provider: MediaRouter.ProviderInfo) {
        }

        override fun onProviderRemoved(router: MediaRouter, provider: MediaRouter.ProviderInfo) {
        }

        override fun onProviderChanged(router: MediaRouter, provider: MediaRouter.ProviderInfo) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_lecture_folder)
        val i = intent
        course = i.getSerializableExtra(Constant.SELECTED_COURSE) as Course
        token = i.getStringExtra(Constant.USER_TOKEN).toString()
        initializeRecyclerView()
        courseViewModel = ViewModelProvider(
            this,
            ViewModelFactory(
                application,
                DatabaseHelperImpl(DatabaseBuilder.getInstance(this))
            )
        ).get(CourseViewModel::class.java)

        mActivityBinding.backImg.setOnClickListener {
            finish()
        }
        initLectureInfo(course.id)
    }

    private fun initializeRecyclerView() {
        mAdapter = FolderAdapter(this)
        mActivityBinding.folderRec.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@LectureFolderActivity, 1)

            adapter = mAdapter
        }
    }

    override fun onRecyclerViewItemClick(position: Int) {
    }

    override fun onQualitySelected(position: Int) {
    }

    override fun openFile(url: String) {
        val intent = Intent(this, WebActivity::class.java)
        intent.putExtra(Constant.WEB_URL, url)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        mMediaRouter = MediaRouter.getInstance(this)
        val fm = supportFragmentManager
        val fragment: DiscoveryFragment?
        fragment = DiscoveryFragment()
        fragment.setCallback(mMediaRouterCB)
        fm.beginTransaction().add(fragment, DISCOVERY_FRAGMENT_TAG).commit()
    }

    private fun initLectureInfo(lecId: String) {
        courseViewModel.getCourseAttachments(token, lecId)
        courseViewModel.attachmentResponse.observeOnce(this) { kt ->
            if (kt != null) {
                mAdapter.setData(kt.attachments)
            }
        }
        courseViewModel.mShowResponseError.observeOnce(this) {
        }
        courseViewModel.mShowProgressBar.observe(this) { bt ->
            if (bt) {
                showLoader()
            } else {
                hideLoader()
            }

        }
        courseViewModel.mShowNetworkError.observeOnce(this) {
            if (it != null) {
                ConnectionDialogFragment.newInstance(Constant.RETRY_LOGIN)
                    .show(
                        supportFragmentManager,
                        ConnectionDialogFragment.TAG
                    )
            }

        }
    }
}