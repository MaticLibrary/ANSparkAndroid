package com.anspark.models;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("displayName")
    private String displayName;

    @SerializedName("birthDate")
    private String birthDate;

    @SerializedName("gender")
    private String gender;

    @SerializedName("preference")
    private String preference;

    // Конструктор
    public RegisterRequest(String email, String password, String displayName,
                           String birthDate, String gender, String preference) {
        this.email = email;
        this.password = password;
        this.displayName = displayName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.preference = preference;
    }

    // Гетери та сетери
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPreference() { return preference; }
    public void setPreference(String preference) { this.preference = preference; }
}