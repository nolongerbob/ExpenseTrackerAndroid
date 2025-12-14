package com.expensetracker.data.repository

import com.expensetracker.data.models.UserProfile
import com.expensetracker.data.remote.ApiService
import com.expensetracker.data.remote.RetrofitClient
import com.expensetracker.data.remote.UpdateProfileRequest

class ProfileRepository(
    private val apiService: ApiService,
    private val retrofitClient: RetrofitClient
) {
    suspend fun getProfile(): Result<UserProfile> {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body() != null) {
                val profileResponse = response.body()!!
                val profile = UserProfile(
                    id = profileResponse.id,
                    name = profileResponse.name ?: profileResponse.id,
                    email = profileResponse.email,
                    avatar = profileResponse.image?.let { retrofitClient.getImageUrl(it) }
                )
                Result.success(profile)
            } else {
                Result.failure(Exception("Failed to fetch profile: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateProfile(name: String?, imageUrl: String?): Result<UserProfile> {
        return try {
            val request = UpdateProfileRequest(name, imageUrl)
            val response = apiService.updateProfile(request)
            if (response.isSuccessful && response.body() != null) {
                val profileResponse = response.body()!!.user
                val profile = UserProfile(
                    id = profileResponse.id,
                    name = profileResponse.name ?: profileResponse.id,
                    email = profileResponse.email,
                    avatar = profileResponse.image?.let { retrofitClient.getImageUrl(it) }
                )
                Result.success(profile)
            } else {
                Result.failure(Exception("Failed to update profile: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


