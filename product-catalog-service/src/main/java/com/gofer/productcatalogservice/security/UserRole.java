package com.gofer.productcatalogservice.security;

public enum UserRole {
    USER,
    SELLER,
    ADMIN;

    public static UserRole fromString(String role) {
        if (role == null) return USER;
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            // return USER as default role
            return USER;
        }
    }
}
