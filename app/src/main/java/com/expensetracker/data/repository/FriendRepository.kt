package com.expensetracker.data.repository

import com.expensetracker.data.models.Friend
import com.expensetracker.data.models.UserProfile
import com.expensetracker.data.remote.ApiService
import com.expensetracker.data.remote.RetrofitClient
import com.expensetracker.data.remote.AddFriendRequest
import com.expensetracker.data.remote.RespondFriendRequest

class FriendRepository(
    private val apiService: ApiService,
    private val retrofitClient: RetrofitClient
) {
    suspend fun getFriends(): Result<List<Friend>> {
        return try {
            val response = apiService.getFriends()
            if (response.isSuccessful && response.body() != null) {
                val friends = response.body()!!.map { friendResponse ->
                    Friend(
                        id = friendResponse.id,
                        name = friendResponse.name ?: friendResponse.id,
                        email = friendResponse.email,
                        avatar = friendResponse.image?.let { retrofitClient.getImageUrl(it) }
                    )
                }
                Result.success(friends)
            } else {
                Result.failure(Exception("Failed to fetch friends: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun addFriend(email: String): Result<Unit> {
        return try {
            val request = AddFriendRequest(email)
            val response = apiService.addFriend(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to add friend: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getFriendRequests(): Result<Pair<List<FriendRequest>, List<FriendRequest>>> {
        return try {
            val response = apiService.getFriendRequests()
            if (response.isSuccessful && response.body() != null) {
                val requestsResponse = response.body()!!
                val incoming = requestsResponse.incoming.map { requestItem ->
                    FriendRequest(
                        id = requestItem.id,
                        user = Friend(
                            id = requestItem.user.id,
                            name = requestItem.user.name ?: requestItem.user.id,
                            email = requestItem.user.email,
                            avatar = requestItem.user.image?.let { retrofitClient.getImageUrl(it) }
                        ),
                        createdAt = requestItem.createdAt
                    )
                }
                val outgoing = requestsResponse.outgoing.map { requestItem ->
                    FriendRequest(
                        id = requestItem.id,
                        user = Friend(
                            id = requestItem.user.id,
                            name = requestItem.user.name ?: requestItem.user.id,
                            email = requestItem.user.email,
                            avatar = requestItem.user.image?.let { retrofitClient.getImageUrl(it) }
                        ),
                        createdAt = requestItem.createdAt
                    )
                }
                Result.success(Pair(incoming, outgoing))
            } else {
                Result.failure(Exception("Failed to fetch friend requests: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun respondToFriendRequest(requestId: String, accept: Boolean): Result<Unit> {
        return try {
            val request = RespondFriendRequest(requestId, if (accept) "accept" else "reject")
            val response = apiService.respondToFriendRequest(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to respond to friend request: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class FriendRequest(
    val id: String,
    val user: Friend,
    val createdAt: String
)


