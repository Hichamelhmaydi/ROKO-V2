package com.example.roko.security;

import com.example.roko.entity.User;
import com.example.roko.entity.Voyageurs;
import com.example.roko.entity.Admin;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String role;
    private Boolean actif;
    private Boolean bloque;
    private Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(User user) {
        String role;
        if (user instanceof Voyageurs) {
            role = "ROLE_VOYAGEUR";
        } else if (user instanceof Admin) {
            role = "ROLE_ADMIN";
        } else {
            role = "ROLE_USER";
        }

        return new UserPrincipal(
                user.getId(),
                user.getNom(),
                user.getPrenom(),
                user.getEmail(),
                user.getPassword(),
                role,
                user.getActif(),
                user.getBloque(),
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !Boolean.TRUE.equals(bloque);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(actif);
    }
}
