package com.example.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SkillApiService {

    @GET("skills")
    suspend fun getRemoteSkills(
        @Query("limit") limit: Int = 10
    ): Response<List<RemoteSkillDto>>

    @GET("health")
    suspend fun checkApiHealth(): Response<ApiStatusResponse>
}
