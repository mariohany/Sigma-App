package com.noob.apps.mvvmcountries.ui.splash

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Display
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.google.firebase.messaging.FirebaseMessaging
import com.noob.apps.mvvmcountries.BuildConfig
import com.noob.apps.mvvmcountries.R
import com.noob.apps.mvvmcountries.data.DatabaseBuilder
import com.noob.apps.mvvmcountries.data.DatabaseHelperImpl
import com.noob.apps.mvvmcountries.models.RefreshTokenModel
import com.noob.apps.mvvmcountries.ui.base.BaseActivity
import com.noob.apps.mvvmcountries.ui.dialog.BlockUserDialog
import com.noob.apps.mvvmcountries.ui.dialog.ConnectionDialogFragment
import com.noob.apps.mvvmcountries.ui.home.HomeActivity
import com.noob.apps.mvvmcountries.ui.login.LoginActivity
import com.noob.apps.mvvmcountries.ui.welcome.UniversityActivity
import com.noob.apps.mvvmcountries.utils.Constant
import com.noob.apps.mvvmcountries.utils.ViewModelFactory
import com.noob.apps.mvvmcountries.viewmodels.SplashViewModel
import com.scottyab.rootbeer.RootBeer
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit


class SplashActivity : BaseActivity() {
    private var isSaved = false
    private var isloggedin = false
    private lateinit var splashViewModel: SplashViewModel
    private var refreshToken = ""
    private var token = ""
    var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash2)
        val displayManager = applicationContext.getSystemService(DISPLAY_SERVICE) as DisplayManager
        val var1: Array<Display?>? = displayManager.displays
        if (var1!!.size > 1) {
            return BlockUserDialog.newInstance("You Cannot run App on Screen Mirroring")
                .show(supportFragmentManager, BlockUserDialog.TAG)
        }
        splashViewModel = ViewModelProvider(
            this,
            ViewModelFactory(
                application,
                DatabaseHelperImpl(DatabaseBuilder.getInstance(applicationContext))
            )
        )[SplashViewModel::class.java]

//        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//        if (mBluetoothAdapter.isEnabled) {
//            return BlockUserDialog.newInstance("Please turn off Bluetooth\n")
//                .show(supportFragmentManager, BlockUserDialog.TAG)
//        }
        if (Settings.Secure.getInt(contentResolver, Settings.Secure.ADB_ENABLED, 0) == 1) {
            return BlockUserDialog.newInstance("Please turn off usb debugging\n")
                .show(supportFragmentManager, BlockUserDialog.TAG)
        }
        if (Settings.Secure.getInt(contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED , 0) == 1) {
            return BlockUserDialog.newInstance("Please turn off developer settings\n")
                .show(supportFragmentManager, BlockUserDialog.TAG)
        }
        if (isAppInstalled("me.weishu.exp")) {
            return BlockUserDialog.newInstance("Please Remove TaiChi app first\n")
                .show(supportFragmentManager, BlockUserDialog.TAG)
        }
        if (checkEmulatorFiles())
            return BlockUserDialog.newInstance("App can't run on Emulators")
                .show(supportFragmentManager, BlockUserDialog.TAG)
        if (isEmulator())
            return BlockUserDialog.newInstance("App can't run on Emulators")
                .show(supportFragmentManager, BlockUserDialog.TAG)
        if (checkRootMethod3()) {
            return BlockUserDialog.newInstance("App can't run on RootedDevice")
                .show(supportFragmentManager, BlockUserDialog.TAG)
        }
        val rootBeer = RootBeer(this)
        if (rootBeer.isRooted) {
            return BlockUserDialog.newInstance("App can't run on RootedDevice")
                .show(supportFragmentManager, BlockUserDialog.TAG)
        } else {
            initVersionUpdateObservers()
        }
    }


    private fun displayRetryDialog(
        context: Context,
        msg: String,
        btnName: String,
        listener: DialogInterface.OnClickListener
    ) {
        if (dialog != null) {
            dialog?.show()
        } else {
            dialog = AlertDialog.Builder(context)
                .setTitle(R.string.app_name)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(btnName, listener)
                .show()
        }
    }

    private fun readToken() {
        userPreferences.getRefreshToken.asLiveData().observeOnce(this) {
            //     refreshToken = it
            if (refreshToken.isNotEmpty())
                initTokenObservers()
            else
                navigate()
        }
    }

    private fun readISSaved() {
        userPreferences.getUniversityData.asLiveData().observeOnce(this) {
            isSaved = it
            readToken()
        }
    }

    private fun initVersionUpdateObservers() {
        splashViewModel.checkForUpdate()
        splashViewModel.mobileVersion.observe(this) {
            it?.let { version ->
                if (version.value > BuildConfig.VERSION_NAME) {
                    displayRetryDialog(
                        this,
                        "",
                        getString(R.string.update)
                    ) { dialog, _ ->
                        try {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=$packageName")
                                )
                            )
                        } catch (e: ActivityNotFoundException) {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                                )
                            )
                        }
                        dialog?.dismiss()
                    }
                } else {
                    userPreferences.savedLogginedFlow.asLiveData().observeOnce(this) {
                        isloggedin = it
                        if (isloggedin)
                            readISSaved()
                        else {
                            CoroutineScope(Dispatchers.IO).launch {
                                delay(TimeUnit.SECONDS.toMillis(2))
                                withContext(Dispatchers.Main) {
                                    openLogin()
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    private fun initTokenObservers() {
        splashViewModel.updateToken(RefreshTokenModel(Constant.REFRESH_TOKEN, refreshToken))
        splashViewModel.updateTokenResponse.observeOnce(this) { kt ->
            if (kt != null) {
                navigate()
            }
        }
        splashViewModel.mShowResponseError.observeOnce(this) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
        splashViewModel.mShowProgressBar.observe(this) { bt ->
            if (bt) {
                showLoader()
            } else {
                hideLoader()
            }

        }
        splashViewModel.mShowNetworkError.observeOnce(this) {
            if (it != null) {
                ConnectionDialogFragment.newInstance(Constant.RETRY_LOGIN)
                    .show(supportFragmentManager, ConnectionDialogFragment.TAG)
            }
        }
    }

    private fun navigate() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(TimeUnit.SECONDS.toMillis(2))
            withContext(Dispatchers.Main) {
                if (isloggedin) {
                    if (isSaved) {
                        openHome()
                    } else {
                        openUniversity()
                    }
                } else {
                    openLogin()

                }


            }
        }
    }

    private fun openHome() {
        val intent = Intent(this@SplashActivity, HomeActivity::class.java)
        startActivity(intent)
        finish()

    }

    private fun openUniversity() {
        val intent =
            Intent(this@SplashActivity, UniversityActivity::class.java)
        startActivity(intent)
        finish()

    }

    private fun openLogin() {
        val intent = Intent(this@SplashActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()

    }

    private fun checkRootMethod3(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val `in` = BufferedReader(InputStreamReader(process.inputStream))
            `in`.readLine() != null
        } catch (t: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }
}
