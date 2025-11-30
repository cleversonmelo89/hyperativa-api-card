package com.hyperativa.cardapi.security;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    
    // Armazena apenas as informações do usuário, não o UserDetails completo
    private final Map<String, UserInfo> usersInfo = new HashMap<>();

    @PostConstruct
    public void init() {
        initializeUsers();
    }

    private void initializeUsers() {
        usersInfo.put("admin", new UserInfo(
                "admin",
                passwordEncoder.encode("admin123"),
                Arrays.asList("CARD_REGISTER", "CARD_QUERY")
        ));

        usersInfo.put("register", new UserInfo(
                "register",
                passwordEncoder.encode("register123"),
                Arrays.asList("CARD_REGISTER")
        ));

        usersInfo.put("query", new UserInfo(
                "query",
                passwordEncoder.encode("query123"),
                Arrays.asList("CARD_QUERY")
        ));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo userInfo = usersInfo.get(username);
        if (userInfo == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        
        // Cria um novo UserDetails a cada chamada para evitar problemas de estado
        List<SimpleGrantedAuthority> authorities = userInfo.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        
        return User.builder()
                .username(userInfo.getUsername())
                .password(userInfo.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
    
    // Classe interna para armazenar informações do usuário
    private static class UserInfo {
        private final String username;
        private final String password;
        private final List<String> roles;
        
        public UserInfo(String username, String password, List<String> roles) {
            this.username = username;
            this.password = password;
            this.roles = roles;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public List<String> getRoles() {
            return roles;
        }
    }
}

