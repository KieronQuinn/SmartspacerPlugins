package com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories

import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.Result
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleApiRepository.Scope
import com.kieronquinn.app.smartspacer.plugin.googlewallet.targets.GoogleWalletDynamicTarget
import com.kieronquinn.app.smartspacer.plugin.googlewallet.targets.GoogleWalletValuableTarget
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.firstNotNull
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.parseCookie
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getPackageInfoCompat
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.*

interface GoogleApiRepository {

    suspend fun getAasToken(oauthToken: String): String?
    suspend fun getToken(scope: Scope): String?
    fun isSignedIn(): Boolean

    enum class Scope(val service: String, val clientSig: String, val packageName: String) {
        AC2DM(
            "ac2dm",
            "38918a453d07199354f8b19af05ec6562ced5788",
            "com.google.android.gms"
        ),
        WALLET(
            "oauth2:https://www.googleapis.com/auth/tapandpay",
            "38918a453d07199354f8b19af05ec6562ced5788",
            "com.google.android.gms"
        )
    }

}

class GoogleApiRepositoryImpl(
    private val context: Context,
    settings: EncryptedSettingsRepository
): GoogleApiRepository {

    companion object {
        private const val PACKAGE_GMS = "com.google.android.gms"
        private const val VERSION_GMS_DEFAULT = "19629032"
    }

    private val packageManager = context.packageManager
    private val aasToken = settings.aasToken
    private val scope = MainScope()

    private val service = Retrofit.Builder()
        .baseUrl("http://localhost/")
        .build()
        .create(GoogleApiService::class.java)

    private val isSignedIn = aasToken
        .asFlow()
        .map { it.isNotEmpty() }
        .stateIn(scope, SharingStarted.Eagerly, null)

    override suspend fun getAasToken(oauthToken: String) = withContext(Dispatchers.IO) {
        val tokenResult = service.run {
            getAasToken(
                oauthToken = oauthToken,
                playServicesVersion = getPlayServicesVersion()
            )
        }
        val tokenBody = when(tokenResult){
            is Result.Success -> String(tokenResult.data.bytes())
            is Result.Failed -> return@withContext null
        }
        tokenBody.decodeForm()["Token"]
    }

    override suspend fun getToken(scope: Scope): String? = withContext(Dispatchers.IO) {
        val aasToken = aasToken.get()
        if(aasToken.isEmpty()) return@withContext null
        val tokenResult = service.run {
            getToken(
                aasToken = aasToken,
                playServicesVersion = getPlayServicesVersion(),
                headerApp = scope.packageName,
                app = scope.packageName,
                service = scope.service,
                clientSig = scope.clientSig,
                callerSig = scope.clientSig,
                callerPackage = scope.packageName
            )
        }
        val tokenBody = when(tokenResult){
            is Result.Success -> String(tokenResult.data.bytes())
            is Result.Failed -> {
                if(tokenResult.code == 403) {
                    //AAS Token has been revoked
                    this@GoogleApiRepositoryImpl.aasToken.clear()
                }
                return@withContext null
            }
        }
        tokenBody.decodeForm()["Auth"]
    }

    override fun isSignedIn(): Boolean {
        return runBlocking {
            isSignedIn.firstNotNull()
        }
    }

    private fun getPlayServicesVersion(): String {
        return try {
            packageManager.getPackageInfoCompat(PACKAGE_GMS).longVersionCode.toString()
        }catch (e: NameNotFoundException){
            VERSION_GMS_DEFAULT
        }
    }

    private fun <T> GoogleApiService.run(block: GoogleApiService.() -> Call<T>): Result<T> {
        val result = try {
            block(this).execute()
        }catch (e: Exception) {
            return Result.Failed(999)
        }
        val body = result.body()
        return if(result.isSuccessful && body != null) {
            Result.Success(body)
        }else{
            Result.Failed(result.code())
        }
    }

    private fun String.decodeForm(): Map<String, String> {
        return lines().associate {
            it.parseCookie()
        }
    }

    private fun setupSignInListener() = scope.launch {
        isSignedIn.collect {
            SmartspacerTargetProvider.notifyChange(context, GoogleWalletDynamicTarget::class.java)
            SmartspacerTargetProvider.notifyChange(context, GoogleWalletValuableTarget::class.java)
        }
    }

    init {
        setupSignInListener()
    }

}

interface GoogleApiService {

    companion object {
        private fun getLocale(): String {
            val locale = Locale.getDefault()
            return "${locale.language}-${locale.country}"
        }

        private fun getCountry(): String {
            return Locale.getDefault().country.lowercase()
        }
    }

    @FormUrlEncoded
    @POST("https://android.clients.google.com/auth")
    fun getAasToken(
        @Field("Token") oauthToken: String,
        @Field("lang") lang: String = getLocale(),
        @Field("google_play_services_version") playServicesVersion: String,
        @Field("sdk_version") sdkVersion: String = Build.VERSION.SDK_INT.toString(),
        @Field("device_country") deviceCountry: String = getCountry(),
        @Field("service") service: String = Scope.AC2DM.service,
        @Field("get_accountid") getAccountId: String = "1",
        @Field("ACCESS_TOKEN") systemPartition: String = "1",
        @Field("callerPkg") callerPackage: String = Scope.AC2DM.packageName,
        @Field("callerSig") callerSig: String = Scope.AC2DM.clientSig,
        @Field("add_account") addAccount: String = "1"
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("https://android.googleapis.com/auth")
    fun getToken(
        @Header("app") headerApp: String,
        @Field("lang") lang: String = getLocale(),
        @Field("google_play_services_version") playServicesVersion: String,
        @Field("sdk_version") sdkVersion: String = Build.VERSION.SDK_INT.toString(),
        @Field("device_country") deviceCountry: String = getCountry(),
        @Field("check_email") checkEmail: String = "1",
        @Field("oauth2_foreground") oauth2Foreground: String = "1",
        @Field("token_request_options") tokenRequestOptions: String = "CAA4AVAB",
        @Field("app") app: String,
        @Field("service") service: String,
        @Field("client_sig") clientSig: String,
        @Field("system_partition") systemPartition: String = "1",
        @Field("callerPkg") callerPackage: String,
        @Field("Token") aasToken: String,
        @Field("callerSig") callerSig: String
    ): Call<ResponseBody>

}