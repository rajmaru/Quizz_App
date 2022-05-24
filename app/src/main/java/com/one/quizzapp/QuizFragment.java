package com.one.quizzapp;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class QuizFragment extends Fragment implements View.OnClickListener {

    private NavController navController;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String quizId;
    private String quizName;
    private TextView quizTitle, questionNumber, questionText, questionFeedback, questionTime;
    private List<QuestionsModel> allQuestionsList = new ArrayList<>();
    private long totalQuestionsToAnswer = 10;
    private List<QuestionsModel> questionsToAnswer = new ArrayList<>();
    private Button optionOneBtn, optionTwoBtn, optionThreeBtn, nextBtn;
    private CountDownTimer countDownTimer;
    private ProgressBar questionProgress;
    private boolean canAnswer = false;
    private int currentQuestion = 0;
    private int correctAnswers = 0;
    private int wrongAnswers = 0;
    private int notAnswered = 0;
    private String currentUserId;

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

        navController = Navigation.findNavController(view);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            currentUserId = firebaseAuth.getCurrentUser().getUid();
        } else {

        }

        firebaseFirestore = FirebaseFirestore.getInstance();

        quizTitle = view.findViewById(R.id.quiz_title);
        questionNumber = view.findViewById(R.id.quiz_question_number);
        questionText = view.findViewById(R.id.quiz_question);
        optionOneBtn = view.findViewById(R.id.quiz_option_one);
        optionTwoBtn = view.findViewById(R.id.quiz_option_two);
        optionThreeBtn = view.findViewById(R.id.quiz_option_three);
        nextBtn = view.findViewById(R.id.quiz_next_btn);
        questionFeedback = view.findViewById(R.id.quiz_question_feedback);
        questionTime = view.findViewById(R.id.quiz_question_time);
        questionProgress = view.findViewById(R.id.quiz_question_progress);

        quizId = QuizFragmentArgs.fromBundle(getArguments()).getQuizId();
        quizName = QuizFragmentArgs.fromBundle(getArguments()).getQuizName();
        totalQuestionsToAnswer = QuizFragmentArgs.fromBundle(getArguments()).getTotalQuestions();

        firebaseFirestore.collection("QuizList")
                .document(quizId).collection("Questions").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            allQuestionsList = task.getResult().toObjects(QuestionsModel.class);
                            pickQuestions();
                            loadUI();
                        } else {
                            quizTitle.setText("Error Loading Data");
                        }
                    }
                });

        optionOneBtn.setOnClickListener(this);
        optionTwoBtn.setOnClickListener(this);
        optionThreeBtn.setOnClickListener(this);

        nextBtn.setOnClickListener(this);
    }

    private void loadUI() {
        quizTitle.setText(quizName);
        questionText.setText("Load First Question");

        enableOptions();
        loadQuestion(1);
    }

    private void loadQuestion(int questionNum) {
        questionNumber.setText(questionNum + "");

        questionText.setText(questionsToAnswer.get(questionNum - 1).getQuestion());

        optionOneBtn.setText(questionsToAnswer.get(questionNum - 1).getOption_a());
        optionTwoBtn.setText(questionsToAnswer.get(questionNum - 1).getOption_b());
        optionThreeBtn.setText(questionsToAnswer.get(questionNum - 1).getOption_c());

        canAnswer = true;
        currentQuestion = questionNum;

        startTimer(questionNum);
    }

    private void startTimer(int questionNumber) {
        Long timeToAnswer = questionsToAnswer.get(questionNumber - 1).getTimer();
        questionTime.setText(timeToAnswer.toString());

        questionProgress.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(timeToAnswer * 1000, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                questionTime.setText(millisUntilFinished / 1000 + "");
                Long percent = millisUntilFinished / (timeToAnswer * 10);
                questionProgress.setProgress(percent.intValue());
            }

            @SuppressLint("ResourceAsColor")
            @Override
            public void onFinish() {
                canAnswer = false;
                questionFeedback.setText("Time Up! No answer was submitted.");
                questionFeedback.setTextColor(R.color.colorPrimary);
                notAnswered++;
                showNextBtn();
            }
        };

        countDownTimer.start();
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
        nextBtn.setEnabled(false);
    }

    private void pickQuestions() {
        for (int i = 0; i <= totalQuestionsToAnswer; i++) {
            Log.d("TAG", "allQuestionsList.size() = " + allQuestionsList.size());
            int randomNumber = getRandomInteger(allQuestionsList.size(), 0);
            Log.d("TAG", "randomNumber = " + randomNumber);
            questionsToAnswer.add(allQuestionsList.get(randomNumber));
        }
    }

    public static int getRandomInteger(int maximum, int minimum) {
        return ((int) (Math.random() * (maximum - minimum))) + minimum;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.quiz_option_one) {
            verifyAnswer(optionOneBtn);
        } else if (view.getId() == R.id.quiz_option_two) {
            verifyAnswer(optionTwoBtn);
        } else if (view.getId() == R.id.quiz_option_three) {
            verifyAnswer(optionThreeBtn);
        } else if (view.getId() == R.id.quiz_next_btn) {
            if (currentQuestion == totalQuestionsToAnswer) {
                submitResults();
            } else {
                currentQuestion++;
                loadQuestion(currentQuestion);
                resetOptions();
            }
        }
    }

    private void submitResults() {
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("correct", correctAnswers);
        resultMap.put("wrong", wrongAnswers);
        resultMap.put("unanswered", notAnswered);

        firebaseFirestore.collection("QuizList")
                .document(quizId)
                .collection("Results")
                .document(currentUserId)
                .set(resultMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            QuizFragmentDirections.ActionQuizFragmentToResultFragment action = QuizFragmentDirections.actionQuizFragmentToResultFragment();
                            action.setQuizId(quizId);
                            navController.navigate(action);
                        } else {
                            quizTitle.setText(task.getException().getMessage());
                        }
                    }
                });
    }

    private void resetOptions() {
        optionOneBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.outline_light_btn_bg, null));
        optionTwoBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.outline_light_btn_bg, null));
        optionThreeBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.outline_light_btn_bg, null));

        optionOneBtn.setTextColor(getResources().getColor(R.color.colorLightText, null));
        optionTwoBtn.setTextColor(getResources().getColor(R.color.colorLightText, null));
        optionThreeBtn.setTextColor(getResources().getColor(R.color.colorLightText, null));

        questionFeedback.setVisibility(View.INVISIBLE);
        nextBtn.setVisibility(View.INVISIBLE);
        nextBtn.setEnabled(false);
    }

    private void verifyAnswer(Button selectedAnswerBtn) {
        if (canAnswer) {
            selectedAnswerBtn.setTextColor(getResources().getColor(R.color.colorDark, null));
            if (questionsToAnswer.get(currentQuestion - 1).getAnswer().contentEquals(selectedAnswerBtn.getText())) {
                correctAnswers++;
                selectedAnswerBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.correct_answer_btn_bg, null));

                questionFeedback.setText("Correct Answer");
                questionFeedback.setTextColor(getResources().getColor(R.color.colorPrimary, null));
            } else {
                wrongAnswers++;
                selectedAnswerBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.wrong_answer_btn_bg, null));

                questionFeedback.setText("Wrong Answer \n \n Correct Answer : " + questionsToAnswer.get(currentQuestion - 1).getAnswer());
                questionFeedback.setTextColor(getResources().getColor(R.color.colorAccent, null));
            }
        }
        canAnswer = false;
        countDownTimer.cancel();

        showNextBtn();
    }

    private void showNextBtn() {
        if (currentQuestion == totalQuestionsToAnswer) {
            nextBtn.setText("Submit Results");
        }
        questionFeedback.setVisibility(View.VISIBLE);
        nextBtn.setVisibility(View.VISIBLE);
        nextBtn.setEnabled(true);
    }
}