package com.noob.apps.mvvmcountries.models

import java.io.Serializable

data class LectureDetails(
    val id: String,
    val name: String,
    val description: String,
    val url: String,
    val sessionTimeout: Int,
    val allowedSessions: Int,
    val actualSessions: Int,
    val resolutions: String,
    val publicWatch: Boolean,
    val duration:Int,
    val studentSessions: List<StudentSessions>,
    val center_attendence: String,
    val views_online: String
) : Serializable
