package com.qldtkhoa.qldtkhoa_app.security;

import lombok.Getter;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Getter
public class AppUserPrincipal implements UserDetails, CredentialsContainer {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String email;
    private String password;
    private final String role;
    private final boolean enabled;
    private final String displayName;
    private final String maNS;
    private final String maSV;
    private final boolean isAdvisor;
    private final Collection<? extends GrantedAuthority> authorities;

    public AppUserPrincipal(String email, String password, String role, boolean enabled,
                            String displayName, String maNS, String maSV, boolean isAdvisor) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
        this.displayName = displayName;
        this.maNS = maNS;
        this.maSV = maSV;
        this.isAdvisor = isAdvisor;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppUserPrincipal other)) return false;
        return Objects.equals(normalizedEmail(email), normalizedEmail(other.email));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(normalizedEmail(email));
    }

    private static String normalizedEmail(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
