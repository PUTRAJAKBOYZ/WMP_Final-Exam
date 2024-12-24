package com.example.wmp_final;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private int studentId; // Pass this from LoginActivity
    private final int maxCredits = 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment);

        db = new DatabaseHelper(this);
        studentId = getIntent().getIntExtra("STUDENT_ID", -1);

        LinearLayout subjectsLayout = findViewById(R.id.subjectsLayout);
        Button enrollButton = findViewById(R.id.btnEnroll);
        Button enrollSummaryButton = findViewById(R.id.btnEnrollSummary);

        List<Subject> subjects = db.getSubjects();
        List<CheckBox> checkBoxes = new ArrayList<>();

        for (Subject subject : subjects) {
            CheckBox checkBox = new CheckBox(this);
            String text = subject.getName() + " (" + subject.getCredits() + " credits)";
            checkBox.setText(text);
            checkBox.setTag(subject);
            subjectsLayout.addView(checkBox);
            checkBoxes.add(checkBox);
        }

        enrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Integer> selectedSubjects = new ArrayList<>();
                int totalCredits = 0;
                List<Integer> alreadyEnrolledSubjects = db.getEnrolledSubjects(studentId);  // Get already enrolled subjects

                // Get the student's current total credits
                int currentCredits = db.getStudentTotalCredits(studentId);

                // Loop through selected subjects
                for (CheckBox checkBox : checkBoxes) {
                    if (checkBox.isChecked()) {
                        Subject subject = (Subject) checkBox.getTag();

                        // Check if the student is already enrolled in this subject
                        if (alreadyEnrolledSubjects.contains(subject.getId())) {
                            Toast.makeText(EnrollmentActivity.this, "You are already enrolled in " + subject.getName(), Toast.LENGTH_SHORT).show();
                            continue;  // Skip enrolling this subject
                        }

                        totalCredits += subject.getCredits();
                        selectedSubjects.add(subject.getId());
                    }
                }

                // Calculate the total credits after enrolling new subjects
                if (currentCredits + totalCredits > maxCredits) {
                    Toast.makeText(EnrollmentActivity.this, "Credit limit exceeded!", Toast.LENGTH_SHORT).show();
                } else if (!selectedSubjects.isEmpty()) {
                    // Enroll the selected subjects
                    boolean success = db.enrollSubjects(studentId, selectedSubjects);
                    if (success) {
                        Toast.makeText(EnrollmentActivity.this, "Enrollment Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(EnrollmentActivity.this, EnrollmentSummaryActivity.class);
                        intent.putExtra("student_id", studentId);
                        startActivity(intent);
                    } else {
                        Toast.makeText(EnrollmentActivity.this, "Enrollment Failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle case where no subjects are selected
                    Toast.makeText(EnrollmentActivity.this, "No subjects selected for enrollment.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        enrollSummaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EnrollmentActivity.this, EnrollmentSummaryActivity.class);
                intent.putExtra("student_id", studentId);
                startActivity(intent);
            }
        });
    }
}
