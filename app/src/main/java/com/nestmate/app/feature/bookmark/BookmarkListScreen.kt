package com.nestmate.app.feature.bookmark

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nestmate.app.data.model.Bookmark
import com.nestmate.app.data.model.BookmarkItemType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkListScreen(
    viewModel: BookmarkListViewModel,
    onListingClick: (String) -> Unit,
    onRequirementClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.errorMessage != null) {
        Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text(text = state.errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
        return
    }

    if (state.bookmarks.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No saved items yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(state.bookmarks, key = { it.itemId }) { bookmark ->
            BookmarkCard(
                bookmark = bookmark,
                onClick = {
                    when (bookmark.itemType) {
                        BookmarkItemType.LISTING -> onListingClick(bookmark.itemId)
                        BookmarkItemType.REQUIREMENT -> onRequirementClick(bookmark.itemId)
                    }
                }
            )
        }
    }
}

@Composable
private fun BookmarkCard(
    bookmark: Bookmark,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bookmark.snapshot.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Badge {
                    Text(if (bookmark.itemType == BookmarkItemType.LISTING) "Room" else "Seeker")
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${bookmark.snapshot.currency} ${bookmark.snapshot.price}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = bookmark.snapshot.locationString,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
