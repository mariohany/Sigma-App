package com.noob.apps.mvvmcountries.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Lectures(
    val id: String,
    val name: String,
    val url: String,
    val sessionTimeout: Int,
    val allowedSessions: Int,
    val allowedWatches: Int,
    val actualSessions: Int,
    @SerializedName("center_attendence")
    val centerAttendence: Int,
    @SerializedName("views_online")
    val viewsOnline: Int,
    val resolutions: String,
    val publicWatch: Boolean,
    val studentSessions: List<StudentSessions>
) : Serializable
