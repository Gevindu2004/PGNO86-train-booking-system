package com.trainmanagement.trainmanagementsystem.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptUtil {
    public static String hash(String plainPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(plainPassword);
    }

    // Main method removed to prevent ClassNotFoundException issues
}
