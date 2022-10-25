package com.noob.apps.mvvmcountries.repositories

import androidx.lifecycle.MutableLiveData
import com.noob.apps.mvvmcountries.interfaces.NetworkResponseCallback
import com.noob.apps.mvvmcountries.models.BoardingRequest
import com.noob.apps.mvvmcountries.models.BoardingResponse
import com.noob.apps.mvvmcountries.network.RestClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UniversityRepository private constructor() {
    private lateinit var mCallback: NetworkResponseCallback
    private var mmUniversityList: MutableLiveData<BoardingResponse> =
        MutableLiveData<BoardingResponse>()
    private var mLevelList: MutableLiveData<BoardingResponse> =
        MutableLiveData<BoardingResponse>()
    private var mDepartmentList: MutableLiveData<BoardingResponse> =
        MutableLiveData<BoardingResponse>()
    private var mmBoardingList: MutableLiveData<BoardingResponse> =
        MutableLiveData<BoardingResponse>()
    private lateinit var mUniversityCall: Call<BoardingResponse>
    private lateinit var mLevelCall: Call<BoardingResponse>
    private lateinit var mDepartmentCall: Call<BoardingResponse>
    private lateinit var mBoardingCall: Call<BoardingResponse>

    companion object {
        private var mInstance: UniversityRepository? = null
        fun getInstance(): UniversityRepository {
            if (mInstance == null) {
                synchronized(this) {
                    mInstance = UniversityRepository()
                }
            }
            return mInstance!!
        }
    }


    fun getUNIVERSITY(
        token: String,
        callback: NetworkResponseCallback
    ): MutableLiveData<BoardingResponse> {
        mCallback = callback
        if (mmUniversityList.value != null) {
            mCallback.onNetworkSuccess()
            mmUniversityList = MutableLiveData()
        }
        val authToken = token.replace("Bearer ", "")
        mUniversityCall = RestClient.getInstance().getApiService().getUNIVERSITY(token, authToken)
        mUniversityCall.enqueue(object : Callback<BoardingResponse> {

            override fun onResponse(
                call: Call<BoardingResponse>,
                response: Response<BoardingResponse>
            ) {
                if (response.code() != 200) {
                    val error:String = response.errorBody()?.string().let {
                        try {
                            if (JSONObject(it).has("error"))
                                JSONObject(it).getJSONArray("error").join("\n").replace("\"", "")
                            else
                                "un Expected Error"
                        }catch (e:Exception){
                            "un Expected Error"
                        }
                    } ?: "un Expected Error"
                    mCallback.onResponseError(error)
                } else {
                    mmUniversityList.value = response.body()
                    mCallback.onNetworkSuccess()
                }
            }

            override fun onFailure(call: Call<BoardingResponse>, t: Throwable) {
                mCallback.onNetworkFailure(t)
            }

        })
        return mmUniversityList
    }

    fun getLevels(
        token: String, collageId: String,
        callback: NetworkResponseCallback
    ): MutableLiveData<BoardingResponse> {
        mCallback = callback
        if (mLevelList.value != null) {
            mCallback.onNetworkSuccess()
            mLevelList = MutableLiveData()
        }
        val authToken = token.replace("Bearer ", "")
        mLevelCall = RestClient.getInstance().getApiService().getLevels(token,authToken, collageId)
        mLevelCall.enqueue(object : Callback<BoardingResponse> {

            override fun onResponse(
                call: Call<BoardingResponse>,
                response: Response<BoardingResponse>
            ) {
                if (response.code() != 200) {
                    val error:String = response.errorBody()?.string().let {
                        try {
                            if (JSONObject(it).has("error"))
                                JSONObject(it).getJSONArray("error").join("\n").replace("\"", "")
                            else
                                "un Expected Error"
                        }catch (e:Exception){
                            "un Expected Error"
                        }
                    } ?: "un Expected Error"
                    mCallback.onResponseError(error)
                } else {
                    mLevelList.value = response.body()
                    mCallback.onNetworkSuccess()
                }
            }

            override fun onFailure(call: Call<BoardingResponse>, t: Throwable) {
                mCallback.onNetworkFailure(t)
            }

        })
        return mLevelList
    }

    fun getDepartments(
        token: String, collageId: String,
        callback: NetworkResponseCallback
    ): MutableLiveData<BoardingResponse> {
        mCallback = callback
        if (mDepartmentList.value != null) {
            mCallback.onNetworkSuccess()
            mDepartmentList = MutableLiveData()
        }
        val authToken = token.replace("Bearer ", "")
        mDepartmentCall = RestClient.getInstance().getApiService().getDepartments(token,authToken, collageId)
        mDepartmentCall.enqueue(object : Callback<BoardingResponse> {

            override fun onResponse(
                call: Call<BoardingResponse>,
                response: Response<BoardingResponse>
            ) {
                if (response.code() != 200) {
                    val error:String = response.errorBody()?.string().let {
                        try {
                            if (JSONObject(it).has("error"))
                                JSONObject(it).getJSONArray("error").join("\n").replace("\"", "")
                            else
                                "un Expected Error"
                        }catch (e:Exception){
                            "un Expected Error"
                        }
                    } ?: "un Expected Error"
                    mCallback.onResponseError(error)
                } else {
                    mDepartmentList.value = response.body()
                    mCallback.onNetworkSuccess()
                }
            }

            override fun onFailure(call: Call<BoardingResponse>, t: Throwable) {
                mCallback.onNetworkFailure(t)
            }

        })
        return mDepartmentList
    }

    fun postBoardingData(
        token: String, model: BoardingRequest,
        callback: NetworkResponseCallback
    ): MutableLiveData<BoardingResponse> {
        mCallback = callback
        if (mmBoardingList.value != null) {
            mCallback.onNetworkSuccess()
            mmBoardingList = MutableLiveData()
        }
        val authToken = token.replace("Bearer ", "")
        mBoardingCall = RestClient.getInstance().getApiService().postBoardingData(token,authToken, model)
        mBoardingCall.enqueue(object : Callback<BoardingResponse> {

            override fun onResponse(
                call: Call<BoardingResponse>,
                response: Response<BoardingResponse>
            ) {
                if (response.code() == 401) {
                    val jsonObject: JSONObject?
                    jsonObject = JSONObject(response.errorBody()!!.string())
                    val userMessage = jsonObject.getString("error")
                    val internalMessage = jsonObject.getString("error_description")
                    mCallback.onResponseError(internalMessage)
                } else if (response.code() == 200) {
                    mmBoardingList.value = response.body()
                    mCallback.onNetworkSuccess()
                } else {
                    mCallback.onResponseError("")
                }
            }

            override fun onFailure(call: Call<BoardingResponse>, t: Throwable) {
                mCallback.onNetworkFailure(t)
            }

        })
        return mmBoardingList
    }
}