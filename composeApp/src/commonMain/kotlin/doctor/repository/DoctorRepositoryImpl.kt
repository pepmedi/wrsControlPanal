package doctor.repository

import core.data.safeCall
import core.domain.DataError
import core.domain.AppResult
import doctor.domain.DoctorRepository
import doctor.domain.DoctorMaster
import imageUpload.uploadImageToFirebaseStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import util.DatabaseCollection
import util.DatabaseDocument
import util.DatabaseDocumentsResponse
import util.DatabaseRequest
import util.DatabaseResponse
import util.DatabaseUtil
import util.DatabaseValue
import util.StorageCollection
import util.buildUpdateMask
import java.io.File

private const val BASE_URL = DatabaseUtil.DATABASE_URL

class DoctorRepositoryImpl(private val httpClient: HttpClient) : DoctorRepository {
    override suspend fun getAllDoctors(): Flow<AppResult<List<DoctorMaster>, DataError.Remote>> =
        flow {
            val url = "$BASE_URL/${DatabaseCollection.DOCTORS}"

            val result: AppResult<DatabaseDocumentsResponse, DataError.Remote> = safeCall {
                httpClient.get(url) {
                    contentType(ContentType.Application.Json)
                }
            }

            when (result) {
                is AppResult.Success -> {
                    val firestoreResponse = result.data
                    val doctors = firestoreResponse.documents.map { document ->
                        val fields = document.fields
                        DoctorMaster(
                            id = document.name.substringAfterLast("/"),
                            name = (fields["name"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            age = (fields["age"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            profilePic = (fields["profilePic"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            consltFee = (fields["consltFee"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            reviews = (fields["reviews"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            experience = (fields["experience"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            hospital = (fields["hospital"] as? DatabaseValue.ArrayValue)?.values?.mapNotNull { (it as? DatabaseValue.StringValue)?.stringValue }
                                .orEmpty(),
                            services = (fields["services"] as? DatabaseValue.ArrayValue)?.values?.mapNotNull { (it as? DatabaseValue.StringValue)?.stringValue }
                                .orEmpty(),
                            createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            speciality = (fields["speciality"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            focus = (fields["focus"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            careerPath = (fields["careerPath"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            profile = (fields["profile"] as? DatabaseValue.StringValue)?.stringValue.orEmpty()
                        )
                    }
                    emit(AppResult.Success(doctors))
                }

                is AppResult.Error -> {
                    emit(AppResult.Error(result.error))
                }
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun addDoctorToDatabase(
        doctor: DoctorMaster,
        imageFile: File
    ): Flow<AppResult<DoctorMaster, DataError.Remote>> =
        flow {
            val url = "$BASE_URL/${DatabaseCollection.DOCTORS}"
            try {
                val response: HttpResponse = httpClient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(
                        DatabaseRequest(
                            fields = mapOf(
                                "name" to DatabaseValue.StringValue(doctor.name),
                                "age" to DatabaseValue.StringValue(doctor.age),
                                "experience" to DatabaseValue.StringValue(doctor.experience),
                                "profilePic" to DatabaseValue.StringValue(doctor.profilePic),
                                "hospital" to DatabaseValue.ArrayValue(doctor.hospital.map {
                                    DatabaseValue.StringValue(
                                        it
                                    )
                                }),
                                "services" to DatabaseValue.ArrayValue(doctor.services.map {
                                    DatabaseValue.StringValue(
                                        it
                                    )
                                }),
                                "slots" to DatabaseValue.ArrayValue(doctor.slots.map {
                                    DatabaseValue.StringValue(
                                        it
                                    )
                                }),
                                "consltFee" to DatabaseValue.StringValue(doctor.consltFee),
                                "reviews" to DatabaseValue.StringValue(doctor.reviews),
                                "createdAt" to DatabaseValue.StringValue(doctor.createdAt),
                                "updatedAt" to DatabaseValue.StringValue(doctor.updatedAt),
                                "speciality" to DatabaseValue.StringValue(doctor.speciality),
                                "focus" to DatabaseValue.StringValue(doctor.focus),
                                "careerPath" to DatabaseValue.StringValue(doctor.careerPath),
                                "profile" to DatabaseValue.StringValue(doctor.profile)
                            )
                        )
                    )
                }

                if (response.status != HttpStatusCode.OK) {
                    println("Error: ${response.status}")
                    emit(AppResult.Error(DataError.Remote.SERVER))
                    return@flow
                }

                val firestoreResponse: DatabaseResponse = response.body()
                val generatedId = firestoreResponse.name.substringAfterLast("/")

                // Step 2: Update only the "id" field
                val patchResponse = httpClient.patch("$url/$generatedId?updateMask.fieldPaths=id") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        DatabaseRequest(
                            fields = mapOf(
                                "id" to DatabaseValue.StringValue(
                                    generatedId
                                )
                            )
                        )
                    )
                }

                if (patchResponse.status == HttpStatusCode.OK) {

                    val downloadUrl = uploadImageToFirebaseStorage(
                        httpClient = httpClient,
                        file = imageFile,
                        folderName = StorageCollection.DOCTOR_IMAGES,
                        fileName = generatedId
                    )

                    val patchImageResponse =
                        httpClient.patch("$url/$generatedId?updateMask.fieldPaths=profilePic") {
                            contentType(ContentType.Application.Json)
                            setBody(
                                DatabaseRequest(
                                    fields = mapOf(
                                        "profilePic" to DatabaseValue.StringValue(
                                            downloadUrl
                                        )
                                    )
                                )
                            )
                        }

                    if (patchImageResponse.status == HttpStatusCode.OK) {
                        val updatedDoctor = doctor.copy(
                            id = generatedId,
                            profilePic = downloadUrl
                        )
                        emit(AppResult.Success(updatedDoctor))
                    } else {
                        emit(AppResult.Error(DataError.Remote.SERVER))
                    }
                } else {
                    emit(AppResult.Error(DataError.Remote.SERVER))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println(e.localizedMessage)
                emit(AppResult.Error(DataError.Remote.UNKNOWN))
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun getDoctor(doctorId: String): Flow<AppResult<DoctorMaster, DataError.Remote>> =
        flow {
            val url = "${DatabaseUtil.DATABASE_URL}/${DatabaseCollection.DOCTORS}/$doctorId"

            try {
                val result: AppResult<DatabaseDocument, DataError.Remote> = safeCall {
                    httpClient.get(url) {
                        contentType(ContentType.Application.Json)
                    }
                }

                when (result) {
                    is AppResult.Success -> {
                        val document =
                            result.data

                        val fields = document.fields
                        val doctor = DoctorMaster(
                            id = document.name.substringAfterLast("/"),
                            name = (fields["name"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            age = (fields["age"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            profilePic = (fields["profilePic"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            consltFee = (fields["consltFee"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            reviews = (fields["reviews"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            experience = (fields["experience"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            speciality = (fields["speciality"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            hospital = (fields["hospital"] as? DatabaseValue.ArrayValue)?.values?.mapNotNull { (it as? DatabaseValue.StringValue)?.stringValue }
                                .orEmpty(),
                            services = (fields["services"] as? DatabaseValue.ArrayValue)?.values?.mapNotNull { (it as? DatabaseValue.StringValue)?.stringValue }
                                .orEmpty(),
                            slots = (fields["slots"] as? DatabaseValue.ArrayValue)?.values?.mapNotNull { (it as? DatabaseValue.StringValue)?.stringValue }
                                .orEmpty(),
                            createdAt = (fields["createdAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            updatedAt = (fields["updatedAt"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            focus = (fields["focus"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            careerPath = (fields["careerPath"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            profile = (fields["profile"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                        )
                        emit(AppResult.Success(doctor))
                    }

                    is AppResult.Error -> {
                        emit(AppResult.Error(result.error))
                    }
                }

            } catch (e: Exception) {
                emit(AppResult.Error(DataError.Remote.SERVER))
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun updateDoctor(
        doctor: DoctorMaster,
        imageFile: File?
    ): Flow<AppResult<String?, DataError.Remote>> = flow {
        try {
            // Step 1: Update doctor details first
            val patchResponse = httpClient.patch(
                "$BASE_URL/${DatabaseCollection.DOCTORS}/${doctor.id}?${
                    buildUpdateMask(
                        "name", "experience", "age", "hospital", "services", "slots",
                        "consltFee", "updatedAt", "focus", "careerPath", "profile"
                    )
                }"
            ) {
                contentType(ContentType.Application.Json)
                setBody(
                    DatabaseRequest(
                        fields = mapOf(
                            "name" to DatabaseValue.StringValue(doctor.name),
                            "age" to DatabaseValue.StringValue(doctor.age),
                            "experience" to DatabaseValue.StringValue(doctor.experience),
                            "hospital" to DatabaseValue.ArrayValue(doctor.hospital.map {
                                DatabaseValue.StringValue(it)
                            }),
                            "services" to DatabaseValue.ArrayValue(doctor.services.map {
                                DatabaseValue.StringValue(it)
                            }),
                            "slots" to DatabaseValue.ArrayValue(doctor.slots.map {
                                DatabaseValue.StringValue(it)
                            }),
                            "consltFee" to DatabaseValue.StringValue(doctor.consltFee),
                            "updatedAt" to DatabaseValue.StringValue(doctor.updatedAt),
                            "focus" to DatabaseValue.StringValue(doctor.focus),
                            "careerPath" to DatabaseValue.StringValue(doctor.careerPath),
                            "profile" to DatabaseValue.StringValue(doctor.profile)
                        )
                    )
                )
            }

            if (patchResponse.status == HttpStatusCode.OK) {
                // Step 2: Upload image if available
                if (imageFile != null) {
                    val downloadUrl = uploadImageToFirebaseStorage(
                        httpClient = httpClient,
                        file = imageFile,
                        folderName = StorageCollection.DOCTOR_IMAGES,
                        fileName = doctor.id
                    )

                    val patchImageResponse = httpClient.patch(
                        "$BASE_URL/${DatabaseCollection.DOCTORS}/${doctor.id}?updateMask.fieldPaths=profilePic"
                    ) {
                        contentType(ContentType.Application.Json)
                        setBody(
                            DatabaseRequest(
                                fields = mapOf(
                                    "profilePic" to DatabaseValue.StringValue(downloadUrl)
                                )
                            )
                        )
                    }

                    if (patchImageResponse.status == HttpStatusCode.OK) {
                        emit(AppResult.Success(downloadUrl)) // ✅ emit URL if uploaded
                    } else {
                        emit(AppResult.Error(DataError.Remote.SERVER))
                    }
                } else {
                    emit(AppResult.Success(null)) // ✅ emit success but no image uploaded
                }
            } else {
                emit(AppResult.Error(DataError.Remote.SERVER))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(AppResult.Error(DataError.Remote.SERVER))
        }
    }.flowOn(Dispatchers.IO)
}