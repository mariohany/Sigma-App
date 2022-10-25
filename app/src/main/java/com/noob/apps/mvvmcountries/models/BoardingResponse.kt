package com.noob.apps.mvvmcountries.models

import com.google.gson.annotations.SerializedName

data class BoardingResponse(

    val status: String,
    val message: String,
    val errors: String,
    @SerializedName("study_fields")
    val fields: List<Collage>,
    val departments: List<Collage>,
    val levels: List<Collage>
)

