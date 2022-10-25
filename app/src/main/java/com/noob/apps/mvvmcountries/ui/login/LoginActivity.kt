package com.noob.apps.mvvmcountries.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.mediarouter.media.MediaRouter
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.safetynet.SafetyNet
import com.noob.apps.mvvmcountries.R
import com.noob.apps.mvvmcountries.data.DatabaseBuilder
import com.noob.apps.mvvmcountries.data.DatabaseHelperImpl
import com.noob.apps.mvvmcountries.data.RoomViewModel
import com.noob.apps.mvvmcountries.databinding.ActivityLoginBinding
import com.noob.apps.mvvmcountries.ui.base.BaseActivity
import com.noob.apps.mvvmcountries.ui.dialog.BlockUserDialog
import com.noob.apps.mvvmcountries.ui.dialog.ConnectionDialogFragment
import com.noob.apps.mvvmcountries.ui.forgetpassword.ForgetPasswordActivity
import com.noob.apps.mvvmcountries.ui.home.HomeActivity
import com.noob.apps.mvvmcountries.ui.signup.SignUpActivity
import com.noob.apps.mvvmcountries.ui.visitor.VisitorActivity
import com.noob.apps.mvvmcountries.ui.welcome.UniversityActivity
import com.noob.apps.mvvmcountries.utils.Constant
import com.noob.apps.mvvmcountries.utils.MobileNumberValidator
import com.noob.apps.mvvmcountries.utils.ViewModelFactory
import com.noob.apps.mvvmcountries.viewmodels.LoginViewModel
import com.noob.apps.mvvmcountries.viewmodels.SharedViewModel
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.TimeUnit

