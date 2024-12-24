package com.example.wmp_final;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

public class EnrollmentSummaryAdapter extends BaseAdapter {

    private Context context;
    private List<Subject> subjects;

    public EnrollmentSummaryAdapter(Context context, List<Subject> subjects) {
        this.context = context;
        this.subjects = subjects;
    }

    @Override
    public int getCount() {
        return subjects.size();
    }

    @Override
    public Object getItem(int position) {
        return subjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_subject, parent, false);
        }

        Subject subject = subjects.get(position);

        TextView subjectNameTextView = convertView.findViewById(R.id.subjectNameTextView);
        TextView creditsTextView = convertView.findViewById(R.id.creditsTextView);

        subjectNameTextView.setText(subject.getName());
        creditsTextView.setText("Credits: " + subject.getCredits());

        return convertView;
    }
}
