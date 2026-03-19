package com.anspark.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.anspark.R;
import com.anspark.models.RegisterRequest;
import com.anspark.viewmodel.AuthViewModel;
import com.google.android.material.button.MaterialButton;

public class RegisterActivity extends AppCompatActivity {
    private String pendingName;
    private String pendingAge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText nameInput = findViewById(R.id.inputName);
        EditText ageInput = findViewById(R.id.inputAge);
        EditText emailInput = findViewById(R.id.inputEmail);
        EditText passwordInput = findViewById(R.id.inputPassword);
        EditText cityInput = findViewById(R.id.inputCity);

        MaterialButton nextButton = findViewById(R.id.buttonNextStep);
        MaterialButton goLoginButton = findViewById(R.id.buttonGoLogin);

        AuthViewModel viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        viewModel.getAuthResponse().observe(this, response -> {
            if (response != null && pendingName != null && pendingAge != null) {
                Intent intent = new Intent(this, EditProfileActivity.class);
                intent.putExtra("name", pendingName);
                intent.putExtra("age", pendingAge);
                startActivity(intent);
            }
        });

        viewModel.getError().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLoading().observe(this, loading -> {
            boolean enabled = loading == null || !loading;
            nextButton.setEnabled(enabled);
        });

        nextButton.setOnClickListener(v -> {
            if (isAnyEmpty(nameInput, ageInput, emailInput, passwordInput, cityInput)) {
                Toast.makeText(this, "Uzupelnij wszystkie pola", Toast.LENGTH_SHORT).show();
                return;
            }

            pendingName = nameInput.getText().toString().trim();
            pendingAge = ageInput.getText().toString().trim();

            int ageValue = 0;
            try {
                ageValue = Integer.parseInt(pendingAge);
            } catch (NumberFormatException ignored) {
            }

            String birthDate = "2000-01-01";
            String gender = "MALE";
            String preference = "WOMEN";

            RegisterRequest request = new RegisterRequest(
                    emailInput.getText().toString().trim(),      // email
                    passwordInput.getText().toString().trim(),   // password
                    pendingName,                                   // displayName
                    birthDate,                                     // birthDate
                    gender,                                         // gender
                    preference                                      // preference
            );

            viewModel.register(request);
        });

        goLoginButton.setOnClickListener(v -> finish());
    }

    private boolean isAnyEmpty(EditText... fields) {
        for (EditText field : fields) {
            if (TextUtils.isEmpty(field.getText().toString().trim())) {
                return true;
            }
        }
        return false;
    }
}
