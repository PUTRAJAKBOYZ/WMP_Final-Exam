package com.example.wmp_final;

import android.database.sqlite.SQLiteOpenHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "school.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Student (" +
                "student_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL);");

        db.execSQL("CREATE TABLE Subject (" +
                "subject_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "subject_name TEXT NOT NULL, " +
                "credits INTEGER NOT NULL);");

        db.execSQL("CREATE TABLE Enrollment (" +
                "enrollment_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "student_id INTEGER NOT NULL, " +
                "subject_id INTEGER NOT NULL, " +
                "FOREIGN KEY(student_id) REFERENCES Student(student_id), " +
                "FOREIGN KEY(subject_id) REFERENCES Subject(subject_id));");

        // Prepopulate subjects
        db.execSQL("INSERT INTO Subject (subject_name, credits) VALUES ('Mathematics', 3), ('Robotics', 3), ('Otomotive', 3)," +
                "('Informatics', 3), ('Sport', 3), ('Pharmacy', 3), ('Accounting', 3), ('Sing', 3), ('English', 3)," +
                "('Indonesian', 3), ('Web Programming', 3), ('WMP', 3);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Student");
        db.execSQL("DROP TABLE IF EXISTS Subject");
        db.execSQL("DROP TABLE IF EXISTS Enrollment");
        onCreate(db);
    }

    public boolean registerStudent(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("email", email);
        contentValues.put("password", password);

        long result = db.insert("Student", null, contentValues);
        return result != -1;
    }

    public boolean login(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Student WHERE email = ? AND password = ?", new String[]{email, password});
        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        return isValid;
    }

    public List<Subject> getSubjects() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Subject> subjects = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM Subject", null);

        if (cursor.moveToFirst()) {
            do {
                // Check if the columns exist by getting their indices
                int nameIndex = cursor.getColumnIndex("subject_name");
                int creditsIndex = cursor.getColumnIndex("credits");
                int columnIndex = cursor.getColumnIndex("subject_id");

                // Ensure the columns are present before accessing
                if (nameIndex >= 0 && creditsIndex >= 0) {
                    String name = cursor.getString(nameIndex);
                    int credits = cursor.getInt(creditsIndex);
                    int id = cursor.getInt(columnIndex);
                    subjects.add(new Subject(id, name, credits));
                } else {
                    // Handle the case where required columns are not found
                    Log.e("Database Error", "Required columns 'subject_name' or 'credits' not found.");
                    throw new IllegalStateException("Required columns not found in the query result.");
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        return subjects;
    }

    public List<Integer> getEnrolledSubjects(int studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Integer> enrolledSubjects = new ArrayList<>();

        // Query to get all subject_ids the student is already enrolled in
        Cursor cursor = db.rawQuery("SELECT subject_id FROM Enrollment WHERE student_id = ?", new String[]{String.valueOf(studentId)});
        int columnIndex = cursor.getColumnIndex("subject_id");

        if (cursor.moveToFirst()) {
            do {
                int subjectId = cursor.getInt(columnIndex);
                enrolledSubjects.add(subjectId);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return enrolledSubjects;
    }

    public List<Integer> getEnrolledSubjectIds(int studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Integer> subjectIds = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT subject_id FROM Enrollment WHERE student_id = ?", new String[]{String.valueOf(studentId)});
        int columnIndex = cursor.getColumnIndex("subject_id");

        if (cursor.moveToFirst()) {
            do {
                int subjectId = cursor.getInt(columnIndex);
                subjectIds.add(subjectId);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return subjectIds;
    }

    public Subject getSubjectById(int subjectId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Subject WHERE subject_id = ?", new String[]{String.valueOf(subjectId)});
        int nameIndex = cursor.getColumnIndex("subject_name");
        int creditsIndex = cursor.getColumnIndex("credits");
        int columnIndex = cursor.getColumnIndex("subject_id");

        if (cursor.moveToFirst()) {
            int id = cursor.getInt(columnIndex);
            String name = cursor.getString(nameIndex);
            int credits = cursor.getInt(creditsIndex);
            cursor.close();
            return new Subject(id, name, credits);
        }

        cursor.close();
        return null;
    }

    public int getStudentTotalCredits(int studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int totalCredits = 0;

        // Query to get the total credits the student is enrolled in
        Cursor cursor = db.rawQuery("SELECT SUM(credits) AS totalCredits FROM Enrollment " +
                        "JOIN Subject ON Enrollment.subject_id = Subject.subject_id WHERE student_id = ?",
                new String[]{String.valueOf(studentId)});
        int totalCreditsIndex = cursor.getColumnIndex("totalCredits");

        if (cursor.moveToFirst()) {
            totalCredits = cursor.getInt(totalCreditsIndex);
        }

        cursor.close();
        return totalCredits;
    }

    public boolean enrollSubjects(int studentId, List<Integer> subjectIds) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean isSuccess = true;

        for (int subjectId : subjectIds) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("student_id", studentId);
            contentValues.put("subject_id", subjectId);

            long result = db.insert("Enrollment", null, contentValues);
            if (result == -1) {
                isSuccess = false;
            }
        }

        return isSuccess;
    }

    public boolean cancelAllEnrollments(int studentId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Delete all enrollments for the student
        int rowsDeleted = db.delete("Enrollment", "student_id = ?", new String[]{String.valueOf(studentId)});

        return rowsDeleted > 0;
    }
}
