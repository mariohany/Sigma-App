package com.noob.apps.mvvmcountries.ui.course

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
import androidx.recyclerview.widget.GridLayoutManager
import com.noob.apps.mvvmcountries.R
import com.noob.apps.mvvmcountries.adapters.CourseAdapter
import com.noob.apps.mvvmcountries.adapters.NewRecyclerViewClickListener
import com.noob.apps.mvvmcountries.data.DatabaseBuilder
import com.noob.apps.mvvmcountries.data.DatabaseHelperImpl
import com.noob.apps.mvvmcountries.databinding.FragmentCoursesBinding
import com.noob.apps.mvvmcountries.models.Course
import com.noob.apps.mvvmcountries.ui.base.BaseFragment
import com.noob.apps.mvvmcountries.ui.details.CourseDetailsActivity
import com.noob.apps.mvvmcountries.ui.dialog.BlockUserDialog
import com.noob.apps.mvvmcountries.ui.dialog.ConnectionDialogFragment
import com.noob.apps.mvvmcountries.ui.login.LoginActivity
import com.noob.apps.mvvmcountries.utils.Constant
import com.noob.apps.mvvmcountries.utils.ViewModelFactory
import com.noob.apps.mvvmcountries.viewmodels.CourseViewModel
import com.scottyab.rootbeer.RootBeer
import kotlinx.coroutines.launch

class CoursesFragment : BaseFragment(),
    NewRecyclerViewClickListener<Course> {
    private lateinit var mActivityBinding: FragmentCoursesBinding
    private val courses: MutableList<Course> = mutableListOf()
    private lateinit var mAdapter: CourseAdapter
    private lateinit var courseViewModel: CourseViewModel
    private var userId = ""
    private var token = ""
    private var fcmToken = ""
    private var refreshToken = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mActivityBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_courses, container, false)
        // Inflate the layout for this fragment
        return mActivityBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        token = ""
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

    private fun initInfoObservers() {
        courseViewModel.getStudentInfo(token)
        courseViewModel.infoResponse.observeOnce(viewLifecycleOwner) { kt ->
            if (kt != null) {
                if (!kt.data.enabled)
                    BlockUserDialog.newInstance("")
                        .show(requireActivity().supportFragmentManager, BlockUserDialog.TAG)
                if (kt.data.deviceId != deviceId)
                    BlockUserDialog.newInstance("App installed on other device") {
                        lifecycleScope.launch {
                            userPreferences.clear()
                        }
                        logOut()
                    }
                        .show(requireActivity().supportFragmentManager, BlockUserDialog.TAG)
            }
        }
        courseViewModel.mShowResponseError.observeOnce(viewLifecycleOwner) {
            AlertDialog.Builder(requireActivity()).setMessage(it).show()
        }
        courseViewModel.mShowProgressBar.observe(viewLifecycleOwner) { bt ->


        }
        courseViewModel.mShowNetworkError.observeOnce(viewLifecycleOwner) {
            if (it != null) {
                ConnectionDialogFragment.newInstance(Constant.RETRY_LOGIN)
                    .show(requireActivity().supportFragmentManager, ConnectionDialogFragment.TAG)
            }

        }
    }


    private fun initializeRecyclerView() {
        mAdapter = CourseAdapter(this)
        mActivityBinding.rvLectures.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = mAdapter
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
                                userPreferences.saveRefreshToken(refreshToken)
                            }
                            initializeObservers()
                            initInfoObservers()
                        } else {
                            logOut()
                        }

                    }
            }
        }
    }

    private fun initializeObservers() {
        courseViewModel.getStudentCourses(token)
        courseViewModel.myCourseResponse.observeOnce(viewLifecycleOwner) { kt ->
            if (kt != null) {
                courses.clear()
                courses.addAll(kt.data.toMutableList())
                courses.forEach {
                    it.eligibleToWatch = true
                }
                mAdapter.setData(courses)
                if (courses.isNotEmpty()) {
                    mActivityBinding.dataLayout.visibility = View.VISIBLE
                    mActivityBinding.emptyLayout.visibility = View.INVISIBLE
                }

            }
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

    override fun onItemClick(item: Course) {
        val intent = Intent(requireContext(), CourseDetailsActivity::class.java)
        intent.putExtra(Constant.SELECTED_COURSE, item)
        intent.putExtra(Constant.ELIGIBLE_TO_WATCH, item.eligibleToWatch)
        startActivity(intent)
    }
}