class LoginActivity : BaseActivity() {
    private val TAG = "LoginActivity"
    private var isSaved = false
    private lateinit var mobileNumber: String
    private lateinit var password: String
    private lateinit var mActivityBinding: ActivityLoginBinding
    private lateinit var mViewModel: LoginViewModel
    private lateinit var roomViewModel: RoomViewModel
    private lateinit var sharedViewModel: SharedViewModel
    private var mMediaRouter: MediaRouter? = null
    private val DISCOVERY_FRAGMENT_TAG = "DiscoveryFragment"
    private var token = ""
    private val mRandom: Random = SecureRandom()


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
            DataBindingUtil.setContentView(this, R.layout.activity_login)
        mActivityBinding.txtVisitor.setOnClickListener {
            startActivity(Intent(this@LoginActivity, VisitorActivity::class.java))
        }
        mActivityBinding.txtForgetPassword.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ForgetPasswordActivity::class.java))
        }
        mActivityBinding.txtCreateNewAccount.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
        }

        mViewModel = ViewModelProvider(
            this,
            ViewModelFactory(
                application,
                DatabaseHelperImpl(DatabaseBuilder.getInstance(applicationContext))
            )
        ).get(LoginViewModel::class.java)

        roomViewModel = ViewModelProvider(
            this,
            ViewModelFactory(
                application,
                DatabaseHelperImpl(DatabaseBuilder.getInstance(applicationContext))
            )
        ).get(RoomViewModel::class.java)
        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
        sharedViewModel.operation.observe(this, Observer {
            if (it == Constant.RETRY_LOGIN)
                initializeObservers()
        })
        mActivityBinding.loginButton.setOnClickListener {
            hideKeyboard()
            mobileNumber = mActivityBinding.etMobileNumber.text.toString()
            password = mActivityBinding.etPassword.text.toString()
            if (checkValidation()) {
                initializeObservers()
            }

        }
        userPreferences.getUniversityData.asLiveData().observeOnce(this) {
            isSaved = it
        }
        userPreferences.getUserToken.asLiveData().observeOnce(this) {
            token = it
            //     if (token.isNotEmpty())
            //  initInfoObservers()
        }
        val nonceData = "Safety Net Sample: " + System.currentTimeMillis()
        val nonce: ByteArray? = getRequestNonce(nonceData)
        if (nonce != null) {
            SafetyNet.getClient(this).attest(nonce, getString(R.string.safety_net_api_key))
                .addOnSuccessListener(this) {
                    // Indicates communication with the service was successful.
                    // Use response.getJwsResult() to get the result data.
                }
                .addOnFailureListener(this) { e ->
                    // An error occurred while communicating with the service.
                    if (e is ApiException) {
                        // An error with the Google Play services API contains some
                        // additional details.
                        val apiException = e as ApiException

                        // You can retrieve the status code using the
                        // apiException.statusCode property.
                        BlockUserDialog.newInstance("App not working on Disable Flag Secure")
                            .show(supportFragmentManager, BlockUserDialog.TAG)
                    } else {
                        // A different, unknown type of error occurred.
                        Log.d(
                            TAG,
                            "Error=${e.message}"
                        )
                        BlockUserDialog.newInstance("App not working on Disable Flag Secure")
                            .show(supportFragmentManager, BlockUserDialog.TAG)
                    }
                }
        }
        SafetyNet.getClient(this)
            .isVerifyAppsEnabled
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result.isVerifyAppsEnabled) {
                        Log.d("MY_APP_TAG", "The Verify Apps feature is enabled.")
                    } else {
                        Log.d("MY_APP_TAG", "The Verify Apps feature is disabled.")
                        BlockUserDialog.newInstance("App not working on Disable Flag Secure")
                            .show(supportFragmentManager, BlockUserDialog.TAG)
                    }
                } else {
                    Log.e("MY_APP_TAG", "A general error occurred.")
                    BlockUserDialog.newInstance("App not working on Disable Flag Secure")
                        .show(supportFragmentManager, BlockUserDialog.TAG)
                }
            }
        SafetyNet.getClient(this)
            .listHarmfulApps()
            .addOnCompleteListener { task ->
                Log.d(TAG, "Received listHarmfulApps() result")

                if (task.isSuccessful) {
                    val result = task.result
                    val scanTimeMs = result.lastScanTimeMs

                    val appList = result.harmfulAppsList
                    if (appList.isNotEmpty()) {
                        BlockUserDialog.newInstance("please remove harmful app from your phone")
                            .show(supportFragmentManager, BlockUserDialog.TAG)

                        for (harmfulApp in appList) {
                            Log.e("MY_APP_TAG", "Information about a harmful app:")
                            Log.e("MY_APP_TAG", "  APK: ${harmfulApp.apkPackageName}")
                            Log.e("MY_APP_TAG", "  SHA-256: ${harmfulApp.apkSha256}")

                            // Categories are defined in VerifyAppsConstants.
                            Log.e("MY_APP_TAG", "  Category: ${harmfulApp.apkCategory}")
                        }
                    } else {
                        Log.d(
                            "MY_APP_TAG",
                            "There are no known potentially harmful apps installed."
                        )
                    }
                } else {
                    Log.d(
                        "MY_APP_TAG",
                        "An error occurred. Call isVerifyAppsEnabled() to ensure that the user "
                                + "has consented."
                    )
                    BlockUserDialog.newInstance("please remove harmful app from your phone")
                        .show(supportFragmentManager, BlockUserDialog.TAG)
                }
            }
    }

    private fun getRequestNonce(data: String): ByteArray? {
        val byteStream = ByteArrayOutputStream()
        val bytes = ByteArray(24)
        mRandom.nextBytes(bytes)
        try {
            byteStream.write(bytes)
            byteStream.write(data.toByteArray())
        } catch (e: IOException) {
            return null
        }
        return byteStream.toByteArray()
    }

    private fun checkValidation(): Boolean {
        var isValid = true
        if (!MobileNumberValidator.validCellPhone(mobileNumber)) {
            mActivityBinding.etMobileNumber.error = getString(R.string.invalid_mobile_number)
            isValid = false
        }
        if (password.isEmpty()) {
            mActivityBinding.etPassword.error = getString(R.string.invalid_password)
            isValid = false
        }
        return isValid
    }

    private fun initializeObservers() {
        mViewModel.fetchCountriesFromServer(mobileNumber, password, deviceId)
            .observeOnce(this) { user ->
                if (user != null) {
//                    if (token.isEmpty() && user.user_device_id != deviceId)
//                        BlockUserDialog.newInstance("App installed on other device")
//                            .show(supportFragmentManager, BlockUserDialog.TAG)
//                    else {

                    lifecycleScope.launch {
                        user.user_id?.let { userPreferences.saveUserId(it) }
                    }
                    lifecycleScope.launch {
                        userPreferences.saveUserLogedIn(true)
                    }
                    lifecycleScope.launch {
                        user.refresh_token?.let { userPreferences.saveRefreshToken(it) }
                    }

                    initInfoObservers("Bearer " + user.access_token.toString())
                }
                //    }
            }
        mViewModel.mShowResponseError.observeOnce(this, Observer {
            AlertDialog.Builder(this).setMessage(it).show()
        })
        mViewModel.mShowProgressBar.observe(this, Observer { bt ->
            if (bt) {
                showLoader()
            } else {
                hideLoader()
            }
        })
        mViewModel.mShowNetworkError.observe(this, Observer {
            ConnectionDialogFragment.newInstance(Constant.RETRY_LOGIN)
                .show(supportFragmentManager, ConnectionDialogFragment.TAG)

        })
    }

    private fun checkUserOnBoard(onboarded: Boolean) {
        if (onboarded) {
            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
            finish()
        } else if (!isSaved) {
            startActivity(Intent(this@LoginActivity, UniversityActivity::class.java))
            finish()
        }

    }

    private fun initInfoObservers(mToken: String) {
        mViewModel.getStudentInfo(mToken)
        mViewModel.infoResponse.observeOnce(this) { kt ->
            if (kt != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    if (!kt.data.webView.isNullOrEmpty())
                        userPreferences.setWebViewData(kt.data.webView)
                    delay(TimeUnit.SECONDS.toMillis(1))
                    withContext(Dispatchers.Main) {
                        checkUserOnBoard(kt.data.user_on_boarded)
                    }
                }
            }
        }
        mViewModel.mShowResponseError.observeOnce(this) {
            AlertDialog.Builder(this).setMessage(it).show()
        }
        mViewModel.mShowProgressBar.observe(this) { bt ->
            if (bt) {
                showLoader()
            } else {
                hideLoader()
            }

        }
        mViewModel.mShowNetworkError.observeOnce(this) {
            if (it != null) {
                ConnectionDialogFragment.newInstance(Constant.RETRY_LOGIN)
                    .show(supportFragmentManager, ConnectionDialogFragment.TAG)
            }

        }
    }

