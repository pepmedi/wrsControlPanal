package di

import DATA_STORE_FILE_NAME
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import createDataStore
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module
    get() = module {
        single<DataStore<Preferences>> { createDataStore { DATA_STORE_FILE_NAME } }
        single<HttpClientEngine> { OkHttp.create() }
    }