package com.noob.apps.mvvmcountries.ui.base

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Display
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.mediarouter.app.MediaRouteDiscoveryFragment
import androidx.mediarouter.media.MediaRouter
import com.framgia.android.emulator.EmulatorDetector
import com.kaopiz.kprogresshud.KProgressHUD
import com.noob.apps.mvvmcountries.data.UserPreferences
import com.noob.apps.mvvmcountries.ui.dialog.BlockUserDialog
import com.noob.apps.mvvmcountries.ui.dialog.MirroringDialog
import com.noob.apps.mvvmcountries.utils.SystemProperties
import java.io.File
import java.util.*


open class BaseActivity : AppCompatActivity() {
    lateinit var deviceId: String
    private var dialog: KProgressHUD? = null
    lateinit var userPreferences: UserPreferences
    var appLanguage = ""
    private var sIsProbablyRunningOnEmulator: Boolean? = null
    private lateinit var blockUserDialog: MirroringDialog

    private val PIPES = arrayOf(
        "/dev/socket/qemud",
        "/dev/qemu_pipe"
    )
    private val X86_FILES = arrayOf(
        "ueventd.android_x86.rc",
        "x86.prop",
        "ueventd.ttVM_x86.rc",
        "init.ttVM_x86.rc",
        "fstab.ttVM_x86",
        "fstab.vbox86",
        "init.vbox86.rc",
        "ueventd.vbox86.rc"
    )
    private val ANDY_FILES = arrayOf(
        "fstab.andy",
        "ueventd.andy.rc"
    )
    private val NOX_FILES = arrayOf(
        "fstab.nox",
        "init.nox.rc",
        "ueventd.nox.rc"
    )


    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        hideSystemUI()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        userPreferences = UserPreferences(this)

        userPreferences.getAppLanguage.asLiveData().observeOnce(this) {
            val config = resources.configuration
            appLanguage = it
            val lang: String = if (appLanguage == "ARABIC")
                "ar"
            else
                "en"
            val locale = Locale(lang)
            Locale.setDefault(locale)
            config.setLocale(locale)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                createConfigurationContext(config)
            resources.updateConfiguration(config, resources.displayMetrics)
        }


    }


    fun hideKeyboard() {
        val imm: InputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view: View? = currentFocus
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showLoader() {
        if (dialog == null) {
            dialog = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show()
        } else {
            dialog?.show()
        }
    }

    fun hideLoader() {
        dialog?.dismiss()
    }

    fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }

    fun isEmulator(): Boolean {
        var result = sIsProbablyRunningOnEmulator
        if (result != null)
            return result
        // Android SDK emulator
        result = (Build.FINGERPRINT.startsWith("google/sdk_gphone_")
                && Build.FINGERPRINT.endsWith(":user/release-keys")
                && Build.MANUFACTURER == "Google" && Build.PRODUCT.startsWith("sdk_gphone_") && Build.BRAND == "google"
                && Build.MODEL.startsWith("sdk_gphone_"))
                //
                || Build.HARDWARE == "vbox86"
                || Build.HARDWARE == "Kirin 710"
                || Build.HARDWARE == "Kirin 710".trim()
                || Build.PRODUCT == "vbox86p"
                || Build.HARDWARE.lowercase(Locale.getDefault()).contains("nox")
                || Build.MODEL.lowercase(Locale.getDefault()).contains("droid4x")
                || Build.HARDWARE == "goldfish"
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.PRODUCT.lowercase(Locale.getDefault()).contains("nox")
                || Build.BOARD.lowercase(Locale.getDefault()).contains("nox")
                || Build.MODEL.contains("Android SDK built for x86")
                //bluestacks
                || "QC_Reference_Phone" == Build.BOARD && !"Xiaomi".equals(
            Build.MANUFACTURER,
            ignoreCase = true
        ) //bluestacks
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.HOST == "Build2" //MSI App Player
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || Build.PRODUCT == "google_sdk"
                // another Android SDK emulator check
                || SystemProperties.getProp("ro.kernel.qemu") == "1"

        sIsProbablyRunningOnEmulator = result
        return result
    }

    private fun checkFiles(targets: Array<String>): Boolean {
        for (pipe in targets) {
            val file = File(pipe)
            if (file.exists()) {
                return true
            }
        }
        return false
    }

    fun checkEmulatorFiles(): Boolean {
        return checkFiles(ANDY_FILES)
                || checkFiles(NOX_FILES)
                || checkFiles(X86_FILES)
                || checkFiles(PIPES)
    }

    protected open fun hideSystemUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    private val bluetoothChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action

            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                )
                when (state) {
                    BluetoothAdapter.STATE_ON ->
                        return BlockUserDialog.newInstance("Please turn off Bluetooth\n")
                            .show(supportFragmentManager, BlockUserDialog.TAG)
                    BluetoothAdapter.STATE_TURNING_ON ->
                        return BlockUserDialog.newInstance("Please turn off Bluetooth\n")
                            .show(supportFragmentManager, BlockUserDialog.TAG)
                }
            }
        }
    }
    private val eventReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            // pause video
            val action = intent.action
            when (action) {
                AudioManager.ACTION_HDMI_AUDIO_PLUG ->                     // EXTRA_AUDIO_PLUG_STATE: 0 - UNPLUG, 1 - PLUG
                    return BlockUserDialog.newInstance("Please plug off HDMI cable")
                        .show(supportFragmentManager, BlockUserDialog.TAG)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        EmulatorDetector.with(this)
            .setCheckTelephony(true)
            .addPackageName("com.bluestacks")
            .setDebug(true)
            .detect {
                if (it)
                    return@detect BlockUserDialog.newInstance("App can't run on Emulators")
                        .show(supportFragmentManager, BlockUserDialog.TAG)
            }
        val filter2 = IntentFilter()
        filter2.addAction(AudioManager.ACTION_HDMI_AUDIO_PLUG)
        registerReceiver(eventReceiver, filter2)
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
        val displayManager = applicationContext.getSystemService(DISPLAY_SERVICE) as DisplayManager
        val var1: Array<Display?>? = displayManager.displays
        if (var1!!.size > 1) {
            return BlockUserDialog.newInstance("You Cannot run App on Screen Mirroring")
                .show(supportFragmentManager, BlockUserDialog.TAG)
        }
    }

    protected fun isAppInstalled(uri: String): Boolean {
        val pm = packageManager
        return try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: Exception) {
            false
        }
    }

    class DiscoveryFragment : MediaRouteDiscoveryFragment() {
        private var mCallback: MediaRouter.Callback? = null
        fun setCallback(cb: MediaRouter.Callback) {
            mCallback = cb
        }

        override fun onCreateCallback(): MediaRouter.Callback? {
            return mCallback
        }

        override fun onPrepareCallbackFlags(): Int {
            // Add the CALLBACK_FLAG_UNFILTERED_EVENTS flag to ensure that we will
            // observe and log all route events including those that are for routes
            // that do not match our selector.  This is only for demonstration purposes
            // and should not be needed by most applications.
            return super.onPrepareCallbackFlags() or MediaRouter.CALLBACK_FLAG_UNFILTERED_EVENTS
        }
    }

    fun showBlockDialog(msg: String) {
        blockUserDialog = MirroringDialog.newInstance(msg)
        blockUserDialog.show(
            supportFragmentManager,
            BlockUserDialog.TAG
        )
    }


    fun hideBlockDialog() {
        if (::blockUserDialog.isInitialized) {
            finish()
            startActivity(intent)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(eventReceiver)
        dialog = null
    }
}