package com.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.data.models.Post
import com.expensetracker.data.models.UserProfile
import com.expensetracker.data.repository.PostRepository
import com.expensetracker.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val postRepository: PostRepository
) : ViewModel() {
    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()
    
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadProfile()
        loadPosts()
    }
    
    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            profileRepository.getProfile()
                .onSuccess { profile ->
                    _profile.value = profile
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun loadPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            postRepository.getPosts()
                .onSuccess { posts ->
                    _posts.value = posts
                    android.util.Log.d("ProfileViewModel", "Loaded ${posts.size} posts")
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _error.value = exception.message
                    android.util.Log.e("ProfileViewModel", "Failed to load posts", exception)
                    _isLoading.value = false
                }
        }
    }
    
    fun toggleLike(postId: String) {
        viewModelScope.launch {
            postRepository.toggleLike(postId)
                .onSuccess { liked ->
                    // Обновляем состояние поста
                    _posts.value = _posts.value.map { post ->
                        if (post.apiId == postId) {
                            // Обновляем лайки
                            val currentUserId = _profile.value?.id
                            if (liked) {
                                // Добавляем лайк
                                if (currentUserId != null && !post.likes.any { it.userId.toString() == currentUserId }) {
                                    val userId = try {
                                        java.util.UUID.fromString(currentUserId)
                                    } catch (e: IllegalArgumentException) {
                                        java.util.UUID.nameUUIDFromBytes(currentUserId.toByteArray())
                                    }
                                    post.copy(likes = post.likes + com.expensetracker.data.models.Like(
                                        id = java.util.UUID.randomUUID(),
                                        userId = userId
                                    ))
                                } else {
                                    post
                                }
                            } else {
                                // Удаляем лайк
                                post.copy(likes = post.likes.filter { it.userId.toString() != currentUserId })
                            }
                        } else {
                            post
                        }
                    }
                }
        }
    }
    
    fun addComment(postId: String, content: String) {
        viewModelScope.launch {
            postRepository.addComment(postId, content)
                .onSuccess { comment ->
                    _posts.value = _posts.value.map { post ->
                        if (post.apiId == postId) {
                            post.copy(comments = post.comments + comment)
                        } else {
                            post
                        }
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
        }
    }
    
    fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            postRepository.deleteComment(postId, commentId)
                .onSuccess {
                    _posts.value = _posts.value.map { post ->
                        if (post.apiId == postId) {
                            post.copy(comments = post.comments.filter { it.apiId != commentId })
                        } else {
                            post
                        }
                    }
                }
        }
    }
    
    fun deletePost(postId: String) {
        viewModelScope.launch {
            postRepository.deletePost(postId)
                .onSuccess {
                    _posts.value = _posts.value.filter { it.apiId != postId }
                }
        }
    }
    
    fun refresh() {
        loadProfile()
        loadPosts()
    }
}

