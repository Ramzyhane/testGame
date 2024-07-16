package com.example.testgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class Results extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // Get data from Intent extras
        Intent intent = getIntent();
        int correctAnswers = intent.getIntExtra("correct_answers", 0);
        int totalQuestions = intent.getIntExtra("total_questions", 0);

        // Display results
        TextView resultsTextView = findViewById(R.id.resultsTextView);
        resultsTextView.setText("Results:\nCorrect Answers: " + correctAnswers + "/" + totalQuestions);

        // Restart button click listener
        Button restartButton = findViewById(R.id.restartButton);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Restart the game by starting MainActivity
                Intent mainIntent = new Intent(Results.this, MainActivity.class);
                startActivity(mainIntent);
                finish(); // Close this activity to prevent going back to results screen on back press
            }
        });
    }
}

