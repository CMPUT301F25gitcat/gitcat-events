package com.example.gitcat_events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventTypeSelectionBottomSheet extends BottomSheetDialogFragment {

    public interface TypeSelectionCallback {
        void onTypesSelected(List<String> selectedTypes);
    }

    private TypeSelectionCallback callback;
    private Set<String> selectedTypes;
    private MaterialCheckBox checkboxSports;
    private MaterialCheckBox checkboxArts;
    private MaterialCheckBox checkboxMusic;
    private MaterialCheckBox checkboxEducation;
    private MaterialCheckBox checkboxFamily;
    private MaterialCheckBox checkboxOther;
    private TextView tvSelectedTypes;
    private Button btnDone;
    private Button btnCancel;

    public static EventTypeSelectionBottomSheet newInstance(TypeSelectionCallback callback) {
        EventTypeSelectionBottomSheet fragment = new EventTypeSelectionBottomSheet();
        fragment.callback = callback;
        return fragment;
    }

    public void setTypeSelectionCallback(TypeSelectionCallback callback) {
        this.callback = callback;
    }

    public void setInitialTypes(List<String> initialTypes) {
        this.selectedTypes = initialTypes != null ? new HashSet<>(initialTypes) : new HashSet<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_event_type_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        checkboxSports = view.findViewById(R.id.checkboxSports);
        checkboxArts = view.findViewById(R.id.checkboxArts);
        checkboxMusic = view.findViewById(R.id.checkboxMusic);
        checkboxEducation = view.findViewById(R.id.checkboxEducation);
        checkboxFamily = view.findViewById(R.id.checkboxFamily);
        checkboxOther = view.findViewById(R.id.checkboxOther);
        tvSelectedTypes = view.findViewById(R.id.tvSelectedTypes);
        btnDone = view.findViewById(R.id.btnDone);
        btnCancel = view.findViewById(R.id.btnCancel);

        // Initialize selectedTypes if not set
        if (selectedTypes == null) {
            selectedTypes = new HashSet<>();
        }

        // Set initial checkbox states
        updateCheckboxStates();

        // Set up checkbox listeners
        checkboxSports.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedTypes.add("Sports");
            } else {
                selectedTypes.remove("Sports");
            }
            updateSelectedTypesDisplay();
        });

        checkboxArts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedTypes.add("Arts");
            } else {
                selectedTypes.remove("Arts");
            }
            updateSelectedTypesDisplay();
        });

        checkboxMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedTypes.add("Music");
            } else {
                selectedTypes.remove("Music");
            }
            updateSelectedTypesDisplay();
        });

        checkboxEducation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedTypes.add("Education");
            } else {
                selectedTypes.remove("Education");
            }
            updateSelectedTypesDisplay();
        });

        checkboxFamily.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedTypes.add("Family");
            } else {
                selectedTypes.remove("Family");
            }
            updateSelectedTypesDisplay();
        });

        checkboxOther.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedTypes.add("Other");
            } else {
                selectedTypes.remove("Other");
            }
            updateSelectedTypesDisplay();
        });

        // Set up button listeners
        btnDone.setOnClickListener(v -> confirmSelection());
        btnCancel.setOnClickListener(v -> dismiss());

        // Update display initially
        updateSelectedTypesDisplay();
    }

    private void updateCheckboxStates() {
        checkboxSports.setChecked(selectedTypes.contains("Sports"));
        checkboxArts.setChecked(selectedTypes.contains("Arts"));
        checkboxMusic.setChecked(selectedTypes.contains("Music"));
        checkboxEducation.setChecked(selectedTypes.contains("Education"));
        checkboxFamily.setChecked(selectedTypes.contains("Family"));
        checkboxOther.setChecked(selectedTypes.contains("Other"));
    }

    private void updateSelectedTypesDisplay() {
        if (selectedTypes.isEmpty()) {
            tvSelectedTypes.setText("No types selected");
        } else {
            String typesText = String.join(", ", selectedTypes);
            tvSelectedTypes.setText("Selected: " + typesText);
        }
    }

    private void confirmSelection() {
        if (callback != null) {
            List<String> typesList = new ArrayList<>(selectedTypes);
            callback.onTypesSelected(typesList);
        }
        dismiss();
    }
}

