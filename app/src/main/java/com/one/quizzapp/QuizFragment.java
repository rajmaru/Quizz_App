package com.one.quizzapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

public class QuizFragment extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    private String quizId;
    private TextView quizTitle, questionNumber, questionText, questionFeedback;
    private List<QuestionsModel> allQuestionsList = new ArrayList<>();
    private long totalQuestionsToAnswer = 10;
    private List<QuestionsModel> questionsToAnswer = new ArrayList<>();
    private Button optionOneBtn, optionTwoBtn, optionThreeBtn, nextBtn;

    public QuizFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        quizTitle = view.findViewById(R.id.quiz_title);
        questionNumber = view.findViewById(R.id.quiz_question_number);
        questionText = view.findViewById(R.id.quiz_question);
        optionOneBtn = view.findViewById(R.id.quiz_option_one);
        optionTwoBtn = view.findViewById(R.id.quiz_option_two);
        optionThreeBtn = view.findViewById(R.id.quiz_option_three);
        nextBtn = view.findViewById(R.id.quiz_next_btn);
        questionFeedback = view.findViewById(R.id.quiz_question_feedback);

        firebaseFirestore = FirebaseFirestore.getInstance();

        quizId = QuizFragmentArgs.fromBundle(getArguments()).getQuizId();
        totalQuestionsToAnswer = QuizFragmentArgs.fromBundle(getArguments()).getTotalQuestions();

        firebaseFirestore.collection("QuizList")
                .document(quizId).collection("Questions").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            allQuestionsList = task.getResult().toObjects(QuestionsModel.class);
                            pickQuestions();
                            loadUI();
                        }else{
                            quizTitle.setText("Error Loading Data");
                        }
                    }
                });
    }

    private void loadUI() {
        quizTitle.setText("Quiz Data Loaded");
        questionNumber.setText("1");
        questionText.setText("Load First Question");

        enableOptions();
        loadQuestion(1);
    }

    private void loadQuestion(int i) {
        questionText.setText(questionsToAnswer.get(i).getQuestion());

        optionOneBtn.setText(questionsToAnswer.get(i).getOption_a());
        optionTwoBtn.setText(questionsToAnswer.get(i).getOption_b());
        optionThreeBtn.setText(questionsToAnswer.get(i).getOption_c());
    }

    private void enableOptions() {
        optionOneBtn.setVisibility(View.VISIBLE);
        optionTwoBtn.setVisibility(View.VISIBLE);
        optionThreeBtn.setVisibility(View.VISIBLE);

        optionOneBtn.setEnabled(true);
        optionTwoBtn.setEnabled(true);
        optionThreeBtn.setEnabled(true);

        questionFeedback.setVisibility(View.INVISIBLE);
        nextBtn.setVisibility(View.INVISIBLE);
        nextBtn.setEnabled(true);
    }

    private void pickQuestions() {
        for(int i = 0; i<=totalQuestionsToAnswer; i++){
            Log.d("TAG","allQuestionsList.size() = " + allQuestionsList.size());
            int randomNumber = getRandomInteger(allQuestionsList.size(),0);
            Log.d("TAG","randomNumber = " + randomNumber);
            questionsToAnswer.add(allQuestionsList.get(randomNumber));
        }
    }

    public static int getRandomInteger(int maximum, int minimum){
        return ((int)  (Math.random()*(maximum-minimum))) + minimum;
    }
}