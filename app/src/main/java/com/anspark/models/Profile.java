package com.anspark.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Profile {
    @SerializedName("id")
    private Long id;

    @SerializedName("displayName")
    private String displayName;

    @SerializedName("birthDate")
    private String birthDate;

    @SerializedName("gender")
    private String gender;

    @SerializedName("preference")
    private String preference;

    @SerializedName("age")
    private int age;

    @SerializedName("bio")
    private String bio;

    @SerializedName("city")
    private String city;

    @SerializedName("avatarUrl")
    private String avatarUrl;

    @SerializedName("photos")
    private List<Photo> photos;

    @SerializedName("tags")
    private List<String> tags;

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPreference() { return preference; }
    public void setPreference(String preference) { this.preference = preference; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public List<Photo> getPhotos() { return photos; }
    public void setPhotos(List<Photo> photos) { this.photos = photos; }
}