package com.anspark.utils;

import androidx.annotation.Nullable;

import com.anspark.R;
import com.anspark.models.Photo;
import com.anspark.models.Profile;

import java.util.List;

public final class ImageUtils {
    private ImageUtils() {
    }

    public static int pickDiscoverPlaceholder(String seed) {
        int[] images = {R.drawable.female_profile_1, R.drawable.female_profile_2};
        if (seed == null || seed.isEmpty()) {
            return images[0];
        }
        int index = Math.abs(seed.hashCode()) % images.length;
        return images[index];
    }

    public static int pickChatPlaceholder(String seed) {
        int[] images = {R.drawable.female_profile_1, R.drawable.female_profile_2};
        if (seed == null || seed.isEmpty()) {
            return images[0];
        }
        int index = Math.abs(seed.hashCode()) % images.length;
        return images[index];
    }

    public static int resolveProfileImage(@Nullable Profile profile, @Nullable String fallbackSeed) {
        if (profile == null) {
            return pickChatPlaceholder(fallbackSeed);
        }

        int avatarRes = resolveImageUrl(profile.getAvatarUrl());
        if (avatarRes != 0) {
            return avatarRes;
        }

        List<Photo> photos = profile.getPhotos();
        if (photos != null) {
            for (Photo photo : photos) {
                if (photo != null && photo.isPrimary()) {
                    int photoRes = resolveImageUrl(photo.getUrl());
                    if (photoRes != 0) {
                        return photoRes;
                    }
                }
            }

            for (Photo photo : photos) {
                if (photo != null) {
                    int photoRes = resolveImageUrl(photo.getUrl());
                    if (photoRes != 0) {
                        return photoRes;
                    }
                }
            }
        }

        String seed = fallbackSeed;
        if ((seed == null || seed.isEmpty()) && profile.getId() != null) {
            seed = String.valueOf(profile.getId());
        }
        return pickChatPlaceholder(seed);
    }

    private static int resolveImageUrl(@Nullable String url) {
        if (url == null || url.trim().isEmpty()) {
            return 0;
        }

        String normalized = url.trim().toLowerCase();
        if (normalized.contains("female_profile_1")) {
            return R.drawable.female_profile_1;
        }
        if (normalized.contains("female_profile_2")) {
            return R.drawable.female_profile_2;
        }
        if (normalized.contains("male_profile")) {
            return R.drawable.male_profile;
        }
        return 0;
    }
}
