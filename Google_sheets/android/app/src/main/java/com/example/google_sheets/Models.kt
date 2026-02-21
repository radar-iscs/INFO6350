package com.example.google_sheets

data class RecordData(
    val first_name: String,
    val last_name: String,
    val time_in: String,
    val time_out: String,
    val notes: String
)

data class ApiResponse(
    val status: String,
    val message: String
)