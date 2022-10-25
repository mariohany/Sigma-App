package com.noob.apps.mvvmcountries.models

import java.io.Serializable

data class UpdateVersionModel(
    val status: String,
    val message: String,
    val errors: String,
    val versions: List<Version>
) : Serializable

data class Version(
    val key: String,
    val value: String
) : Serializable
