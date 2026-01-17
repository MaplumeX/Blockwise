package com.maplume.blockwise.feature.settings.presentation.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * License information for an open source library.
 */
data class LibraryLicense(
    val name: String,
    val author: String,
    val license: String,
    val url: String? = null
)

/**
 * License screen showing open source libraries used in the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseScreen(
    onNavigateBack: () -> Unit
) {
    val licenses = remember { getOpenSourceLicenses() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("开源许可") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            items(licenses) { license ->
                LicenseItem(license)
            }
        }
    }
}

@Composable
private fun LicenseItem(license: LibraryLicense) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = license.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = license.author,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = license.license,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun getOpenSourceLicenses(): List<LibraryLicense> = listOf(
    LibraryLicense(
        name = "Kotlin",
        author = "JetBrains",
        license = "Apache License 2.0"
    ),
    LibraryLicense(
        name = "Jetpack Compose",
        author = "Google",
        license = "Apache License 2.0"
    ),
    LibraryLicense(
        name = "Room",
        author = "Google",
        license = "Apache License 2.0"
    ),
    LibraryLicense(
        name = "Hilt",
        author = "Google",
        license = "Apache License 2.0"
    ),
    LibraryLicense(
        name = "Kotlinx Coroutines",
        author = "JetBrains",
        license = "Apache License 2.0"
    ),
    LibraryLicense(
        name = "Kotlinx Serialization",
        author = "JetBrains",
        license = "Apache License 2.0"
    ),
    LibraryLicense(
        name = "Kotlinx DateTime",
        author = "JetBrains",
        license = "Apache License 2.0"
    ),
    LibraryLicense(
        name = "Vico",
        author = "Patryk Goworowski & Patrick Michalik",
        license = "Apache License 2.0"
    ),
    LibraryLicense(
        name = "Material Icons Extended",
        author = "Google",
        license = "Apache License 2.0"
    ),
    LibraryLicense(
        name = "DataStore",
        author = "Google",
        license = "Apache License 2.0"
    )
)
