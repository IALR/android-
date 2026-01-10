package com.example.robotcontrol.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.robotcontrol.R;

/**
 * Generic fragment for showing a single education topic.
 */
public class EducationTopicFragment extends Fragment {

    private static final String ARG_TITLE_RES_ID = "title_res_id";
    private static final String ARG_CONTENT_RES_ID = "content_res_id";

    public static EducationTopicFragment newInstance(int titleResId, int contentResId) {
        EducationTopicFragment fragment = new EducationTopicFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TITLE_RES_ID, titleResId);
        args.putInt(ARG_CONTENT_RES_ID, contentResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_education_topic, container, false);

        TextView tvTitle = view.findViewById(R.id.tvTopicTitle);
        TextView tvContent = view.findViewById(R.id.tvTopicContent);

        Bundle args = getArguments();
        int titleResId = args != null ? args.getInt(ARG_TITLE_RES_ID, 0) : 0;
        int contentResId = args != null ? args.getInt(ARG_CONTENT_RES_ID, 0) : 0;

        if (titleResId != 0) {
            tvTitle.setText(getString(titleResId));
        }
        if (contentResId != 0) {
            tvContent.setText(getString(contentResId));
        }

        return view;
    }
}
