package com.noob.apps.mvvmcountries.ui.details

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.mediarouter.media.MediaRouter
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerControlView
import com.noob.apps.mvvmcountries.HandlePlayerControllerListener
import com.noob.apps.mvvmcountries.R
import com.noob.apps.mvvmcountries.ZoomableTextureView
import com.noob.apps.mvvmcountries.adapters.CourseLectureAdapter
import com.noob.apps.mvvmcountries.adapters.QualityAdapter
import com.noob.apps.mvvmcountries.adapters.RecyclerViewClickListener
import com.noob.apps.mvvmcountries.data.DatabaseBuilder
import com.noob.apps.mvvmcountries.data.DatabaseHelperImpl
import com.noob.apps.mvvmcountries.data.RoomViewModel
import com.noob.apps.mvvmcountries.databinding.ActivityCourseDetailsBinding
import com.noob.apps.mvvmcountries.databinding.CallDialogBinding
import com.noob.apps.mvvmcountries.databinding.InvalidWatchDialogBinding
import com.noob.apps.mvvmcountries.models.*
import com.noob.apps.mvvmcountries.ui.base.BaseActivity2
import com.noob.apps.mvvmcountries.ui.dialog.ConnectionDialogFragment
import com.noob.apps.mvvmcountries.ui.dialog.LectureWatchDialog
import com.noob.apps.mvvmcountries.utils.AESUtils
import com.noob.apps.mvvmcountries.utils.Constant
import com.noob.apps.mvvmcountries.utils.ViewModelFactory
import com.noob.apps.mvvmcountries.viewmodels.CourseViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class CourseDetailsActivity : BaseActivity2(), RecyclerViewClickListener,
    PlayerControlView.VisibilityListener {
    private val TAG = "CourseDetailsActivity"
    private lateinit var mActivityBinding: ActivityCourseDetailsBinding
    private var player: SimpleExoPlayer? = null
    private var zoomableTextureView: ZoomableTextureView? = null
    private var playWhenReady = false
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private lateinit var mAdapter: CourseLectureAdapter
    private lateinit var course: Course
    private var courseLectures: MutableList<LectureDetails> = mutableListOf()
    private var eligibleToWatch = true
    private val resolutions: MutableList<Files> = mutableListOf()
    private lateinit var lectureResponse: LectureDetails
    private lateinit var courseViewModel: CourseViewModel
    private var userId = ""
    private var token = ""
    private var selectedLectureId = ""
    private var startTime = ""
    private var endTime = ""
    private var sessionTimeout = 0
    private var countDownTimer: CountDownTimer? = null
    private var trackSelector: DefaultTrackSelector? = null
    private var trackSelectorParameters: DefaultTrackSelector.Parameters? = null
    private var link = ""
    private var isFullScreen = false
    private lateinit var qualityAdapter: QualityAdapter
    private var duration: Int = 0
    private lateinit var roomViewModel: RoomViewModel
    private lateinit var user: User
    private var lastLecId = ""
    private var lastDuration: Long = 0
    private var isBack = false
    private var selectedPosition = 0
    private var lecturesDB = mutableListOf<WatchedLectures>()
    private var currentSpeed = "1"
    private var playTime = 0L // in ms
    private var pauseTime = 0L // in ms
    private var totalTime = 0L // in ms
    private var pressedPaused = 0
    private var mPlayDurationInSec = 0
    private var initTime = 0L

    private val analyticsListener: AnalyticsListener = object : AnalyticsListener {


        override fun onIsPlayingChanged(
            eventTime: AnalyticsListener.EventTime,
            isPlaying: Boolean
        ) {
            if (isPlaying) {
                if (initTime != 0L) pauseTime += System.currentTimeMillis() - initTime
                initTime = System.currentTimeMillis()
            } else {
                if (initTime != 0L) playTime += System.currentTimeMillis() - initTime
                initTime = System.currentTimeMillis()
                pressedPaused++
            }
            totalTime = playTime + pauseTime
            val seconds = (totalTime / 1000) % 60
            val minutes = (totalTime / (1000 * 60) % 60)
            val hours = (totalTime / (1000 * 60 * 60) % 24)
            val mTotalDurationInSec = seconds + (minutes * 60) + (hours * 60 * 60)
            mPlayDurationInSec = mTotalDurationInSec.toInt()
            Log.e("onIsPlaying", "PLAYTIME: $playTime")
            Log.e("onIsPlaying", "PRESSEDPAUSE: $pressedPaused")
            Log.e("onIsPlaying", "PAUSETIME: $pauseTime")
            Log.e("onIsPlaying", "TOTALTIME: $totalTime")
            Log.e("onIsPlaying", "TOTALTIMEinSeconds: $mPlayDurationInSec")


            super.onIsPlayingChanged(eventTime, isPlaying)
        }
    }

    companion object {
        var lastQualityPosition = 0
        var lastSpeedPosition = 0
    }

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

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_course_details)
        mActivityBinding.aspect.setAspectRatio(16f / 9f)
        initViewModel()
        readValues()
        initView()
        userPreferences.getUserId.asLiveData().observeOnce(this) {
            if (it != null) {
                userId = it
                courseViewModel.findUser(userId)
                    .observeOnce(this) { result ->
                        if (result != null && result.size > 0) {
                            user = result[0]
                            initializeRecyclerView()
                            token = "Bearer " + result[0].access_token.toString()
                            mActivityBinding.mobileNumber.text = user.user_mobile_number
                            getCourseLecture(course.id)
                        }
                    }
            }

        }
        initPlayerView()
        initializePlayer()
        initZoomableView()
        createMediaItem(course.introUrl)
    }

    @SuppressLint("CutPasteId")
    private fun initPlayerView() {
        val builder = DefaultTrackSelector.ParametersBuilder( /* context= */this)
        trackSelectorParameters = builder.build()
        mActivityBinding.playerView.setControllerVisibilityListener(this)
        mActivityBinding.playerView.requestFocus()

        val fullScreenButton: AppCompatImageView =
            mActivityBinding.playerView.findViewById(R.id.exo_fullscreen_icon)
        val prevButton: AppCompatImageButton =
            mActivityBinding.playerView.findViewById(R.id.player_rewind)
        val forwardButton: AppCompatImageButton =
            mActivityBinding.playerView.findViewById(R.id.player_forward)
        val normalScreenButton: AppCompatImageView =
            mActivityBinding.playerView.findViewById(R.id.exo_fullscreen_icon)
        fullScreenButton.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_fullscreen_open
            )
        )
        normalScreenButton.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_fullscreen_close
            )
        )
        prevButton.setOnClickListener {
            player?.let {
                it.seekTo(it.currentPosition - 10000)
            }

        }
        forwardButton.setOnClickListener {
            player?.let {
                it.seekTo(it.currentPosition + 10000)
            }
        }
        normalScreenButton.setOnClickListener {
            if (!isFullScreen) {
                isFullScreen = true
                supportActionBar?.hide()
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                val displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                val layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                )
                mActivityBinding.aspect.layoutParams = layoutParams
            } else {
                isFullScreen = false
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                val layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    600
                )
                mActivityBinding.aspect.layoutParams = layoutParams

            }
        }
        mActivityBinding.btnBack.setOnClickListener {
            if (isFullScreen) {
                isFullScreen = false
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                val layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    600
                )
                mActivityBinding.aspect.layoutParams = layoutParams
            } else {

                Log.e("onIsPlaying", "newTOTALTIME: $totalTime")
                finish()
            }

        }
        mActivityBinding.btnSetting.setOnClickListener {
            mActivityBinding.qualityCard.visibility = View.VISIBLE
        }
        initializeQualityAdapter()

    }

    private fun initZoomableView() {
        val zoomableTextureView = mActivityBinding.playerView.findViewById<ZoomableTextureView>(R.id.textureView)

        player?.clearVideoSurface()
        mActivityBinding.playerView.removeView(zoomableTextureView)
        zoomableTextureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, p1: Int, p2: Int) {
                val s = Surface(surface)
                player?.setVideoSurface(s)
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, p1: Int, p2: Int) {}
            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }

        zoomableTextureView.setOnZoomableClickListener(object : HandlePlayerControllerListener {
            override fun onClick() {
                onVisibilityChange(if (mActivityBinding.playerView.isControllerVisible) View.GONE else View.VISIBLE)
            }
        })
    }

    private fun initView() {
        mActivityBinding.txtLectures.setTextColor(
            ResourcesCompat.getColor(resources, R.color.blue, null)
        )
        mActivityBinding.txtLectures.setOnClickListener {
            mActivityBinding.txtLectures.setTextColor(
                ResourcesCompat.getColor(resources, R.color.blue, null)
            )
            mActivityBinding.txtInfo.setTextColor(
                ResourcesCompat.getColor(resources, R.color.gray_purple, null)
            )
            mActivityBinding.lectureLay.visibility = View.VISIBLE
            mActivityBinding.infoLay.visibility = View.GONE
        }
        mActivityBinding.txtInfo.setOnClickListener {
            mActivityBinding.txtLectures.setTextColor(
                ResourcesCompat.getColor(resources, R.color.gray_purple, null)
            )
            mActivityBinding.txtInfo.setTextColor(
                ResourcesCompat.getColor(resources, R.color.blue, null)
            )
            mActivityBinding.lectureLay.visibility = View.GONE
            mActivityBinding.infoLay.visibility = View.VISIBLE
        }
        mActivityBinding.txtGroup.setOnClickListener {
            if (eligibleToWatch)
                callWinnerDialog()
            else
                invalidWatchDialog(getString(R.string.enrrol_first))

        }
        mActivityBinding.txtFolder.setOnClickListener {
            if (eligibleToWatch) {
                val intent = Intent(this, LectureFolderActivity::class.java)
                intent.putExtra(Constant.SELECTED_COURSE, course)
                intent.putExtra(Constant.USER_TOKEN, token)
                startActivity(intent)
            } else
                invalidWatchDialog(getString(R.string.enrrol_first))
        }
        mActivityBinding.txtAboutCourse.setOnClickListener {
            if (eligibleToWatch)
                openGroupDialog()
            else
                invalidWatchDialog(getString(R.string.enrrol_first))
        }
        mActivityBinding.speedTxt.setOnClickListener {
            if (currentSpeed == "1") {
                mActivityBinding.speedTxt.text = "1.25X"
                currentSpeed = "1.25"
                player?.setPlaybackSpeed(1.25f)
            } else if (currentSpeed == "1.25") {
                mActivityBinding.speedTxt.text = "1.5X"
                currentSpeed = "1.5"
                player?.setPlaybackSpeed(1.5f)
            } else if (currentSpeed == "1.5") {
                mActivityBinding.speedTxt.text = "1.75X"
                currentSpeed = "1.75"
                player?.setPlaybackSpeed(1.75f)
            } else if (currentSpeed == "1.75") {
                mActivityBinding.speedTxt.text = "2X"
                currentSpeed = "2"
                player?.setPlaybackSpeed(2f)
            } else if (currentSpeed == "2") {
                mActivityBinding.speedTxt.text = "1X"
                currentSpeed = "1"
                player?.setPlaybackSpeed(1f)
            }
        }

    }

    private fun readValues() {
        try {
            val i = intent
            course = i.getSerializableExtra(Constant.SELECTED_COURSE) as Course
            eligibleToWatch = i.getBooleanExtra(Constant.ELIGIBLE_TO_WATCH, false)
            mActivityBinding.txtLecId.text = course.name
            mActivityBinding.professorName.text = course.professor.name
            mActivityBinding.professorName.isVisible = !course.professor.name.isNullOrEmpty()
            mActivityBinding.txtLecNum.text = course.lecturesCount
            mActivityBinding.price.text = course.price.toString() + " " + getString(R.string.pound)
        } catch (e: Exception) {
            e.message
        }

    }

    private fun getCourseLecture(lecId: String) {
        courseViewModel.getCourseLecture(token, lecId)
        courseViewModel.courseLectureResponse.observeOnce(this) { kt ->
            if (kt != null) {
                courseLectures = kt.data.toMutableList()
                mAdapter.setData(kt.data)
            }
        }
        courseViewModel.mShowResponseError.observeOnce(this) {
        }
        courseViewModel.mShowProgressBar.observe(this) { bt ->
            if (bt) {
                //     showLoader()
            } else {
                //    hideLoader()
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

    private fun initViewModel() {
        courseViewModel = ViewModelProvider(
            this,
            ViewModelFactory(
                application,
                DatabaseHelperImpl(DatabaseBuilder.getInstance(this))
            )
        )[CourseViewModel::class.java]
        roomViewModel = ViewModelProvider(
            this,
            ViewModelFactory(
                application,
                DatabaseHelperImpl(DatabaseBuilder.getInstance(this))
            )
        )[RoomViewModel::class.java]
        roomViewModel.getLectures()
            .observe(this) { result ->
                lecturesDB = result
            }
    }

    private fun initializeRecyclerView() {
        mAdapter = CourseLectureAdapter(user, this, this)
        mActivityBinding.lectureRv.apply {
            setHasFixedSize(true)
            mActivityBinding.lectureRv.isNestedScrollingEnabled = false
            layoutManager = GridLayoutManager(this@CourseDetailsActivity, 1)
            adapter = mAdapter
        }
    }

    private fun initializePlayer() {
        if (player == null) {
            trackSelector = DefaultTrackSelector( /* context= */this)
            trackSelector?.parameters = trackSelectorParameters!!
            player = SimpleExoPlayer.Builder(this).build()
            mActivityBinding.playerView.player = player
        }
        player?.playWhenReady = true
        player?.seekTo(currentWindow, playbackPosition)
    }

    private fun createMediaItem(url: String?, pos: Long = 0) {
        playTime = 0L // in ms
        pauseTime = 0L // in ms
        totalTime = 0L // in ms
        initTime = 0L
        pressedPaused = 0
        mPlayDurationInSec = 0
//        roomViewModel.getLectures()
//            .observe(this) { result ->
//                lecturesDB = result
//                if (!resolutions.isNullOrEmpty() && !lecturesDB.isNullOrEmpty()) {
//                    //   Toast.makeText(this, lectureResponse.uuid, Toast.LENGTH_LONG).show()
//                    lecturesDB.firstOrNull { it.uuid == lectureResponse.id }?.let {
//
//                    }
//                }
//            }
        url?.also {
            val mediaItem = MediaItem.fromUri(it)
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.addAnalyticsListener(analyticsListener)
            player?.seekTo(0, pos)
        }
        if (resolutions.isEmpty()) {
            mActivityBinding.btnSetting.visibility = View.INVISIBLE
            mActivityBinding.mobileNumber.visibility = View.INVISIBLE
        } else {
            mActivityBinding.btnSetting.visibility = View.VISIBLE
            startAnimation()
        }
        player?.setPlaybackSpeed(1f)
    }

    private fun startAnimation() {
        val max = 9000
        val min = 3000
        val rand = Random()
       val randomNum: Long = (rand.nextInt(max - min) + min).toLong()
        val display: Display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width: Int = size.x - 260
        mActivityBinding.mobileNumber.visibility = View.VISIBLE
        mActivityBinding.mobileNumber.clearAnimation()
        val displaymetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displaymetrics)
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    val R = Random()
                    val dx = R.nextFloat() * width
                    val dy = R.nextFloat() * 450
                    mActivityBinding.mobileNumber.animate()
                        .x(dx)
                        .y(dy)
                        .setDuration(0)
                        .start()
                }
            }
        }, 0, randomNum)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRecyclerViewItemClick(position: Int) {
        if (!eligibleToWatch) {
            invalidWatchDialog(getString(R.string.enrrol_first))
        } else {
            lastQualityPosition = 0
            selectedPosition = position
            resolutions.clear()
            player?.let {
                it.playWhenReady = false
                it.stop()
                lastDuration = (totalTime / 1000) % 60
                if (lastLecId.isNotEmpty() && duration != 0) {
                    isBack = true

                    val lecture = WatchedLectures(selectedLectureId, it.currentPosition)
                    roomViewModel.updateLecture(lecture)
                    val position = it.currentPosition
                    val playedSec: Int = (playTime.div(1000)).toInt()
                    if (playTime > 0 && position > 0)
                        initAddWatch(lastLecId, playedSec, position)


                }
            }
            if (eligibleToWatch) {
                initLectureInfo(courseLectures[position].id)
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onQualitySelected(position: Int) {
        lastQualityPosition = position
        qualityAdapter.notifyDataSetChanged()
        mActivityBinding.qualityCard.visibility = View.INVISIBLE
        link = resolutions[position].link
        val time = player?.currentPosition ?: 0
        createMediaItem(link)
        player?.seekTo(0, time)
    }

    override fun openFile(url: String) {
    }

    fun onStartWatchClicked() {
        val watchedLectures = WatchedLectures(lectureResponse.id, 0)
        roomViewModel.addLecture(watchedLectures)
        startTime = getStartDate()
        endTime = getStartDate(sessionTimeout)
//        releasePlayer()
        initializePlayer()
        printDifferenceDateForHours(startTime, endTime)
        createMediaItem(resolutions.firstOrNull()?.link)
        initAddSession(selectedLectureId)
    }

    private fun initLectureInfo(lecId: String) {
        selectedLectureId = lecId
        courseViewModel.getLectureInfo(token, lecId)
        courseViewModel.lectureResponse.observeOnce(this) { kt ->
            if (kt != null) {
                lastLecId = kt.data.id
                getResolutions(kt)
                sessionTimeout = lectureResponse.sessionTimeout
                checkVideoSession()
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

    private fun checkVideoSession() {
        var expired = true
        startTime = getStartDate()
        endTime = lectureResponse.studentSessions.lastOrNull()?.expiredAt ?: ""
        if (endTime.isNotEmpty())
            expired = isSessionExpired(startTime, endTime)
        qualityAdapter.setData(resolutions)
        val lecture = lecturesDB.filter { it.uuid == lectureResponse.id }
//        if (lectureResponse.allowedSessions - lectureResponse.actualSessions == 0 &&
//            expired
//        ) {
//            invalidWatchDialog(getString(R.string.excced_watch))
//        } else if (lectureResponse.studentSessions.isEmpty())
//            LectureWatchDialog.newInstance(lectureResponse)
//                .show(
//                    supportFragmentManager,
//                    LectureWatchDialog.TAG
//                )
//        else if (lecture.isNotEmpty() &&
//            lectureResponse.actualSessions <= lectureResponse.allowedSessions
//        ) {
//            releasePlayer()
//            initializePlayer()
//            clearTimer()
//            if (lectureResponse.studentSessions.isNotEmpty()) {
//                startTime = getStartDate()
//                endTime = getStartDate(sessionTimeout)
//                printDifferenceDateForHours(startTime, endTime)
//            }
//            if (resolutions.size > 0)
//                createMediaItem(resolutions.firstOrNull()?.link)
//        } else if (
//            lectureResponse.actualSessions <= lectureResponse.allowedSessions
//        ) {
//            releasePlayer()
//            initializePlayer()
//            clearTimer()
//            startTime = getStartDate()
//            endTime = lectureResponse.studentSessions.lastOrNull()?.expiredAt ?: ""
//            printDifferenceDateForHours(startTime, endTime)
//            createMediaItem(resolutions.firstOrNull()?.link)
        if (!lectureResponse.url.isNullOrEmpty() || !lectureResponse.resolutions.isNullOrEmpty()) {
            if (lectureResponse.studentSessions.isEmpty() || expired)
                LectureWatchDialog.newInstance(lectureResponse)
                    .show(
                        supportFragmentManager,
                        LectureWatchDialog.TAG
                    )
            else if (!expired) {
//                releasePlayer()
                initializePlayer()
                clearTimer()
                printDifferenceDateForHours(startTime, endTime)
                createMediaItem(
                    resolutions.firstOrNull()?.link,
                    lectureResponse.studentSessions.lastOrNull()?.videoPosition ?: 0
                )
            } else {
                invalidWatchDialog(getString(R.string.excced_watch))
            }
        } else {
            invalidWatchDialog(getString(R.string.excced_watch))
        }
    }

    private fun getResolutions(kt: LectureDetailsResponse) {
        resolutions.clear()
        lectureResponse = kt.data
        duration = kt.data.duration
        val files: JSONArray?
//        val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        if (!kt.data.resolutions.isNullOrEmpty()) {
            val x = AESUtils.decryptResponse(kt.data.resolutions)
            files = JSONArray(x)
            files.let {
                for (i in 0 until it.length()) {
                    val winspeed = it.getString(i)
                    val jsonObject: JSONObject?
                    jsonObject = JSONObject(winspeed)
                    resolutions.add(
                        i,
                        Files(jsonObject.getString("quality"), jsonObject.getString("link"))
                    )
                }
            }
        } else if (!kt.data.url.isNullOrEmpty()) {
            val x = AESUtils.decryptResponse(kt.data.url)
            x?.let {
                resolutions.add(Files("High", it))
            }
        } else
            invalidWatchDialog(getString(R.string.excced_watch))
    }

    private fun initAddSession(lecId: String) {
        courseViewModel.addSession(token, lecId)
        courseViewModel.sessionResponse.observeOnce(this) { kt ->
            if (kt != null) {
//                if (isBack)
//                    finish()
                //       initializePlayer()
                //     printDifferenceDateForHours(startTime, endTime)
                //      createMediaItem(resolutions[0].link)
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

    private fun initAddWatch(lecId: String, progress: Int, position: Long) {
        courseViewModel.addWatch(token, lecId, progress, position)
        courseViewModel.watchResponse.observeOnce(this) { kt ->

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

    private fun getStartDate(hours: Int): String {
        val c = Calendar.getInstance(Locale.ENGLISH).time
        val df = SimpleDateFormat("MM/dd/yyyy hh:mm aa", Locale.getDefault())
        val formattedDate: String = df.format(c)
        val d = df.parse(formattedDate)
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.time = d
        cal.add(Calendar.HOUR_OF_DAY, hours)
        return df.format(cal.time)
    }


    private fun getStartDate(): String {
        val c = Calendar.getInstance(Locale.ENGLISH).time
        val df = SimpleDateFormat("MM/dd/yyyy hh:mm aa", Locale.getDefault())
        val formattedDate: String = df.format(c)
        val d = df.parse(formattedDate)
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.time = d
        return df.format(cal.time)
    }

    private fun isSessionExpired(strTime: String, endTime: String): Boolean {
        val format1 = SimpleDateFormat("MM/dd/yyyy hh:mm aa", Locale.getDefault())
        val currentTime = format1.parse(strTime)
        val endDate = format1.parse(endTime)
        val different = endDate.time - currentTime.time
        return different <= 0
    }

    private fun printDifferenceDateForHours(strTime: String, endTime: String) {
        val format1 = SimpleDateFormat("MM/dd/yyyy hh:mm aa", Locale.getDefault())
        val currentTime = format1.parse(strTime)
        val endDate = format1.parse(endTime)
        val different = endDate.time - currentTime.time
        countDownTimer = object : CountDownTimer(different, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsInMilli: Long = 1000
                val minutesInMilli = secondsInMilli * 60
                minutesInMilli * 60

            }

            override fun onFinish() {
                releasePlayer()
                invalidWatchDialog(getString(R.string.excced_watch))
            }
        }.start()
    }

    private fun clearTimer() {
        countDownTimer?.cancel()
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        mActivityBinding.playerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }


    override fun onResume() {
        super.onResume()
        if (lastLecId.isNotEmpty()) {
            initLectureInfo(lastLecId)
        } else {
            hideSystemUi()
//            if ((Util.SDK_INT < 24 || player == null)) {
            initializePlayer()
//            }
            mMediaRouter = MediaRouter.getInstance(this)
            val fm = supportFragmentManager
            val fragment: DiscoveryFragment?
            fragment = DiscoveryFragment()
            fragment.setCallback(mMediaRouterCB)
            fm.beginTransaction().add(fragment, DISCOVERY_FRAGMENT_TAG).commit()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            player?.let {
                it.playWhenReady = false
                it.stop()
                lastDuration = (totalTime / 1000) % 60
                if (lastLecId.isNotEmpty() && duration != 0) {
                    isBack = true
                    val lecture = WatchedLectures(selectedLectureId, it.currentPosition)
                    roomViewModel.updateLecture(lecture)
                    val progress = it.currentPosition
                    val playedSec: Int = (playTime.div(1000)).toInt()
                    if (lastDuration > 0 && progress > 0)
                        initAddWatch(lastLecId, playedSec, progress)

                }
            }

        } catch (e: Exception) {
            e.message
        }

    }

    override fun onStop() {
        super.onStop()
//        if (Util.SDK_INT >= 24) {
        releasePlayer()
//        }
    }


    private fun releasePlayer() {
        player?.let {
            playWhenReady = it.playWhenReady
            playbackPosition = it.currentPosition
            currentWindow = it.currentWindowIndex
            it.release()
            player = null
        }
    }

    private fun openGroupDialog() {
        lateinit var dialog: AlertDialog
        val inflater = LayoutInflater.from(this)
        val builder: AlertDialog.Builder = AlertDialog.Builder(
            this,
            R.style.CustomDialog
        )
        val customLayout: CallDialogBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.call_dialog,
            null,
            false
        )
        builder.setCancelable(true)
        builder.setView(customLayout.root)
        dialog = builder.create()
        customLayout.firstNumber.text = getString(R.string.whatsApp)
        customLayout.secondNumber.text = getString(R.string.Facebook)
        customLayout.callButton.setOnClickListener {
            dialog.dismiss()
        }
        customLayout.firstNumber.setOnClickListener {
            val intent = Intent(this, WebActivity::class.java)
            intent.putExtra(Constant.WEB_URL, course.whatsapp)
            startActivity(intent)
        }
        customLayout.secondNumber.setOnClickListener {
            val intent = Intent(this, WebActivity::class.java)
            intent.putExtra(Constant.WEB_URL, course.facebook)
            startActivity(intent)
        }
        dialog.show()
    }

    private fun callWinnerDialog() {
        var firstNumber = ""
        var secondNumber = ""
        if (!course.firstPhone.isNullOrEmpty())
            firstNumber = course.firstPhone
        else if (!course.secondPhone.isNullOrEmpty())
            secondNumber = course.secondPhone
        lateinit var dialog: AlertDialog
        val inflater = LayoutInflater.from(this)
        val builder: AlertDialog.Builder = AlertDialog.Builder(
            this,
            R.style.CustomDialog
        )
        val customLayout: CallDialogBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.call_dialog,
            null,
            false
        )
        builder.setCancelable(true)
        builder.setView(customLayout.root)
        dialog = builder.create()
        if (firstNumber.isEmpty())
            customLayout.firstNumber.visibility = View.GONE
        if (secondNumber.isEmpty())
            customLayout.secondNumber.visibility = View.GONE
        customLayout.firstNumber.text = firstNumber
        customLayout.secondNumber.text = secondNumber
        customLayout.callButton.setOnClickListener {
            dialog.dismiss()
        }
        customLayout.firstNumber.setOnClickListener {
            try {
                val url = "https://api.whatsapp.com/send?phone=$firstNumber"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            } catch (e: Exception) {

            }
            dialog.dismiss()
        }
        customLayout.secondNumber.setOnClickListener {
            try {
                val url = "https://api.whatsapp.com/send?phone=$secondNumber"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            } catch (e: Exception) {

            }
        }
        dialog.show()
    }

    private fun invalidWatchDialog(title: String) {
//        val inflater = LayoutInflater.from(applicationContext)
        val builder: AlertDialog.Builder = AlertDialog.Builder(
            this,
            R.style.CustomDialog
        )
        val customLayout = InvalidWatchDialogBinding.inflate(layoutInflater)
        customLayout.txtTitle.text = title
        builder.setCancelable(true)
        builder.setView(customLayout.root)
        val dialog: AlertDialog = builder.create()
        customLayout.callButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }


    override fun onVisibilityChange(visibility: Int) {
        if (visibility == View.GONE) {
            mActivityBinding.playerView.hideController()
            mActivityBinding.btnBack.visibility = View.INVISIBLE
            mActivityBinding.btnSetting.visibility = View.INVISIBLE
            mActivityBinding.qualityCard.visibility = View.INVISIBLE
            mActivityBinding.speedTxt.visibility = View.INVISIBLE
        } else {
            mActivityBinding.playerView.showController()
            mActivityBinding.btnBack.visibility = View.VISIBLE
            mActivityBinding.speedTxt.visibility = View.VISIBLE
            if (resolutions.isNotEmpty())
                mActivityBinding.btnSetting.visibility = View.VISIBLE
            else
                mActivityBinding.btnSetting.visibility = View.INVISIBLE
        }

    }

    private fun initializeQualityAdapter() {
        qualityAdapter = QualityAdapter(this, this)
        mActivityBinding.rvQuality.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@CourseDetailsActivity, 1)
            adapter = qualityAdapter
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI2()
        }
    }

    fun hideSystemUI2() {
        // Enables  "lean back" mode
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        val decorView = window.decorView
        decorView.systemUiVisibility = ( // Hide the nav bar and status bar
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }


}