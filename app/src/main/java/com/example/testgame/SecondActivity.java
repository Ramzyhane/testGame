package com.example.testgame;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SecondActivity extends AppCompatActivity {

    private static final String TAG = "SecondActivity";
    private TextView questionTextView; // משתנה לאחסון הפניה ל-TextView שמציג את השאלה הנוכחית
    private TextView questionCounterTextView;  // משתנה לאחסון הפניה ל-TextView שמציג את מונה השאלות הנוכחי
    private Button[] answerButtons;// מערך לאחסון הפניות לכפתורי התשובות בממשק המשתמש

    private List<String> answers;// רשימה לאחסון כל התשובות כולל התשובה הנכונה
    private String correctAnswer;// משתנה לאחסון התשובה הנכונה של השאלה הנוכחית
    private int currentQuestionNumber = 0;// משתנה לאחסון מונה השאלות הנוכחי
    private int correctAnswersCount = 0; // משתנה לאחסון מספר התשובות הנכונות שהמשתמש ענה

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secondactivity);

        // אתחול ה-views
        questionTextView = findViewById(R.id.questionTextView);
        questionCounterTextView = findViewById(R.id.questionCounterTextView);
        answerButtons = new Button[]{
                findViewById(R.id.answerButton1),
                findViewById(R.id.answerButton2),
                findViewById(R.id.answerButton3),
                findViewById(R.id.answerButton4)
        };

        // קריאה ל-API כדי לשלוף שאלות
        new FetchQuestionsTask().execute("https://opentdb.com/api.php?amount=1&category=18&type=multiple"); // Change type as needed
    }

    private class FetchQuestionsTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            if (urls.length == 0) {
                return null;
            }

            String apiUrl = urls[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String questionsJsonString = null;

            try {
                URL url = new URL(apiUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // קריאה לזרם הקלט לתוך מחרוזת
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder builder = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }

                if (builder.length() == 0) {
                    return null;
                }
                questionsJsonString = builder.toString();
            } catch (IOException e) {
                Log.e(TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
            return questionsJsonString;
        }

        @Override
        protected void onPostExecute(String questionsJsonString) {
            if (questionsJsonString != null) {
                try {
                    JSONObject response = new JSONObject(questionsJsonString);
                    JSONArray results = response.getJSONArray("results");
                    JSONObject questionObj = results.getJSONObject(0); // קח את השאלה הראשונה
                    String question = questionObj.getString("question");
                    correctAnswer = questionObj.getString("correct_answer");

                    JSONArray incorrectAnswers = questionObj.getJSONArray("incorrect_answers");
                    answers = new ArrayList<>();
                    answers.add(correctAnswer);  // הוסף תשובה נכונה קודם
                    for (int i = 0; i < incorrectAnswers.length(); i++) {
                        answers.add(incorrectAnswers.getString(i));
                    }

                    // ערבב תשובות לשיפור האקראיות
                    Collections.shuffle(answers);

                    // הגדל את מונה השאלות
                    currentQuestionNumber++;
                    questionCounterTextView.setText("Question " + currentQuestionNumber + " of 5");

                    // הצגת השאלה והתשובות בממשק המשתמש
                    questionTextView.setText(question);
                    for (int i = 0; i < answerButtons.length; i++) {
                        answerButtons[i].setText(answers.get(i));
                        answerButtons[i].setVisibility(View.VISIBLE);
                    }

                    // קביעת מאזיני לחיצה לכפתורי התשובות
                    for (Button button : answerButtons) {
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                checkAnswer((Button) v);
                            }
                        });
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing JSON", e);
                }
            } else {
                Log.e(TAG, "No response from API");
            }
        }
    }

    private void checkAnswer(Button selectedButton) {
        String selectedAnswer = selectedButton.getText().toString();
        if (selectedAnswer.equals(correctAnswer)) {
            // לוגיקה לתשובה נכונה
            Log.d(TAG, "Correct Answer!");
            correctAnswersCount++;  // Increment correct answers count
        } else {
            // לוגיקה לתשובה לא נכונה
            Log.d(TAG, "Incorrect Answer!");
        }

        // לאחר מתן תשובה, בדוק אם זו השאלה האחרונה
        if (currentQuestionNumber < 5) {
            // שלוף שאלה הבאה
            new FetchQuestionsTask().execute("https://opentdb.com/api.php?amount=1&category=18&type=multiple"); // Change type as needed
        } else {
            // הצגת דף התוצאות
            Intent intent = new Intent(SecondActivity.this, Results.class);
            intent.putExtra("correct_answers", correctAnswersCount);
            intent.putExtra("total_questions", currentQuestionNumber);
            startActivity(intent);
            finish(); // סיים את הפעילות הנוכחית אם דף התוצאות לא צריך להיות עם אפשרות חזרה אחורה
        }
    }

}
