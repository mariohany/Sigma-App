package com.noob.apps.mvvmcountries.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.messaging.FirebaseMessaging
import com.noob.apps.mvvmcountries.R
import com.noob.apps.mvvmcountries.adapters.NewRecyclerViewClickListener
import com.noob.apps.mvvmcountries.adapters.SectionAdapter
import com.noob.apps.mvvmcountries.data.DatabaseBuilder
import com.noob.apps.mvvmcountries.data.DatabaseHelperImpl
import com.noob.apps.mvvmcountries.databinding.FragmentHomeBinding
import com.noob.apps.mvvmcountries.models.Course
import com.noob.apps.mvvmcountries.models.RefreshTokenModel
import com.noob.apps.mvvmcountries.ui.base.BaseFragment
import com.noob.apps.mvvmcountries.ui.details.CourseDetailsActivity
import com.noob.apps.mvvmcountries.ui.dialog.BlockUserDialog
import com.noob.apps.mvvmcountries.ui.dialog.ConnectionDialogFragment
import com.noob.apps.mvvmcountries.ui.fcm.MyFirebaseMessagingService
import com.noob.apps.mvvmcountries.ui.login.LoginActivity
import com.noob.apps.mvvmcountries.utils.Constant
import com.noob.apps.mvvmcountries.utils.ViewModelFactory
import com.noob.apps.mvvmcountries.viewmodels.CourseViewModel
import com.scottyab.rootbeer.RootBeer
import kotlinx.coroutines.launch


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : BaseFragment(),
    NewRecyclerViewClickListener<Course> {
    private lateinit var mActivityBinding: FragmentHomeBinding
    private var sections: Map<String, List<Course>> = mapOf()
    private lateinit var mAdapter: SectionAdapter
    private var param1: String? = null
    private var param2: String? = null
    private var userId = ""
    private var token = ""
    private var fcmToken = ""
    private var refreshToken = ""
    private lateinit var courseViewModel: CourseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mActivityBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        // Inflate the layout for this fragment
        return mActivityBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        token = ""
        fcmToken = MyFirebaseMessagingService.mToken
        courseViewModel = ViewModelProvider(
            this,
            ViewModelFactory(
                requireActivity().application,
                DatabaseHelperImpl(DatabaseBuilder.getInstance(requireContext()))
            )
        )[CourseViewModel::class.java]
        initializeRecyclerView()
        val rootBeer = RootBeer(context)
        if (rootBeer.isRooted) {
            return BlockUserDialog.newInstance("App can't run on RootedDevice")
                .show(requireActivity().supportFragmentManager, BlockUserDialog.TAG)
        } else
            getData()
//        userPreferences.getFCMToken.asLiveData().observeOnce(viewLifecycleOwner, {
//            fcmToken = it
//
//        })
        mActivityBinding.swipeContainer.setOnRefreshListener {
            mActivityBinding.swipeContainer.isRefreshing = false
            getData()
        }


    }

    private fun getData() {
        userPreferences.getUserId.asLiveData().observeOnce(viewLifecycleOwner) {
            if (it != null) {
                userId = it
                courseViewModel.findUser(userId)
                    .observeOnce(viewLifecycleOwner) { result ->
                        if (result != null && result.size > 0) {
                            token = "Bearer " + result[0].access_token.toString()
                            refreshToken = result[0].refresh_token.toString()
                            lifecycleScope.launch {
                                userPreferences.saveUserToken(token)
                            }
                            lifecycleScope.launch {
                                userPreferences.saveRefreshToken(refreshToken)
                            }
                            initializeObservers()
                        } else {
                            logOut()
                        }

                    }
            }
        }
    }

    private fun initializeRecyclerView() {
        mAdapter = SectionAdapter(this)
        mActivityBinding.rvLectures.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
    }

    private fun initializeObservers() {
        courseViewModel.getDepartmentCourses(token)
        courseViewModel.depResponse.observeOnce(viewLifecycleOwner) { kt ->
            if (kt != null) {
                sections = kt.data.toMutableList().groupBy { it.subject }
                mAdapter.setData(sections)
                initInfoObservers()
            }
        }
        courseViewModel.mShowResponseError.observeOnce(viewLifecycleOwner) {
            initTokenObservers()
        }
        courseViewModel.mShowProgressBar.observe(viewLifecycleOwner) { bt ->
            if (bt) {
                showLoader()
            } else {
                hideLoader()
            }
        }
        courseViewModel.mShowNetworkError.observeOnce(viewLifecycleOwner) {
            if (it != null) {
                ConnectionDialogFragment.newInstance(Constant.RETRY_LOGIN)
                    .show(requireActivity().supportFragmentManager, ConnectionDialogFragment.TAG)
            }

        }
    }

    private fun initInfoObservers() {
        with(courseViewModel) {
            getStudentInfo(token)
            infoResponse.observeOnce(viewLifecycleOwner) { kt ->
                if (kt != null) {
                    lifecycleScope.launch {
                        if (!kt.data.webView.isNullOrEmpty())
                            userPreferences.setWebViewData(kt.data.webView)
                    }
                    mActivityBinding.txtFaculty.text = kt.data.studyFieldName
                    mActivityBinding.txtDepartment.text =
                        kt.data.levelName + " " + kt.data.departmentName
                    if (!kt.data.enabled)
                        BlockUserDialog.newInstance("")
                            .show(requireActivity().supportFragmentManager, BlockUserDialog.TAG)
//                    if (fcmToken.isNotEmpty())
//                        initFCMTokenObservers()
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            return@addOnCompleteListener
                        }

                        initFCMTokenObservers(task.result)
                    }
                    if (kt.data.deviceId == null) {
//                        addDeviceId()
                        logOut()
                    }
                    if (kt.data.deviceId != null && kt.data.deviceId != deviceId)
                        BlockUserDialog.newInstance("App installed on other device") {
                            lifecycleScope.launch {
                                userPreferences.clear()
                            }
                            logOut()
                        }
                            .show(requireActivity().supportFragmentManager, BlockUserDialog.TAG)
                }
            }
            mShowResponseError.observeOnce(viewLifecycleOwner) {
                AlertDialog.Builder(requireActivity()).setMessage(it).show()
            }
            mShowProgressBar.observe(viewLifecycleOwner) { bt ->
                if (bt) {
                    showLoader()
                } else {
                    hideLoader()
                }

            }
            mShowNetworkError.observeOnce(viewLifecycleOwner) {
                if (it != null) {
                    ConnectionDialogFragment.newInstance(Constant.RETRY_LOGIN)
                        .show(
                            requireActivity().supportFragmentManager,
                            ConnectionDialogFragment.TAG
                        )
                }
            }
        }
    }

    private fun initTokenObservers() {
        courseViewModel.updateToken(RefreshTokenModel(Constant.REFRESH_TOKEN, refreshToken))
        courseViewModel.updateTokenResponse.observeOnce(viewLifecycleOwner) { kt ->
            if (kt != null) {
                getData()
            }
        }
        courseViewModel.mShowResponseError.observeOnce(viewLifecycleOwner) {
            logOut()
        }
        courseViewModel.mShowProgressBar.observe(viewLifecycleOwner) { bt ->
            if (bt) {
                showLoader()
            } else {
                hideLoader()
            }

        }
        courseViewModel.mShowNetworkError.observeOnce(viewLifecycleOwner) {
            if (it != null) {
                ConnectionDialogFragment.newInstance(Constant.RETRY_LOGIN)
                    .show(requireActivity().supportFragmentManager, ConnectionDialogFragment.TAG)
            }

        }
    }

    private fun logOut() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finishAffinity()
    }

    private fun initFCMTokenObservers(fcm: String) {
        courseViewModel.updateFCMToken(token, fcm)
        courseViewModel.fcmResponse.observeOnce(viewLifecycleOwner) { kt ->

        }
        courseViewModel.mShowResponseError.observeOnce(viewLifecycleOwner) {
        }
        courseViewModel.mShowProgressBar.observe(viewLifecycleOwner) { bt ->
            if (bt) {
                showLoader()
            } else {
                hideLoader()
            }

        }
        courseViewModel.mShowNetworkError.observeOnce(viewLifecycleOwner) {
            if (it != null) {
                ConnectionDialogFragment.newInstance(Constant.RETRY_LOGIN)
                    .show(requireActivity().supportFragmentManager, ConnectionDialogFragment.TAG)
            }

        }
    }

    private fun addDeviceId() {
        courseViewModel.addDeviceId(token, deviceId)
        courseViewModel.deviceIdResponse.observeOnce(viewLifecycleOwner) { kt ->
            if (kt != null) {
            }
        }
        courseViewModel.mShowResponseError.observeOnce(viewLifecycleOwner) {
            AlertDialog.Builder(requireActivity()).setMessage(it).show()
        }
        courseViewModel.mShowProgressBar.observe(viewLifecycleOwner) { bt ->
            if (bt) {
                showLoader()
            } else {
                hideLoader()
            }

        }
        courseViewModel.mShowNetworkError.observeOnce(viewLifecycleOwner) {
            if (it != null) {
                ConnectionDialogFragment.newInstance(Constant.RETRY_LOGIN)
                    .show(requireActivity().supportFragmentManager, ConnectionDialogFragment.TAG)
            }

        }
    }


    companion object {
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(item: Course) {
        val intent = Intent(requireContext(), CourseDetailsActivity::class.java)
        item.id = item.courseId
        intent.putExtra(Constant.SELECTED_COURSE, item)
        intent.putExtra(Constant.ELIGIBLE_TO_WATCH, item.eligibleToWatch)
        startActivity(intent)
    }
}