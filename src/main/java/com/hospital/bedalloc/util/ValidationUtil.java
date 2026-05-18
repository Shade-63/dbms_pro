package com.hospital.bedalloc.util;

public class ValidationUtil {
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^[0-9]{10}$");
    }

    public static boolean isValidAge(int age) {
        return age >= 0 && age <= 150;
    }
}
