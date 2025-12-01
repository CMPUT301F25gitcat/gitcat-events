package com.example.gitcat_events;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class EventFilterBottomSheet extends BottomSheetDialogFragment {

    public interface FilterCallback {
        void onFilterApplied(Calendar startDate, Calendar endDate, Set<String> selectedInterests);
        void onFiltersCleared();
    }

    private FilterCallback callback;
    private Calendar selectedStartDate;
    private Calendar selectedEndDate;
    private Set<String> selectedInterests;

    private TextView tvStartDateDisplay;
    private TextView tvEndDateDisplay;
    private TextView tvFilterError;
    private Button btnSelectStartDate;
    private Button btnSelectEndDate;
    private Button btnApplyFilter;
    private Button btnClearFilters;
    
    // Interest checkboxes
    private MaterialCheckBox checkboxFilterSports;
    private MaterialCheckBox checkboxFilterArts;
    private MaterialCheckBox checkboxFilterMusic;
    private MaterialCheckBox checkboxFilterEducation;
    private MaterialCheckBox checkboxFilterFamily;
    private MaterialCheckBox checkboxFilterOther;

    public static EventFilterBottomSheet newInstance(FilterCallback callback) {
        EventFilterBottomSheet fragment = new EventFilterBottomSheet();
        fragment.callback = callback;
        return fragment;
    }

    public void setFilterCallback(FilterCallback callback) {
        this.callback = callback;
    }

    public void setInitialDates(Calendar startDate, Calendar endDate) {
        this.selectedStartDate = startDate != null ? (Calendar) startDate.clone() : null;
        this.selectedEndDate = endDate != null ? (Calendar) endDate.clone() : null;
    }

    public void setInitialInterests(Set<String> interests) {
        this.selectedInterests = interests != null ? new HashSet<>(interests) : new HashSet<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_event_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        tvStartDateDisplay = view.findViewById(R.id.tvStartDateDisplay);
        tvEndDateDisplay = view.findViewById(R.id.tvEndDateDisplay);
        tvFilterError = view.findViewById(R.id.tvFilterError);
        btnSelectStartDate = view.findViewById(R.id.btnSelectStartDate);
        btnSelectEndDate = view.findViewById(R.id.btnSelectEndDate);
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter);
        btnClearFilters = view.findViewById(R.id.btnClearFilters);
        
        // Initialize interest checkboxes
        checkboxFilterSports = view.findViewById(R.id.checkboxFilterSports);
        checkboxFilterArts = view.findViewById(R.id.checkboxFilterArts);
        checkboxFilterMusic = view.findViewById(R.id.checkboxFilterMusic);
        checkboxFilterEducation = view.findViewById(R.id.checkboxFilterEducation);
        checkboxFilterFamily = view.findViewById(R.id.checkboxFilterFamily);
        checkboxFilterOther = view.findViewById(R.id.checkboxFilterOther);
        
        // Initialize selectedInterests if not set
        if (selectedInterests == null) {
            selectedInterests = new HashSet<>();
        }

        // Update displays with initial dates if set
        if (selectedStartDate != null) {
            updateStartDateDisplay(selectedStartDate);
        }
        if (selectedEndDate != null) {
            updateEndDateDisplay(selectedEndDate);
        }
        
        // Update checkbox states with initial interests
        updateInterestCheckboxStates();

        // Set up date picker for start date
        btnSelectStartDate.setOnClickListener(v -> selectStartDate());
        
        // Set up interest checkbox listeners
        checkboxFilterSports.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedInterests.add("Sports");
            } else {
                selectedInterests.remove("Sports");
            }
        });
        
        checkboxFilterArts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedInterests.add("Arts");
            } else {
                selectedInterests.remove("Arts");
            }
        });
        
        checkboxFilterMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedInterests.add("Music");
            } else {
                selectedInterests.remove("Music");
            }
        });
        
        checkboxFilterEducation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedInterests.add("Education");
            } else {
                selectedInterests.remove("Education");
            }
        });
        
        checkboxFilterFamily.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedInterests.add("Family");
            } else {
                selectedInterests.remove("Family");
            }
        });
        
        checkboxFilterOther.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedInterests.add("Other");
            } else {
                selectedInterests.remove("Other");
            }
        });

        // Set up date picker for end date
        btnSelectEndDate.setOnClickListener(v -> selectEndDate());

        // Set up apply filter button
        btnApplyFilter.setOnClickListener(v -> applyFilter());

        // Set up clear filters button
        btnClearFilters.setOnClickListener(v -> clearFilters());
    }

    private void selectStartDate() {
        Calendar calendar = selectedStartDate != null ? selectedStartDate : Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedStartDate = Calendar.getInstance();
                    selectedStartDate.set(year, month, dayOfMonth);
                    selectedStartDate.set(Calendar.HOUR_OF_DAY, 0);
                    selectedStartDate.set(Calendar.MINUTE, 0);
                    selectedStartDate.set(Calendar.SECOND, 0);
                    selectedStartDate.set(Calendar.MILLISECOND, 0);
                    updateStartDateDisplay(selectedStartDate);
                    // Clear error when date is selected
                    tvFilterError.setVisibility(View.GONE);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void selectEndDate() {
        Calendar calendar = selectedEndDate != null ? selectedEndDate : Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedEndDate = Calendar.getInstance();
                    selectedEndDate.set(year, month, dayOfMonth);
                    selectedEndDate.set(Calendar.HOUR_OF_DAY, 23);
                    selectedEndDate.set(Calendar.MINUTE, 59);
                    selectedEndDate.set(Calendar.SECOND, 59);
                    selectedEndDate.set(Calendar.MILLISECOND, 999);
                    updateEndDateDisplay(selectedEndDate);
                    // Clear error when date is selected
                    tvFilterError.setVisibility(View.GONE);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateStartDateDisplay(Calendar date) {
        if (date != null) {
            String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                    date.get(Calendar.DAY_OF_MONTH),
                    date.get(Calendar.MONTH) + 1,
                    date.get(Calendar.YEAR));
            tvStartDateDisplay.setText(formattedDate);
        } else {
            tvStartDateDisplay.setText("Not selected");
        }
    }

    private void updateEndDateDisplay(Calendar date) {
        if (date != null) {
            String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                    date.get(Calendar.DAY_OF_MONTH),
                    date.get(Calendar.MONTH) + 1,
                    date.get(Calendar.YEAR));
            tvEndDateDisplay.setText(formattedDate);
        } else {
            tvEndDateDisplay.setText("Not selected");
        }
    }

    private void applyFilter() {
        // Validate dates
        if (selectedStartDate != null && selectedEndDate != null) {
            // Normalize to start of day for comparison
            Calendar startNormalized = (Calendar) selectedStartDate.clone();
            startNormalized.set(Calendar.HOUR_OF_DAY, 0);
            startNormalized.set(Calendar.MINUTE, 0);
            startNormalized.set(Calendar.SECOND, 0);
            startNormalized.set(Calendar.MILLISECOND, 0);

            Calendar endNormalized = (Calendar) selectedEndDate.clone();
            endNormalized.set(Calendar.HOUR_OF_DAY, 0);
            endNormalized.set(Calendar.MINUTE, 0);
            endNormalized.set(Calendar.SECOND, 0);
            endNormalized.set(Calendar.MILLISECOND, 0);

            if (startNormalized.after(endNormalized)) {
                tvFilterError.setText("Start date must be before or equal to end date");
                tvFilterError.setVisibility(View.VISIBLE);
                return;
            }
        }

        // Clear any error
        tvFilterError.setVisibility(View.GONE);

        // Apply filter via callback
        if (callback != null) {
            callback.onFilterApplied(selectedStartDate, selectedEndDate, selectedInterests);
        }

        // Dismiss the bottom sheet
        dismiss();
    }
    
    private void updateInterestCheckboxStates() {
        checkboxFilterSports.setChecked(selectedInterests.contains("Sports"));
        checkboxFilterArts.setChecked(selectedInterests.contains("Arts"));
        checkboxFilterMusic.setChecked(selectedInterests.contains("Music"));
        checkboxFilterEducation.setChecked(selectedInterests.contains("Education"));
        checkboxFilterFamily.setChecked(selectedInterests.contains("Family"));
        checkboxFilterOther.setChecked(selectedInterests.contains("Other"));
    }

    private void clearFilters() {
        selectedStartDate = null;
        selectedEndDate = null;
        selectedInterests.clear();
        updateStartDateDisplay(null);
        updateEndDateDisplay(null);
        updateInterestCheckboxStates();
        tvFilterError.setVisibility(View.GONE);

        // Notify callback
        if (callback != null) {
            callback.onFiltersCleared();
        }

        // Dismiss the bottom sheet
        dismiss();
    }
}

