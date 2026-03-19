package com.anspark.utils;

import com.anspark.models.AuthResponse;
import com.anspark.models.Chat;
import com.anspark.models.Match;
import com.anspark.models.Message;
import com.anspark.models.Photo;
import com.anspark.models.Profile;
import com.anspark.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MockData {
    private MockData() {
    }

    private static String id(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString();
    }

    public static AuthResponse sampleAuthResponse() {
        User user = new User("user_1", "Adrian", "adrian@anspark.com");
        AuthResponse response = new AuthResponse();
        response.setToken("mock_access_token");
        response.setUserId("user_1");
        response.setEmail("adrian@anspark.com");
        response.setProfile(sampleProfile());
        return response;
    }

    public static Profile sampleProfile() {
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setDisplayName("Adrian");
        profile.setAge(27);
        profile.setCity("Warszawa");
        profile.setBio("Frontend, siłownia, fotografia analogowa.");

        List<Photo> photos = new ArrayList<>();
        photos.add(new Photo(id("photo"), "local://male_profile", true));
        photos.add(new Photo(id("photo"), "local://male_profile", false));
        profile.setPhotos(photos);
        return profile;
    }

    public static List<Profile> sampleDiscoverProfiles() {
        List<Profile> profiles = new ArrayList<>();

        Profile maja = new Profile();
        maja.setId(2L);
        maja.setDisplayName("Maja");
        maja.setAge(24);
        maja.setBio("Uwielbiam góry, analogowe zdjęcia i nocne spacery po mieście.");
        maja.setCity("Krakow");

        List<Photo> majaPhotos = new ArrayList<>();
        majaPhotos.add(new Photo(id("photo"), "local://female_profile_1", true));
        maja.setPhotos(majaPhotos);
        profiles.add(maja);

        Profile kasia = new Profile();
        kasia.setId(3L);
        kasia.setDisplayName("Kasia");
        kasia.setAge(26);
        kasia.setBio("Biegam o poranku, lubię kino i szukam kogoś z dobrą energią.");
        kasia.setCity("Poznan");

        List<Photo> kasiaPhotos = new ArrayList<>();
        kasiaPhotos.add(new Photo(id("photo"), "local://female_profile_2", true));
        kasia.setPhotos(kasiaPhotos);
        profiles.add(kasia);

        return profiles;
    }

    public static List<Match> sampleMatches() {
        List<Match> matches = new ArrayList<>();
        for (Profile profile : sampleDiscoverProfiles()) {
            Match match = new Match();
            match.setId("match_" + profile.getId());
            match.setProfile(profile);
            match.setLiked(true);
            match.setMatchedAt("now");
            matches.add(match);
        }
        return matches;
    }

    public static List<Chat> sampleChats() {
        List<Chat> chats = new ArrayList<>();

        Profile maja = new Profile();
        maja.setId(2L);
        maja.setDisplayName("Maja");
        maja.setAge(24);

        List<Photo> majaPhotos = new ArrayList<>();
        majaPhotos.add(new Photo(id("photo"), "local://female_profile_1", true));
        maja.setPhotos(majaPhotos);

        Message lastMaja = new Message(id("msg"), "chat_maja", "profile_maja", "Jutro po 18:00 mam wolne, pasuje Ci?", "09:12", false);
        Chat chatMaja = new Chat();
        chatMaja.setId("chat_maja");
        chatMaja.setParticipant(maja);
        chatMaja.setLastMessage(lastMaja);
        chatMaja.setLastMessageAt("09:12");
        chats.add(chatMaja);

        Profile kasia = new Profile();
        kasia.setId(3L);
        kasia.setDisplayName("Kasia");
        kasia.setAge(26);

        List<Photo> kasiaPhotos = new ArrayList<>();
        kasiaPhotos.add(new Photo(id("photo"), "local://female_profile_2", true));
        kasia.setPhotos(kasiaPhotos);

        Message lastKasia = new Message(id("msg"), "chat_kasia", "profile_kasia", "Dzięki za super rozmowę wczoraj.", "Wczoraj", false);
        Chat chatKasia = new Chat();
        chatKasia.setId("chat_kasia");
        chatKasia.setParticipant(kasia);
        chatKasia.setLastMessage(lastKasia);
        chatKasia.setLastMessageAt("Wczoraj");
        chats.add(chatKasia);

        return chats;
    }

    public static List<Message> sampleMessages(String chatId) {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message(id("msg"), chatId, "profile_maja", "Cześć! Widziałam, że też lubisz trekking.", "09:10", false));
        messages.add(new Message(id("msg"), chatId, "user_1", "Tak, najczęściej wypady w góry na weekend.", "09:11", true));
        messages.add(new Message(id("msg"), chatId, "profile_maja", "Super! To może kawa i plan na mini wyjazd?", "09:12", false));
        return messages;
    }
}