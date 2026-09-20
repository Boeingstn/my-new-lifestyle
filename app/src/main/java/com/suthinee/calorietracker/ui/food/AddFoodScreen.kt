package com.suthinee.calorietracker.ui.food

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.AsyncImage
import com.suthinee.calorietracker.domain.model.MealType
import com.suthinee.calorietracker.domain.nlp.ParsedFoodDescription
import com.suthinee.calorietracker.domain.nlp.PortionOption
import com.suthinee.calorietracker.ui.common.LocalAppContainer
import com.suthinee.calorietracker.util.PhotoStorage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    initialMealType: MealType,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: AddFoodViewModel = viewModel(
        factory = viewModelFactory { initializer { AddFoodViewModel(container, initialMealType) } }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val form = state.form

    LaunchedEffect(form.saved) {
        if (form.saved) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Food") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MealTypeSelector(selected = form.mealType, onSelect = viewModel::setMealType)

            if (state.favorites.isNotEmpty()) {
                Text("Favorites", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.favorites) { favorite ->
                        AssistChip(
                            onClick = { viewModel.applyFavorite(favorite) },
                            label = { Text("${favorite.foodName} (${favorite.calories} kcal)") }
                        )
                    }
                }
            }

            val tabs = AddFoodTab.entries.toList()
            TabRow(selectedTabIndex = tabs.indexOf(form.tab)) {
                tabs.forEach { tab ->
                    Tab(
                        selected = form.tab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(tab.label()) }
                    )
                }
            }

            when (form.tab) {
                AddFoodTab.PHOTO -> PhotoTabContent(
                    photoPath = form.photoPath,
                    onPhotoTaken = viewModel::setPhotoPath
                )
                AddFoodTab.DESCRIBE -> DescribeTabContent(
                    describeText = form.describeText,
                    onTextChanged = viewModel::setDescribeText,
                    onEstimate = viewModel::parseDescription,
                    parsed = form.parsedDescription,
                    answeredOption = form.answeredPortionOption,
                    onAnswer = viewModel::answerPortionQuestion,
                    onSkip = viewModel::skipPortionQuestion
                )
                AddFoodTab.MANUAL -> Text(
                    "Enter the food details below.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            OutlinedTextField(
                value = form.foodName,
                onValueChange = viewModel::setFoodName,
                label = { Text("Food name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = form.portion,
                onValueChange = viewModel::setPortion,
                label = { Text("Portion") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = form.caloriesText,
                onValueChange = viewModel::setCalories,
                label = { Text("Calories (kcal)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = viewModel::save,
                enabled = form.canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
            OutlinedButton(
                onClick = viewModel::saveAsFavorite,
                enabled = form.canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save as favorite")
            }
        }
    }
}

private fun AddFoodTab.label(): String = when (this) {
    AddFoodTab.PHOTO -> "Photo"
    AddFoodTab.DESCRIBE -> "Describe"
    AddFoodTab.MANUAL -> "Manual"
}

@Composable
private fun MealTypeSelector(selected: MealType, onSelect: (MealType) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(MealType.entries.toList()) { mealType ->
            FilterChip(
                selected = mealType == selected,
                onClick = { onSelect(mealType) },
                label = { Text(mealType.displayName) }
            )
        }
    }
}

@Composable
private fun PhotoTabContent(
    photoPath: String?,
    onPhotoTaken: (String) -> Unit
) {
    val context = LocalContext.current
    var pendingFile by remember { mutableStateOf<File?>(null) }
    val onPhotoTakenState = rememberUpdatedState(onPhotoTaken)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingFile?.let { onPhotoTakenState.value(it.absolutePath) }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (photoPath != null) {
            AsyncImage(
                model = File(photoPath),
                contentDescription = "Meal photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
        OutlinedButton(
            onClick = {
                val file = PhotoStorage.createNewPhotoFile(context)
                pendingFile = file
                val uri: Uri = PhotoStorage.uriForFile(context, file)
                launcher.launch(uri)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Text("  Take Photo")
        }
    }
}

@Composable
private fun DescribeTabContent(
    describeText: String,
    onTextChanged: (String) -> Unit,
    onEstimate: () -> Unit,
    parsed: ParsedFoodDescription?,
    answeredOption: PortionOption?,
    onAnswer: (PortionOption) -> Unit,
    onSkip: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = describeText,
            onValueChange = onTextChanged,
            label = { Text("Describe what you ate") },
            placeholder = { Text("e.g. ข้าวกะเพราไก่ ไข่ดาว") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = onEstimate, enabled = describeText.isNotBlank()) {
            Text("Estimate calories")
        }
        if (parsed != null) {
            Text(
                if (parsed.matchedKnownDish) {
                    "Matched: ${parsed.suggestedName}"
                } else {
                    "Couldn't match a known dish — please confirm calories below."
                },
                style = MaterialTheme.typography.bodyMedium
            )
            parsed.followUpQuestion?.let { question ->
                Text(question.prompt, style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(question.options) { option ->
                        FilterChip(
                            selected = answeredOption == option,
                            onClick = {
                                if (option.multiplier == null) onSkip() else onAnswer(option)
                            },
                            label = { Text(option.label) }
                        )
                    }
                }
            }
        }
    }
}
