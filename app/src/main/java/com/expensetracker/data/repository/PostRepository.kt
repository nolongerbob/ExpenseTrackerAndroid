package com.expensetracker.data.repository

import com.expensetracker.data.models.*
import com.expensetracker.data.remote.*
import com.expensetracker.data.remote.RetrofitClient
import java.text.SimpleDateFormat
import java.util.*

class PostRepository(
    private val apiService: ApiService,
    private val retrofitClient: RetrofitClient
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    suspend fun getPosts(): Result<List<Post>> {
        return try {
            android.util.Log.d("PostRepository", "Fetching posts from API...")
            val response = apiService.getPosts()
            android.util.Log.d("PostRepository", "Response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("PostRepository", "Response body posts size: ${response.body()!!.posts.size}")
                val posts = response.body()!!.posts.map { postResponse ->
                    Post(
                        id = safeParseUUID(postResponse.id),
                        apiId = postResponse.id,
                        content = postResponse.content,
                        author = UserProfile(
                            id = postResponse.author.id,
                            name = postResponse.author.name ?: postResponse.author.id,
                            email = postResponse.author.id,
                            avatar = postResponse.author.image
                        ),
                        authorId = postResponse.author.id,
                        createdAt = parseDate(postResponse.createdAt) ?: System.currentTimeMillis(),
                        imageUrl = postResponse.imageUrl?.let { retrofitClient.getImageUrl(it) },
                        likes = postResponse.likes.mapNotNull { likeResponse ->
                            likeResponse.userId?.let { userId ->
                                Like(
                                    id = UUID.randomUUID(),
                                    userId = safeParseUUID(userId)
                                )
                            }
                        },
                        comments = postResponse.comments.map { commentResponse ->
                            Comment(
                                id = safeParseUUID(commentResponse.id),
                                apiId = commentResponse.id,
                                content = commentResponse.content,
                                author = UserProfile(
                                    id = commentResponse.author.id,
                                    name = commentResponse.author.name ?: commentResponse.author.id,
                                    email = commentResponse.author.id,
                                    avatar = commentResponse.author.image
                                ),
                                authorId = commentResponse.author.id,
                                createdAt = parseDate(commentResponse.createdAt) ?: System.currentTimeMillis()
                            )
                        }
                    )
                }
                Result.success(posts)
            } else {
                Result.failure(Exception("Failed to fetch posts: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createPost(content: String, imageUrl: String?): Result<Post> {
        return try {
            val request = CreatePostRequest(content, imageUrl)
            val response = apiService.createPost(request)
            if (response.isSuccessful && response.body() != null) {
                val postResponse = response.body()!!.post
                val post = Post(
                    id = safeParseUUID(postResponse.id),
                    apiId = postResponse.id,
                    content = postResponse.content,
                    author = UserProfile(
                        id = postResponse.author.id,
                        name = postResponse.author.name ?: postResponse.author.id,
                        email = postResponse.author.id,
                        avatar = postResponse.author.image
                    ),
                    authorId = postResponse.author.id,
                    createdAt = parseDate(postResponse.createdAt) ?: System.currentTimeMillis(),
                    imageUrl = postResponse.imageUrl?.let { retrofitClient.getImageUrl(it) },
                    likes = postResponse.likes.mapNotNull { likeResponse ->
                        likeResponse.userId?.let { userId ->
                            Like(
                                id = UUID.randomUUID(),
                                userId = UUID.fromString(userId)
                            )
                        }
                    },
                    comments = postResponse.comments.map { commentResponse ->
                        Comment(
                            id = UUID.fromString(commentResponse.id),
                            apiId = commentResponse.id,
                            content = commentResponse.content,
                            author = UserProfile(
                                id = commentResponse.author.id,
                                name = commentResponse.author.name ?: commentResponse.author.id,
                                email = commentResponse.author.id,
                                avatar = commentResponse.author.image
                            ),
                            authorId = commentResponse.author.id,
                            createdAt = parseDate(commentResponse.createdAt) ?: System.currentTimeMillis()
                        )
                    }
                )
                Result.success(post)
            } else {
                Result.failure(Exception("Failed to create post: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updatePost(id: String, content: String): Result<Post> {
        return try {
            val request = UpdatePostRequest(content)
            val response = apiService.updatePost(id, request)
            if (response.isSuccessful && response.body() != null) {
                val postResponse = response.body()!!
                val post = Post(
                    id = safeParseUUID(postResponse.id),
                    apiId = postResponse.id,
                    content = postResponse.content,
                    author = UserProfile(
                        id = postResponse.author.id,
                        name = postResponse.author.name ?: postResponse.author.id,
                        email = postResponse.author.id,
                        avatar = postResponse.author.image
                    ),
                    authorId = postResponse.author.id,
                    createdAt = parseDate(postResponse.createdAt) ?: System.currentTimeMillis(),
                    imageUrl = postResponse.imageUrl?.let { retrofitClient.getImageUrl(it) },
                    likes = postResponse.likes.mapNotNull { likeResponse ->
                        likeResponse.userId?.let { userId ->
                            Like(
                                id = UUID.randomUUID(),
                                userId = UUID.fromString(userId)
                            )
                        }
                    },
                    comments = postResponse.comments.map { commentResponse ->
                        Comment(
                            id = UUID.fromString(commentResponse.id),
                            apiId = commentResponse.id,
                            content = commentResponse.content,
                            author = UserProfile(
                                id = commentResponse.author.id,
                                name = commentResponse.author.name ?: commentResponse.author.id,
                                email = commentResponse.author.id,
                                avatar = commentResponse.author.image
                            ),
                            authorId = commentResponse.author.id,
                            createdAt = parseDate(commentResponse.createdAt) ?: System.currentTimeMillis()
                        )
                    }
                )
                Result.success(post)
            } else {
                Result.failure(Exception("Failed to update post: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deletePost(id: String): Result<Unit> {
        return try {
            val response = apiService.deletePost(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete post: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun toggleLike(postId: String): Result<Boolean> {
        return try {
            val response = apiService.toggleLike(postId)
            if (response.isSuccessful && response.body() != null) {
                val likeResponse = response.body()!!
                Result.success(likeResponse.liked ?: false)
            } else {
                Result.failure(Exception("Failed to toggle like: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun addComment(postId: String, content: String): Result<Comment> {
        return try {
            val request = AddCommentRequest(content)
            val response = apiService.addComment(postId, request)
            if (response.isSuccessful && response.body() != null) {
                val commentResponse = response.body()!!.comment
                val comment = Comment(
                    id = safeParseUUID(commentResponse.id),
                    apiId = commentResponse.id,
                    content = commentResponse.content,
                    author = UserProfile(
                        id = commentResponse.author.id,
                        name = commentResponse.author.name ?: commentResponse.author.id,
                        email = commentResponse.author.id,
                        avatar = commentResponse.author.image
                    ),
                    authorId = commentResponse.author.id,
                    createdAt = parseDate(commentResponse.createdAt) ?: System.currentTimeMillis()
                )
                Result.success(comment)
            } else {
                Result.failure(Exception("Failed to add comment: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteComment(postId: String, commentId: String): Result<Unit> {
        return try {
            val response = apiService.deleteComment(postId, commentId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete comment: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun parseDate(dateString: String): Long? {
        return try {
            dateFormat.parse(dateString)?.time
        } catch (e: Exception) {
            null
        }
    }
    
    private fun safeParseUUID(id: String): UUID {
        return try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            // Если ID не является валидным UUID, создаем детерминированный UUID из строки
            android.util.Log.w("PostRepository", "Invalid UUID format: $id, generating deterministic UUID")
            UUID.nameUUIDFromBytes(id.toByteArray())
        }
    }
}

