package de.kitshn.android.ui.route.main.subroute.mealplan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.kitshn.android.R
import de.kitshn.android.api.tandoor.model.TandoorMealPlan
import de.kitshn.android.parseTandoorDate
import de.kitshn.android.toHumanReadableDateLabel
import de.kitshn.android.toLocalDate
import de.kitshn.android.ui.component.LoadingGradientWrapper
import de.kitshn.android.ui.component.model.mealplan.MealPlanDayCard
import de.kitshn.android.ui.dialog.mealplan.MealPlanCreationAndEditDefaultValues
import de.kitshn.android.ui.dialog.mealplan.MealPlanCreationDialogState
import de.kitshn.android.ui.dialog.mealplan.MealPlanDetailsDialogState
import de.kitshn.android.ui.selectionMode.SelectionModeState
import de.kitshn.android.ui.state.ErrorLoadingSuccessState
import de.kitshn.android.ui.theme.Typography
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMainSubrouteMealplanScaffoldContent(
    pv: PaddingValues,
    scrollBehavior: TopAppBarScrollBehavior,

    startDate: LocalDate,
    endDate: LocalDate,

    list: List<TandoorMealPlan>,

    pageLoadingState: ErrorLoadingSuccessState,
    selectionModeState: SelectionModeState<Int>,

    detailsDialogState: MealPlanDetailsDialogState,
    creationDialogState: MealPlanCreationDialogState,

    onChangeMealPlanStartDate: (day: LocalDate) -> Unit
) {
    var showDatePickerDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if(showDatePickerDialog) DatePickerDialog(
        onDismissRequest = { showDatePickerDialog = false },
        confirmButton = {
            Button(
                onClick = {
                    if(datePickerState.selectedDateMillis == null) return@Button

                    showDatePickerDialog = false
                    onChangeMealPlanStartDate(datePickerState.selectedDateMillis!!.toLocalDate())
                }
            ) {
                Text(stringResource(id = R.string.common_okay))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }

    LoadingGradientWrapper(
        Modifier.padding(pv),
        loadingState = pageLoadingState
    ) {
        LazyVerticalStaggeredGrid(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            columns = StaggeredGridCells.Adaptive(250.dp),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item(
                span = StaggeredGridItemSpan.FullLine
            ) {
                Row(
                    Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SmallFloatingActionButton(
                        onClick = {
                            onChangeMealPlanStartDate(startDate.minusDays(7))
                        },
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 0.dp
                        )
                    ) {
                        Icon(Icons.Rounded.Remove, stringResource(R.string.action_minus_one_week))
                    }

                    Spacer(Modifier.width(4.dp))

                    AssistChip(
                        onClick = {
                            showDatePickerDialog = true
                        },
                        label = {
                            Text(
                                modifier = Modifier.padding(12.dp),
                                style = Typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                text = "${startDate.toHumanReadableDateLabel()} — ${endDate.toHumanReadableDateLabel()}"
                            )
                        }
                    )

                    Spacer(Modifier.width(4.dp))

                    SmallFloatingActionButton(
                        onClick = {
                            onChangeMealPlanStartDate(startDate.plusDays(7))
                        },
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 0.dp
                        )
                    ) {
                        Icon(Icons.Rounded.Add, stringResource(R.string.action_plus_one_week))
                    }
                }
            }

            items(7) { index ->
                val day = startDate.plusDays(index.toLong())

                Column {
                    MealPlanDayCard(
                        day = day,
                        mealPlanItems = list.toMutableList().filter { mealPlan ->
                            mealPlan.from_date.parseTandoorDate()
                                .isEqual(day) || mealPlan.to_date.parseTandoorDate().isEqual(day)
                        }.sortedBy { mealPlan ->
                            mealPlan.meal_type.order
                        },

                        loadingState = pageLoadingState,
                        selectionState = selectionModeState,

                        onClick = { mealPlan ->
                            detailsDialogState.open(
                                linkContent = mealPlan
                            )
                        }
                    ) {
                        creationDialogState.open(
                            MealPlanCreationAndEditDefaultValues(
                                startDate = day
                            )
                        )
                    }
                }
            }
        }
    }
}