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
                            qualification = (fields["qualification"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            profilePic = (fields["profilePic"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            doctorInfoPic = (fields["doctorInfoPic"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            doctorHomePageUrl = (fields["doctorHomePageUrl"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            consltFee = (fields["consltFee"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            reviews = (fields["reviews"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),

                            experience = (fields["experience"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            city = (fields["city"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            hospital = (fields["hospital"] as? DatabaseValue.ArrayValue)?.values?.mapNotNull { (it as? DatabaseValue.StringValue)?.stringValue }
                                .orEmpty(),
                            services = (fields["services"] as? DatabaseValue.ArrayValue)?.values?.mapNotNull { (it as? DatabaseValue.StringValue)?.stringValue }
                                .orEmpty(),
                            blockedDates = (fields["blockedDates"] as? DatabaseValue.ArrayValue)?.values?.mapNotNull { (it as? DatabaseValue.StringValue)?.stringValue }
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
        profileImageFile: File,
        infoImageFile: File,
        doctorHomePageImage: File
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
                                "qualification" to DatabaseValue.StringValue(doctor.qualification),
                                "experience" to DatabaseValue.StringValue(doctor.experience),
                                "city" to DatabaseValue.StringValue(doctor.city),
                                "profilePic" to DatabaseValue.StringValue(doctor.profilePic),
                                "doctorHomePageUrl" to DatabaseValue.StringValue(doctor.doctorHomePageUrl),
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

                    val profileImageUrl = uploadImageToFirebaseStorage(
                        httpClient = httpClient,
                        file = profileImageFile,
                        folderName = StorageCollection.DOCTOR_IMAGES,
                        fileName = generatedId
                    )

                    val infoImageUrl = uploadImageToFirebaseStorage(
                        httpClient = httpClient,
                        file = profileImageFile,
                        folderName = StorageCollection.DOCTOR_INFO_IMAGES,
                        fileName = generatedId
                    )

                    val doctorHomePageImageUrl = uploadImageToFirebaseStorage(
                        httpClient = httpClient,
                        file = doctorHomePageImage,
                        folderName = StorageCollection.DOCTOR_HOME_PAGE_IMAGES,
                        fileName = generatedId
                    )

                    val patchImageResponse =
                        httpClient.patch(
                            "$url/$generatedId?${
                                buildUpdateMask(
                                    "profilePic",
                                    "doctorInfoPic",
                                    "doctorHomePageUrl"
                                )
                            }"
                        ) {
                            contentType(ContentType.Application.Json)
                            setBody(
                                DatabaseRequest(
                                    fields = mapOf(
                                        "profilePic" to DatabaseValue.StringValue(
                                            profileImageUrl
                                        ),
                                        "doctorInfoPic" to DatabaseValue.StringValue(
                                            infoImageUrl
                                        ),
                                        "doctorHomePageUrl" to DatabaseValue.StringValue(
                                            doctorHomePageImageUrl
                                        )
                                    )
                                )
                            )
                        }

                    if (patchImageResponse.status == HttpStatusCode.OK) {
                        val updatedDoctor = doctor.copy(
                            id = generatedId,
                            profilePic = profileImageUrl,
                            doctorInfoPic = infoImageUrl,
                            doctorHomePageUrl = doctorHomePageImageUrl
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
                            doctorInfoPic = (fields["doctorInfoPic"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            doctorHomePageUrl = (fields["doctorHomePageUrl"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            qualification = (fields["qualification"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            consltFee = (fields["consltFee"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            reviews = (fields["reviews"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            experience = (fields["experience"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
                            city = (fields["city"] as? DatabaseValue.StringValue)?.stringValue.orEmpty(),
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
        profileImageFile: File?,
        infoImageFile: File?,
        doctorHomePageImage: File?
    ): Flow<AppResult<Triple<String?, String?, String?>, DataError.Remote>> = flow {
        try {
            // Step 1: Update doctor details first
            val patchResponse = httpClient.patch(
                "$BASE_URL/${DatabaseCollection.DOCTORS}/${doctor.id}?${
                    buildUpdateMask(
                        "name",
                        "age",
                        "qualification",
                        "experience",
                        "hospital",
                        "services",
                        "slots",
                        "consltFee",
                        "updatedAt",
                        "focus",
                        "careerPath",
                        "profile",
                        "city"
                    )
                }"
            ) {
                contentType(ContentType.Application.Json)
                setBody(
                    DatabaseRequest(
                        fields = mapOf(
                            "name" to DatabaseValue.StringValue(doctor.name),
                            "age" to DatabaseValue.StringValue(doctor.age),
                            "qualification" to DatabaseValue.StringValue(doctor.qualification),
                            "experience" to DatabaseValue.StringValue(doctor.experience),
                            "city" to DatabaseValue.StringValue(doctor.city),
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
                if (profileImageFile != null || infoImageFile != null || doctorHomePageImage != null) {

                    val updatedFields = mutableMapOf<String, DatabaseValue>()
                    var updatedProfileImageUrl: String? = null
                    var updatedInfoImageUrl: String? = null
                    var updatedHomePageImageUrl: String? = null

                    if (profileImageFile != null) {
                        updatedProfileImageUrl = uploadImageToFirebaseStorage(
                            httpClient = httpClient,
                            file = profileImageFile,
                            folderName = StorageCollection.DOCTOR_IMAGES,
                            fileName = doctor.id
                        )
                        updatedFields["profilePic"] =
                            DatabaseValue.StringValue(updatedProfileImageUrl)
                    }

                    if (infoImageFile != null) {
                        updatedInfoImageUrl = uploadImageToFirebaseStorage(
                            httpClient = httpClient,
                            file = infoImageFile,
                            folderName = StorageCollection.DOCTOR_INFO_IMAGES,
                            fileName = doctor.id
                        )
                        updatedFields["doctorInfoPic"] =
                            DatabaseValue.StringValue(updatedInfoImageUrl)
                    }

                    if (doctorHomePageImage != null) {
                        updatedHomePageImageUrl = uploadImageToFirebaseStorage(
                            httpClient = httpClient,
                            file = doctorHomePageImage,
                            folderName = StorageCollection.DOCTOR_HOME_PAGE_IMAGES,
                            fileName = doctor.id
                        )
                        updatedFields["doctorHomePageUrl"] =
                            DatabaseValue.StringValue(updatedHomePageImageUrl)
                    }

                    if (updatedFields.isNotEmpty()) {
                        val fieldPaths =
                            updatedFields.keys.joinToString("&updateMask.fieldPaths=")

                        val patchImageResponse = httpClient.patch(
                            "$BASE_URL/${DatabaseCollection.DOCTORS}/${doctor.id}?updateMask.fieldPaths=$fieldPaths"
                        ) {
                            contentType(ContentType.Application.Json)
                            setBody(DatabaseRequest(fields = updatedFields))
                        }

                        if (patchImageResponse.status == HttpStatusCode.OK) {
                            emit(
                                AppResult.Success(
                                    Triple(
                                        updatedProfileImageUrl,
                                        updatedInfoImageUrl,
                                        updatedHomePageImageUrl
                                    )
                                )
                            )
                        } else {
                            emit(AppResult.Error(DataError.Remote.SERVER))
                        }
                    }

                } else {
                    emit(
                        AppResult.Success(
                            Triple(
                                null,
                                null,
                                null
                            )
                        )
                    ) // ✅ emit success but no image uploaded
                }
            } else {
                emit(AppResult.Error(DataError.Remote.SERVER))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.localizedMessage)
            emit(AppResult.Error(DataError.Remote.SERVER))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun blockDoctorDates(
        doctorId: String,
        blockedDates: List<String>
    ): Flow<AppResult<Unit, DataError.Remote>> = flow {
        try {
            // Step 1: Update doctor details first
            val patchResponse = httpClient.patch(
                "$BASE_URL/${DatabaseCollection.DOCTORS}/${doctorId}?${
                    buildUpdateMask(
                        "blockedDates",
                    )
                }"
            ) {
                contentType(ContentType.Application.Json)
                setBody(
                    DatabaseRequest(
                        fields = mapOf(
                            "blockedDates" to DatabaseValue.ArrayValue(blockedDates.map {
                                DatabaseValue.StringValue(it)
                            }),
                        )
                    )
                )
            }

            if (patchResponse.status == HttpStatusCode.OK) {
                emit(AppResult.Success(Unit))
            } else {
                emit(AppResult.Error(DataError.Remote.SERVER))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.localizedMessage)
            emit(AppResult.Error(DataError.Remote.SERVER))
        }
    }
}