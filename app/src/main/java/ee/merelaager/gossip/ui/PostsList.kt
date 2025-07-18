package ee.merelaager.gossip.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Panorama
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import ee.merelaager.gossip.Screen
import ee.merelaager.gossip.data.model.Post
import ee.merelaager.gossip.ui.theme.GossipPink
import ee.merelaager.gossip.util.formatCreatedAt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostsPagingList(
    posts: LazyPagingItems<Post>,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry = navController.currentBackStackEntry
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    // https://stackoverflow.com/a/79599377
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    val onRefresh: () -> Unit = {
        isRefreshing = true
        posts.refresh()
    }

    val isLoading = posts.loadState.refresh is LoadState.Loading
    LaunchedEffect(isLoading) {
        isRefreshing = isRefreshing && isLoading
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = pullToRefreshState
    ) {
        LazyColumn(modifier = modifier) {
            if (posts.itemCount == 0 && !isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Postitusi pole.")
                        }
                    }
                }
            } else {
                items(posts.itemCount) { index ->
                    posts[index]?.let { post ->
                        Column {
                            ListPost(post, Modifier.fillMaxWidth()) {
                                navController.navigate("post/${post.id}?from=$currentRoute")
                            }

                            if (index < posts.itemCount) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.2f) else Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }

            when {
                posts.loadState.append == LoadState.Loading -> {
                    item {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    }
                }

//                posts.loadState.refresh == LoadState.Loading -> {
//                    if (posts.itemCount != 0) {
//                        isRefreshing = true
//                    }
//                }

                posts.loadState.append is LoadState.Error -> {
                    val e = posts.loadState.append as LoadState.Error
                    item {
                        Text("Error loading more: ${e.error.localizedMessage}")
                    }
                }

                posts.loadState.refresh is LoadState.Error -> {
                    val e = posts.loadState.refresh as LoadState.Error
                    item {
                        Text("Error loading: ${e.error.localizedMessage}")
                    }
                }
            }
        }
    }
}

@Composable
fun ListPost(post: Post, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp)
            .padding(bottom = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = post.title, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = formatCreatedAt(post.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
            )
        }
        post.imageId?.let {
            Icon(
                imageVector = Icons.Outlined.Panorama,
                contentDescription = "Contains image",
                modifier = Modifier.padding(vertical = 8.dp),
                tint = Color.Gray
            )
        }
        post.content?.let { Text(text = it, maxLines = 3, overflow = TextOverflow.Ellipsis) }

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (post.isLiked) "Liked" else "Not liked",
                tint = if (post.isLiked) GossipPink else Color.Gray
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${post.likeCount}",
                color = if (post.isLiked) GossipPink else Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}
