package blog.screen

import PrimaryAppColor
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.ripple
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import blog.domain.BlogMaster
import blog.viewModel.AllBLogListViewModel
import blog.viewModel.BLogListUiState
import blog.viewModel.BlogListActions
import coil3.compose.AsyncImage
import component.AppCircularProgressIndicator
import component.SlideInScreen
import core.components.SearchBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BlogListScreenRoot(viewModel: AllBLogListViewModel = koinViewModel()) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    BlogListScreen(
        uiState = uiState,
        onAction = { action ->
            viewModel.onAction(action)
        })
}

@Composable
fun BlogListScreen(
    uiState: BLogListUiState,
    onAction: (BlogListActions) -> Unit
) {
    var expandedCardId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var addBlogUiScreen by remember { mutableStateOf(false) }
    var filteredBlog by remember { mutableStateOf<List<BlogMaster>>(emptyList()) }

    var showUpdateBlogScreen by remember { mutableStateOf(false) }
    var currentBlogId by remember { mutableStateOf("") }

    LaunchedEffect(searchQuery, uiState) {
        withContext(Dispatchers.Default) {
            val filtered = uiState.blogList.filter {
                it.title.contains(searchQuery, ignoreCase = true)
            }
            withContext(Dispatchers.Main) {
                filteredBlog = filtered
            }
        }
    }

    val displayedBlogs = remember(searchQuery, uiState.blogList) {
        if (searchQuery.isNotEmpty()) filteredBlog else uiState.blogList
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            if (uiState.isLoading) {
                AppCircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                SearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 300.dp),
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayedBlogs) { blog ->
                        BlogCardItem(
                            blog = blog,
                            modifier = Modifier.animateItem(),
                            onUpdateClick = {
                                currentBlogId = blog.id
                                showUpdateBlogScreen = true
                                expandedCardId = null
                            },
                            onBlockClick = { onAction(BlogListActions.ChangeBlogStatus(it)) },
                            isExpanded = expandedCardId == blog.id,
                            onExpand = { expandedCardId = blog.id },
                            onCollapse = { expandedCardId = null }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { addBlogUiScreen = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape = RoundedCornerShape(corner = CornerSize(8.dp)),
            containerColor = PrimaryAppColor
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Shop")
        }

        SlideInScreen(visible = addBlogUiScreen) {
            AddBlogScreenRoot(
                onBackClick = {
                    addBlogUiScreen = false
                },
                onBlogAdded = { addedBlog ->
                    if (addedBlog != null) {
                        onAction(BlogListActions.OnBlogAdded(addedBlog))
                    }
                })
        }

        SlideInScreen(visible = showUpdateBlogScreen) {
            UpdateBlogScreenRoot(
                blog = uiState.blogList.find { it.id == currentBlogId }!!,
                onBack = {
                    showUpdateBlogScreen = false
                },
                onBlogUpdated = {
                    if (it != null) {
                        onAction(BlogListActions.OnBlogUpdated(it))
                    }
                }
            )
        }
    }
}

@Composable
fun BlogCardItem(
    blog: BlogMaster,
    modifier: Modifier,
    onUpdateClick: (BlogMaster) -> Unit,
    onBlockClick: (BlogMaster) -> Unit,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
) {
    val isBlogBlocked = blog.blogActive == "1"
    val backgroundColor = if (isBlogBlocked) Color.LightGray else Color.White
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(5.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.LightGray)
            ) { if (isExpanded) onCollapse() else onExpand() },

        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Blog image
            AsyncImage(
                model = blog.imageUrl,
                contentDescription = "Blog Image",
                modifier = Modifier
                    .height(250.dp)
                    .clip(RoundedCornerShape(topEnd = 12.dp, topStart = 12.dp)),
                contentScale = ContentScale.FillBounds
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .animateContentSize()
            ) {

                // Blog title
                Text(
                    text = blog.title.trim(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Blog description
                Text(
                    text = blog.description.trim(),
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                if (isExpanded) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            modifier = Modifier.padding(end = 10.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.textButtonColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, PrimaryAppColor),
                            onClick = { onUpdateClick(blog) }
                        ) {
                            Text("Update")
                        }

                        TextButton(
                            modifier = Modifier.padding(end = 10.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.textButtonColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, PrimaryAppColor),
                            onClick = { onBlockClick(blog) }
                        ) {
                            Text(text = if (isBlogBlocked) "UnBlock" else "Block")
                        }
                    }
                }
            }
        }
    }
}