//    private fun initInfoObservers() {
//        mViewModel.getStudentInfo(token)
//        mViewModel.infoResponse.observeOnce(this, { kt ->
//            if (kt != null) {
//                if (kt.data.deviceId != deviceId)
//                    BlockUserDialog.newInstance("App installed on other device")
//                        .show(supportFragmentManager, BlockUserDialog.TAG)
//            }
//        })
//        mViewModel.mShowResponseError.observeOnce(this, {
//        })
//        mViewModel.mShowProgressBar.observe(this, { bt ->
//            if (bt) {
//                showLoader()
//            } else {
//                hideLoader()
//            }
//
//        })
//        mViewModel.mShowNetworkError.observeOnce(this, {
//            if (it != null) {
//                ConnectionDialogFragment.newInstance(Constant.RETRY_LOGIN)
//                    .show(supportFragmentManager, ConnectionDialogFragment.TAG)
//            }
//
//        })
//    }


    override fun onResume() {
        super.onResume()
        //     mActivityBinding.surfaceView.onResume()
        mMediaRouter = MediaRouter.getInstance(this)
        val fm = supportFragmentManager
        val fragment: DiscoveryFragment?
        fragment = DiscoveryFragment()
        fragment.setCallback(mMediaRouterCB)
        fm.beginTransaction().add(fragment, DISCOVERY_FRAGMENT_TAG).commit()
    }

}