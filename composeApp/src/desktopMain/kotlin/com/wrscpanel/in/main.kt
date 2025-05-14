package com.wrscpanel.`in`

import DATA_STORE_FILE_NAME
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.plcoding.bookpedia.di.initKoin
import com.wrscpanel.`in`.config.Config
import controlPanalUser.domain.UserRole
import controlPanalUser.repository.SessionManager
import createDataStore
import dashboard.DashboardApp
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import login.presentation.LoginScreen
import util.DatabaseCollection
import util.DatabaseUtil
import io.ktor.client.*

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import kotlinx.coroutines.*

fun main() {
    val prefs = createDataStore {
        DATA_STORE_FILE_NAME
    }

    println(Config.BASE_URL)
    val version = object {}.javaClass.getResourceAsStream("/build.properties")?.use { stream ->
        val props = java.util.Properties().apply { load(stream) }
        props.getProperty("version") ?: "unknown"
    } ?: "unknown"

    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "We Are Spine Control Panel ($version)",
        ) {
//            App(
//                prefs = prefs
//            )
//            LaunchedEffect(Unit) {
//                getTotalAppointmentCount()
//            }
            var isLoggedIn by remember { mutableStateOf(SessionManager.currentUser != null) }
//
            if (isLoggedIn && SessionManager.currentUser != null) {
                // Show the Dashboard after successful login
                SessionManager.currentUser?.let {
                    DashboardApp(
                        userSession = it,
                        SessionManager.currentUser?.role ?: UserRole.EMPLOYEE,
                        onLogout = {
                            SessionManager.currentUser = null
                            isLoggedIn = false
                        }
                    )
                }
            } else {
                LoginScreen { status ->
                    // Perform login validation here if needed
                    isLoggedIn = status
                }
            }
        }
    }
}


//suspend fun getTotalAppointmentCount() {
//    try {
        val BASe_URL = DatabaseUtil.DATABASE_URL
        val appointment = DatabaseCollection.APPOINTMENTS
        val client = HttpClient()
//
//        val requestBody = buildJsonObject {
//            putJsonObject("structuredAggregationQuery") {
//                putJsonArray("aggregations") {
//                    add(buildJsonObject { putJsonObject("count") {}; put("alias", "count") })
//                }
//                putJsonObject("structuredQuery") {
//                    putJsonArray("from") {
//                        add(buildJsonObject { put("collectionId", appointment) })
//                    }
//                }
//            }
//        }
//
//        val response = client.post("$BASe_URL:runAggregationQuery") {
//            contentType(ContentType.Application.Json)
//            setBody(requestBody.toString())
//        }
//
//        val resultArray = Json.parseToJsonElement(response.bodyAsText()).jsonArray
//        val countString = resultArray
//            .firstOrNull()
//            ?.jsonObject?.get("result")
//            ?.jsonObject?.get("aggregateFields")
//            ?.jsonObject?.get("count")
//            ?.jsonObject?.get("integerValue")
//            ?.jsonPrimitive?.content
//
//        val count = countString?.toIntOrNull() ?: 0
//        println("🔥 Appointment count: $count")
//
//        client.close()
//    } catch (e: Exception) {
//        e.printStackTrace()
//        println(e.localizedMessage)
//    }
//}