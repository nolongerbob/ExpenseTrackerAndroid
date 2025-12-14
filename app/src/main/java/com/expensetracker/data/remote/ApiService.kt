package com.expensetracker.data.remote

import com.expensetracker.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Auth
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    // Profile
    @GET("/api/profile")
    suspend fun getProfile(): Response<UserProfileResponse>
    
    @PUT("/api/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UpdateProfileResponse>
    
    // Expenses
    @GET("/api/expenses")
    suspend fun getExpenses(): Response<List<ExpenseResponse>>
    
    @POST("/api/expenses")
    suspend fun createExpense(@Body request: CreateExpenseRequest): Response<ExpenseResponse>
    
    @PUT("/api/expenses/{id}")
    suspend fun updateExpense(@Path("id") id: String, @Body request: UpdateExpenseRequest): Response<ExpenseResponse>
    
    @DELETE("/api/expenses/{id}")
    suspend fun deleteExpense(@Path("id") id: String): Response<EmptyResponse>
    
    // Categories
    @GET("/api/categories")
    suspend fun getCategories(@Query("type") type: String? = null): Response<List<CategoryResponse>>
    
    @POST("/api/categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): Response<CategoryResponse>
    
    @DELETE("/api/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String): Response<EmptyResponse>
    
    // Notes
    @GET("/api/notes")
    suspend fun getNotes(): Response<List<NoteResponse>>
    
    @POST("/api/notes")
    suspend fun createNote(@Body request: CreateNoteRequest): Response<NoteResponse>
    
    @PUT("/api/notes/{id}")
    suspend fun updateNote(@Path("id") id: String, @Body request: UpdateNoteRequest): Response<NoteResponse>
    
    @DELETE("/api/notes/{id}")
    suspend fun deleteNote(@Path("id") id: String): Response<EmptyResponse>
    
    // Posts
    @GET("/api/posts")
    suspend fun getPosts(): Response<PostsResponse>
    
    @POST("/api/posts")
    suspend fun createPost(@Body request: CreatePostRequest): Response<PostResponseWrapper>
    
    @PUT("/api/posts/{id}")
    suspend fun updatePost(@Path("id") id: String, @Body request: UpdatePostRequest): Response<PostResponse>
    
    @DELETE("/api/posts/{id}")
    suspend fun deletePost(@Path("id") id: String): Response<EmptyResponse>
    
    @POST("/api/posts/{id}/like")
    suspend fun toggleLike(@Path("id") id: String): Response<LikeResponse>
    
    @POST("/api/posts/{id}/comments")
    suspend fun addComment(@Path("id") id: String, @Body request: AddCommentRequest): Response<CommentResponseWrapper>
    
    @DELETE("/api/posts/{postId}/comments/{commentId}")
    suspend fun deleteComment(@Path("postId") postId: String, @Path("commentId") commentId: String): Response<EmptyResponse>
    
    // Friends
    @GET("/api/friends")
    suspend fun getFriends(): Response<List<FriendResponse>>
    
    @POST("/api/friends")
    suspend fun addFriend(@Body request: AddFriendRequest): Response<FriendRequestResponse>
    
    @GET("/api/friends/requests")
    suspend fun getFriendRequests(): Response<FriendRequestsResponse>
    
    @POST("/api/friends/respond")
    suspend fun respondToFriendRequest(@Body request: RespondFriendRequest): Response<EmptyResponse>
    
    // Leaderboard
    @GET("/api/leaderboard")
    suspend fun getLeaderboard(): Response<List<LeaderboardResponse>>
    
    // Upload
    @Multipart
    @POST("/api/upload")
    suspend fun uploadImage(@Part file: okhttp3.MultipartBody.Part): Response<UploadResponse>
}

// Request/Response models
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val email: String, val password: String, val name: String? = null)
data class AuthResponse(val token: String, val user: UserProfileResponse)

data class UpdateProfileRequest(val name: String? = null, val image: String? = null)
data class UpdateProfileResponse(val message: String?, val user: UserProfileResponse)

data class CreateExpenseRequest(
    val amount: Double,
    val currency: String = "RUB",
    val categoryId: String? = null,
    val note: String? = null,
    val type: String = "EXPENSE"
)

data class UpdateExpenseRequest(
    val amount: Double? = null,
    val categoryId: String? = null,
    val note: String? = null,
    val type: String? = null,
    val spentAt: String? = null
)

data class CreateCategoryRequest(val name: String, val color: String, val type: String = "EXPENSE")
data class CreateNoteRequest(
    val title: String,
    val content: String? = null,
    val noteDate: String? = null,
    val reminderDate: String? = null
)

data class UpdateNoteRequest(
    val title: String? = null,
    val content: String? = null,
    val noteDate: String? = null,
    val reminderDate: String? = null
)

data class CreatePostRequest(val content: String, val imageUrl: String? = null)
data class UpdatePostRequest(val content: String)
data class AddCommentRequest(val content: String)
data class AddFriendRequest(val email: String)
data class RespondFriendRequest(val requestId: String, val action: String)

data class ExpenseResponse(
    val id: String,
    val amount: String,
    val currency: String,
    val note: String?,
    val spentAt: String,
    val category: CategoryResponse?,
    val categoryId: String?,
    val type: String?
)

data class CategoryResponse(
    val id: String,
    val name: String,
    val color: String,
    val type: String?
)

data class NoteResponse(
    val id: String,
    val title: String,
    val content: String?,
    val noteDate: String,
    val reminderDate: String?,
    val createdAt: String,
    val updatedAt: String
)

data class UserProfileResponse(
    val id: String,
    val email: String,
    val name: String?,
    val image: String?
)

data class PostResponse(
    val id: String,
    val content: String,
    val imageUrl: String?,
    val createdAt: String,
    val author: AuthorResponse,
    val likes: List<LikeResponse>,
    val comments: List<CommentResponse>,
    val _count: PostCountResponse?
)

data class PostResponseWrapper(val post: PostResponse)
data class PostsResponse(val posts: List<PostResponse>)

data class AuthorResponse(
    val id: String,
    val name: String?,
    val image: String?
)

data class LikeResponse(
    val userId: String?,
    val postId: String?,
    val liked: Boolean?
)

data class CommentResponse(
    val id: String,
    val content: String,
    val createdAt: String,
    val author: AuthorResponse
)

data class CommentResponseWrapper(val comment: CommentResponse)
data class PostCountResponse(val likes: Int, val comments: Int)

data class FriendResponse(
    val id: String,
    val name: String?,
    val email: String,
    val image: String?
)

data class FriendRequestResponse(val id: String, val addressee: FriendResponse)
data class FriendRequestsResponse(val incoming: List<FriendRequestItem>, val outgoing: List<FriendRequestItem>)
data class FriendRequestItem(val id: String, val user: FriendResponse, val createdAt: String)

data class LeaderboardResponse(
    val userId: String,
    val name: String,
    val total: String,
    val image: String?
)

data class UploadResponse(
    val url: String,
    val filename: String? = null,
    val public_id: String? = null,
    val format: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val size: Int? = null,
    val mimetype: String? = null
)

data class EmptyResponse(val success: Boolean? = null)

