package com.example.rosetta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.speech.ModelDownloadListener;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner fromLang, toLang;
    private EditText text;
    private Button translate,speak;
    private TextView result;
    private TextToSpeech textToSpeech;

    String[] fromLanguages = {
            "From", "English","Hindi","Telugu","Japanese","Korean"
    };

    String[] toLanguages = {
            "To", "English","Hindi","Telugu","Japanese","Korean"
    };

    private static final int REQUEST_CODE = 1;

    String langCode,fromLanguageCode,toLanguageCode,from,to = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromLang = findViewById(R.id.fromLang);
        toLang = findViewById(R.id.toLang);
        text = findViewById(R.id.text);
        translate = findViewById(R.id.translate);
        speak = findViewById(R.id.speak);
        result = findViewById(R.id.result);


        fromLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLanguageCode = getLanguageCode(fromLanguages[position]);
                from = fromLanguages[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                from = "";
            }
        });

        ArrayAdapter fromAdapter = new ArrayAdapter(this,
                com.google.android.material.R.layout.support_simple_spinner_dropdown_item, fromLanguages){
            @Override
            public boolean isEnabled(int position) {
                if(fromLanguages[position].equals(to)){
                    return false;
                }
                return true;
            }
        };
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromLang.setAdapter(fromAdapter);

        toLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLanguageCode = getLanguageCode(toLanguages[position]);
                to = toLanguages[position];
                textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
//                    "English","Hindi","Telugu","Japanese"
                            speak.setEnabled(true);
                            if(to.equals("English"))  textToSpeech.setLanguage(Locale.US);
                            else if(to.equals("Hindi")){
                                Toast.makeText(MainActivity.this,"Hindi speech is unavailable",Toast.LENGTH_SHORT).show();
                                speak.setEnabled(false);
                            }
                            else if(to.equals("Telugu")){
                                Toast.makeText(MainActivity.this, "Telugu speech is unavailable", Toast.LENGTH_SHORT).show();
                                speak.setEnabled(false);
                            }
                            else if(to.equals("Japanese")){
                                Toast.makeText(MainActivity.this, "Japanese speech is unavailable", Toast.LENGTH_SHORT).show();
                                speak.setEnabled(false);
                            }
                            else if (to.equals("Korean")) textToSpeech.setLanguage(Locale.KOREA);
//                            Toast.makeText(MainActivity.this, "textttospeech", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                to="";
            }
        });

        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeech.speak(text.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        ArrayAdapter toAdapter = new ArrayAdapter(this,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, toLanguages){
            @Override
            public boolean isEnabled(int position) {
                if(toLanguages[position].equals(from)){
                    return false;
                }
                return true;
            }
        };
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toLang.setAdapter(toAdapter);

        translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result.setText("");
                if(text.getText().toString().equals(""))
                    Toast.makeText(MainActivity.this, "Please enter your text", Toast.LENGTH_SHORT).show();
                else if(fromLanguageCode.isEmpty())
                    Toast.makeText(MainActivity.this, "Please Select Source Language", Toast.LENGTH_SHORT).show();
                else if(toLanguageCode.isEmpty())
                    Toast.makeText(MainActivity.this, "Please Select Target Language", Toast.LENGTH_SHORT).show();
                else {
//                    Toast.makeText(MainActivity.this, from + " " + to, Toast.LENGTH_SHORT).show();
                    translateText(fromLanguageCode, toLanguageCode, text.getText().toString());
                }
            }
        });

        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeech.speak(result.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
            }
        });

    }

    @Override
    protected void onPause() {
        if(!textToSpeech.isSpeaking()) super.onPause();
    }

    private void translateText(String fromLanguageCode, String toLanguageCode, String src) {
        result.setText("Processing..");
        try{
            TranslatorOptions opt = new TranslatorOptions.Builder()
                    .setSourceLanguage(fromLanguageCode)
                    .setTargetLanguage(toLanguageCode)
                    .build();
//            Toast.makeText(this, "This is translator", Toast.LENGTH_SHORT).show();
            final Translator translator = Translation.getClient(opt);
            DownloadConditions conditions = new DownloadConditions
                    .Builder()
                    .requireWifi()
                    .build();
//            Toast.makeText(this, "This is DownloadConditions", Toast.LENGTH_SHORT).show();
            translator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            result.setText("Translating...");
                            translator.translate(src).addOnSuccessListener(new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(String s) {
                                    result.setText(s);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this,
                                            "Failed to translate",
                                            Toast.LENGTH_SHORT).show();
                                    Log.v("Error101","failure in translation");
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this,
                                    "Failed to download model from MLKit!",
                                    Toast.LENGTH_SHORT).show();
                            Log.v("Error101","No model");
                        }
                    });

        }catch(Exception e){
            Log.v("Error101","No model");
            e.printStackTrace();
        }
    }

    private String getLanguageCode(String Language) {
        String code = "";
        switch(Language){
            case "English":
                code = TranslateLanguage.ENGLISH;
                break;
            case "Hindi":
                code = TranslateLanguage.HINDI;
                break;
            case "Telugu":
                code = TranslateLanguage.TELUGU;
                break;
            case "Japanese":
                code = TranslateLanguage.JAPANESE;
                break;
            case "Korean":
                code = TranslateLanguage.KOREAN;
                break;
        }
        return code;
    }
}