package com.expensetracker.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.expensetracker.ui.utils.AppColors
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.expensetracker.data.models.Post
import com.expensetracker.data.models.UserProfile
import com.expensetracker.data.repository.ProfileRepository
import com.expensetracker.data.repository.PostRepository
import com.expensetracker.data.local.PreferencesManager
import com.expensetracker.data.remote.RetrofitClient
import com.expensetracker.ui.components.LiquidGlassCard
import com.expensetracker.ui.settings.SettingsScreen
import com.expensetracker.ui.profile.CreatePostScreen
import com.expensetracker.ui.friends.FriendsScreen
import com.expensetracker.ui.leaderboard.LeaderboardScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    profileRepository: ProfileRepository? = null,
    postRepository: PostRepository? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val retrofitClient = remember { RetrofitClient(preferencesManager) }
    val profRepo = profileRepository ?: remember { 
        ProfileRepository(retrofitClient.apiService, retrofitClient) 
    }
    val pstRepo = postRepository ?: remember {
        PostRepository(retrofitClient.apiService, retrofitClient)
    }
    val viewModel: com.expensetracker.ui.viewmodel.ProfileViewModel = viewModel(
        factory = com.expensetracker.ui.viewmodel.ProfileViewModelFactory(profRepo, pstRepo)
    )
    
    val profile by viewModel.profile.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    var showCreatePost by remember { mutableStateOf(false) }
    var showFriends by remember { mutableStateOf(false) }
    var showLeaderboard by remember { mutableStateOf(false) }
    
    val isLight = MaterialTheme.colorScheme.surface == Color.White
    
    // Обновляем данные при появлении экрана
    LaunchedEffect(Unit) {
        if (posts.isEmpty() && !isLoading) {
            viewModel.refresh()
        }
    }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                actions = {
                    IconButton(onClick = { showCreatePost = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Создать пост")
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Настройки")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreatePost = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Создать пост")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppColors.backgroundGradient(isLight))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (error != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ошибка: $error",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.refresh() }) {
                        Text("Повторить")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        ProfileHeader(profile = profile)
                    }
                    
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showFriends = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Друзья")
                            }
                            Button(
                                onClick = { showLeaderboard = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Лидерборд")
                            }
                        }
                    }
                    
                    item {
                        Text(
                            text = "Посты",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    items(
                        items = posts,
                        key = { it.id.toString() }
                    ) { post ->
                        PostCard(
                            post = post,
                            currentUserId = profile?.id,
                            onLike = { viewModel.toggleLike(post.apiId) },
                            onComment = { content ->
                                viewModel.addComment(post.apiId, content)
                            },
                            onDeleteComment = { commentId ->
                                viewModel.deleteComment(post.apiId, commentId)
                            },
                            onDeletePost = {
                                viewModel.deletePost(post.apiId)
                            }
                        )
                    }
                }
            }
        }
    }
    
    if (showSettings) {
        ModalBottomSheet(
            onDismissRequest = { showSettings = false },
            containerColor = AppColors.cardBackground(isLight)
        ) {
            SettingsScreen(
                profileRepository = profRepo,
                onLogout = {
                    showSettings = false
                    // TODO: Navigate to login
                }
            )
        }
    }
    
    if (showCreatePost) {
        ModalBottomSheet(
            onDismissRequest = { showCreatePost = false },
            containerColor = Color.Transparent
        ) {
            CreatePostScreen(
                postRepository = pstRepo,
                onPostCreated = {
                    viewModel.refresh()
                    showCreatePost = false
                }
            )
        }
    }
    
    if (showFriends) {
        ModalBottomSheet(
            onDismissRequest = { showFriends = false },
            containerColor = AppColors.cardBackground(isLight)
        ) {
            FriendsScreen(
                friendRepository = null
            )
        }
    }
    
    if (showLeaderboard) {
        ModalBottomSheet(
            onDismissRequest = { showLeaderboard = false },
            containerColor = AppColors.cardBackground(isLight)
        ) {
            LeaderboardScreen(
                apiService = retrofitClient.apiService
            )
        }
    }
}

@Composable
fun ProfileHeader(profile: UserProfile?) {
    LiquidGlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile?.name ?: "Загрузка...",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = profile?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            if (profile?.avatar != null) {
                AsyncImage(
                    model = profile.avatar,
                    contentDescription = "Аватар",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile?.name?.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    currentUserId: String?,
    onLike: () -> Unit,
    onComment: (String) -> Unit,
    onDeleteComment: (String) -> Unit,
    onDeletePost: () -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    var showComments by remember { mutableStateOf(false) }
    val isLiked = remember(post.likes) {
        post.likes.any { it.userId.toString() == currentUserId }
    }
    
    LiquidGlassCard {
        Column {
            // Автор поста
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (post.author.avatar != null) {
                    AsyncImage(
                        model = post.author.avatar,
                        contentDescription = "Аватар",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.author.name.firstOrNull()?.toString() ?: "?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = post.author.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                if (post.authorId == currentUserId) {
                    IconButton(onClick = onDeletePost) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Содержание поста
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Изображение поста
            if (post.imageUrl != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = "Изображение поста",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Действия
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextButton(
                    onClick = onLike,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("❤️ ${post.likes.size}")
                }
                TextButton(
                    onClick = { showComments = !showComments }
                ) {
                    Text("💬 ${post.comments.size}")
                }
            }
            
            // Комментарии
            if (showComments) {
                Spacer(modifier = Modifier.height(8.dp))
                post.comments.forEach { comment ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = comment.author.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = comment.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                        if (comment.authorId == currentUserId) {
                            IconButton(
                                onClick = { onDeleteComment(comment.apiId) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Удалить",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // Форма добавления комментария
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        label = { Text("Комментарий") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                onComment(commentText)
                                commentText = ""
                            }
                        }
                    ) {
                        Text("Отправить")
                    }
                }
            }
        }
    }
}

