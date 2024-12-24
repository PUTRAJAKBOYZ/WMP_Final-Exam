package com.example.wmp_final;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentSummaryActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment_summary);

        // Initialize views
        ListView subjectsListView = findViewById(R.id.subjectsListView);
        TextView totalCreditsTextView = findViewById(R.id.totalCreditsTextView);

        // Initialize the DatabaseHelper
        dbHelper = new DatabaseHelper(this);
        studentId = getIntent().getIntExtra("student_id", -1);

        // Get the studentId from the intent
        Intent intent = getIntent();
        int studentId = intent.getIntExtra("student_id", -1);

        // Get the subjects the student is enrolled in
        List<Subject> enrolledSubjects = getEnrolledSubjects(studentId);

        // Display the subjects in a list and calculate total credits
        EnrollmentSummaryAdapter adapter = new EnrollmentSummaryAdapter(this, enrolledSubjects);
        subjectsListView.setAdapter(adapter);

        // Calculate the total credits
        int totalCredits = calculateTotalCredits(enrolledSubjects);
        String text = "Total Credits: " + totalCredits;
        totalCreditsTextView.setText(text);

        Button cancelAllButton = findViewById(R.id.btnCancelAllEnrollments);
        cancelAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAllEnrollments();
            }
        });
    }

    // Method to get the list of enrolled subjects for the student
    private List<Subject> getEnrolledSubjects(int studentId) {
        List<Subject> enrolledSubjects = new ArrayList<>();
        List<Integer> subjectIds = dbHelper.getEnrolledSubjectIds(studentId);

        for (int subjectId : subjectIds) {
            Subject subject = dbHelper.getSubjectById(subjectId);
            if (subject != null) {
                enrolledSubjects.add(subject);
            }
        }
        return enrolledSubjects;
    }

    // Method to calculate total credits for the enrolled subjects
    private int calculateTotalCredits(List<Subject> enrolledSubjects) {
        int totalCredits = 0;
        for (Subject subject : enrolledSubjects) {
            totalCredits += subject.getCredits();
        }
        return totalCredits;
    }

    private void cancelAllEnrollments() {
        boolean isCancelled = dbHelper.cancelAllEnrollments(studentId);

        if (isCancelled) {
            Toast.makeText(this, "All enrollments have been cancelled", Toast.LENGTH_SHORT).show();
            // Optionally, navigate back to the EnrollmentActivity or a different screen
            finish();  // Close the current activity to go back
        } else {
            Toast.makeText(this, "Failed to cancel enrollments", Toast.LENGTH_SHORT).show();
        }
    }
}
