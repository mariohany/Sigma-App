package com.noob.apps.mvvmcountries.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.noob.apps.mvvmcountries.interfaces.NetworkResponseCallback
import com.noob.apps.mvvmcountries.models.BoardingRequest
import com.noob.apps.mvvmcountries.models.BoardingResponse
import com.noob.apps.mvvmcountries.repositories.UniversityRepository
import com.noob.apps.mvvmcountries.utils.NetworkHelper

class UniversityViewModel(private val app: Application) : AndroidViewModel(app) {
    private var mList: MutableLiveData<BoardingResponse> =
        MutableLiveData<BoardingResponse>()
    private var boardingResponse: MutableLiveData<BoardingResponse> =
        MutableLiveData<BoardingResponse>()
    private var mLevels: MutableLiveData<BoardingResponse> =
        MutableLiveData<BoardingResponse>()
    private var mDepartments: MutableLiveData<BoardingResponse> =
        MutableLiveData<BoardingResponse>()
    val mShowProgressBar = MutableLiveData(true)
    val mShowNetworkError: MutableLiveData<Boolean> = MutableLiveData()
    val mShowApiError = MutableLiveData<String>()
    val mShowResponseError = MutableLiveData<String>()
    private var mRepository = UniversityRepository.getInstance()

    fun getUniversity(token: String): MutableLiveData<BoardingResponse> {
        if (NetworkHelper.isOnline(app.baseContext)) {
            mShowProgressBar.value = true
            mList = mRepository.getUNIVERSITY(token, object : NetworkResponseCallback {
                override fun onNetworkFailure(th: Throwable) {
                    mShowApiError.value = th.message
                }

                override fun onNetworkSuccess() {
                    mShowProgressBar.value = false
                }

                override fun onResponseError(message: String) {
                    mShowProgressBar.value = false
                    mShowResponseError.value = message
                }
            })
        } else {
            mShowProgressBar.value = false
            mShowNetworkError.value = true
        }
        return mList
    }

    fun getLevels(token: String, collageId: String): MutableLiveData<BoardingResponse> {
        if (NetworkHelper.isOnline(app.baseContext)) {
            mShowProgressBar.value = true
            mLevels = mRepository.getLevels(token, collageId, object : NetworkResponseCallback {
                override fun onNetworkFailure(th: Throwable) {
                    mShowApiError.value = th.message
                }

                override fun onNetworkSuccess() {
                    mShowProgressBar.value = false
                }

                override fun onResponseError(message: String) {
                    mShowProgressBar.value = false
                    mShowResponseError.value = message
                }
            })
        } else {
            mShowProgressBar.value = false
            mShowNetworkError.value = true
        }
        return mLevels
    }

    fun getDepartments(token: String, collageId: String): MutableLiveData<BoardingResponse> {
        if (NetworkHelper.isOnline(app.baseContext)) {
            mShowProgressBar.value = true
            mDepartments =
                mRepository.getDepartments(token, collageId, object : NetworkResponseCallback {
                    override fun onNetworkFailure(th: Throwable) {
                        mShowApiError.value = th.message
                    }

                    override fun onNetworkSuccess() {
                        mShowProgressBar.value = false
                    }

                    override fun onResponseError(message: String) {
                        mShowProgressBar.value = false
                        mShowResponseError.value = message
                    }
                })
        } else {
            mShowProgressBar.value = false
            mShowNetworkError.value = true
        }
        return mDepartments
    }

    fun postUserUniversity(
        token: String,
        model: BoardingRequest
    ): MutableLiveData<BoardingResponse> {
        if (NetworkHelper.isOnline(app.baseContext)) {
            mShowProgressBar.value = true
            boardingResponse = mRepository.postBoardingData(token,model, object : NetworkResponseCallback {
                override fun onNetworkFailure(th: Throwable) {
                    mShowApiError.value = th.message
                }

                override fun onNetworkSuccess() {
                    mShowProgressBar.value = false
                }

                override fun onResponseError(message: String) {
                    mShowProgressBar.value = false
                    mShowResponseError.value = message
                }
            })
        } else {
            mShowProgressBar.value = false
            mShowNetworkError.value = true
        }
        return boardingResponse
    }
}