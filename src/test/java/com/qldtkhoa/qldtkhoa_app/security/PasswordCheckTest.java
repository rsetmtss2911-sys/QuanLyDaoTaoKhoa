package com.qldtkhoa.qldtkhoa_app.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordCheckTest {

    @Test
    void testPasswordMatches() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String verifiedHash123456 = "$2a$10$7zNMO9gnsT8qduEbUuWmjeoi/5wSHU/xHc8Irt1Gc//M.Iu6Wr6Y2";
        assertThat(encoder.matches("123456", verifiedHash123456)).isTrue();
    }

    @Test
    void testDaoAuthenticationProviderWithVerifiedHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String verifiedHash123456 = "$2a$10$7zNMO9gnsT8qduEbUuWmjeoi/5wSHU/xHc8Irt1Gc//M.Iu6Wr6Y2";

        AppUserPrincipal principal = new AppUserPrincipal(
                "admin@truong.edu.vn",
                verifiedHash123456,
                "ADMIN",
                true,
                "Quản Trị Viên",
                "NS_ADMIN",
                null,
                false
        );

        org.springframework.security.authentication.dao.DaoAuthenticationProvider provider =
                new org.springframework.security.authentication.dao.DaoAuthenticationProvider(username -> principal);
        provider.setPasswordEncoder(encoder);

        org.springframework.security.core.Authentication auth = provider.authenticate(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("admin@truong.edu.vn", "123456")
        );

        assertThat(auth.isAuthenticated()).isTrue();
        assertThat(auth.getPrincipal()).isEqualTo(principal);
    }
}
