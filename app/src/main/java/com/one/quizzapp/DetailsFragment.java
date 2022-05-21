package com.one.quizzapp;

import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class DetailsFragment extends Fragment implements View.OnClickListener {

    private QuizListViewModel quizListViewModel;
    private NavController navController;

    private ImageView detailsImage;
    private TextView detailsTitle;
    private TextView detailsDiff;
    private TextView detailsDesc;
    private TextView detailsQuestions;
    private Button detailsStartBtn;
    private int position;
    private String quizId;
    private long totalQuestions = 0;

    public DetailsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        detailsImage = view.findViewById(R.id.details_image);
        detailsTitle = view.findViewById(R.id.details_title);
        detailsDesc = view.findViewById(R.id.details_desc);
        detailsDiff = view.findViewById(R.id.details_difficulty_text);
        detailsQuestions = view.findViewById(R.id.details_questions_text);
        detailsStartBtn = view.findViewById(R.id.details_start_quiz_btn);

        position = DetailsFragmentArgs.fromBundle(getArguments()).getPosition();

        quizListViewModel = new ViewModelProvider(getActivity()).get(QuizListViewModel.class);
        quizListViewModel.getQuizListModelData().observe(getViewLifecycleOwner(), new Observer<List<QuizListModel>>() {
            @Override
            public void onChanged(List<QuizListModel> quizListModels) {
                detailsTitle.setText(quizListModels.get(position).getName());
                detailsDesc.setText(quizListModels.get(position).getDesc());
                detailsDiff.setText(quizListModels.get(position).getLevel());
                detailsQuestions.setText(String.valueOf(quizListModels.get(position).getQuestions()));

                quizId = quizListModels.get(position).getQuiz_id();
                totalQuestions = quizListModels.get(position).getQuestions();

                Glide.with(getContext())
                        .load(quizListModels.get(position).getImage())
                        .centerCrop()
                        .placeholder(R.drawable.image_placeholder)
                        .into(detailsImage);
            }
        });

        detailsStartBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        DetailsFragmentDirections.ActionDetailsFragmentToQuizFragment action = DetailsFragmentDirections.actionDetailsFragmentToQuizFragment();
        action.setQuizId(quizId);
        action.setTotalQuestions(totalQuestions);
        navController.navigate(action);
    }
}