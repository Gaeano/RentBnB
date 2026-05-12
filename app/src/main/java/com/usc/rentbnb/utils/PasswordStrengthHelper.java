package com.usc.rentbnb.utils;

import com.usc.rentbnb.R;

public class PasswordStrengthHelper {

    public enum Strength {
        EMPTY(0, "Too Short", R.color.text_grey),
        WEAK(1, "Weak", R.color.strength_weak),
        FAIR(2, "Fair", R.color.strength_fair),
        GOOD(3, "Good", R.color.strength_good),
        STRONG(4, "Strong", R.color.strength_strong);

        public final int score;
        public final String label;
        public final int colorRes;

        Strength(int score, String label, int colorRes) {
            this.score = score;
            this.label = label;
            this.colorRes = colorRes;
        }
    }

    public static Strength calculateStrength(String password) {
        if (password == null || password.isEmpty()) {
            return Strength.EMPTY;
        }
        
        if (password.length() < 6) {
            return Strength.WEAK;
        }

        int score = 0;
        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[^a-zA-Z0-9].*")) score++;

        switch (score) {
            case 0:
            case 1:
                return Strength.WEAK;
            case 2:
                return Strength.FAIR;
            case 3:
                return Strength.GOOD;
            case 4:
                return Strength.STRONG;
            default:
                return Strength.WEAK;
        }
    }
}