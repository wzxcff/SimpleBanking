package org.example.simplebanking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private PasswordEncoder passwordEncode = new BCryptPasswordEncoder();

    private final JwtUtils jwtUtils;
    private UserRepository userRepository;


    public AuthController(JwtUtils jwtUtils, UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Users users) {
        if (userRepository.findByUsername(users.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username %s already exists".formatted(users.getUsername()));
        }

        users.setPassword(passwordEncode.encode(users.getPassword()));
        userRepository.save(users);
        return ResponseEntity.ok().body("Username %s registered successfully".formatted(users.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        if (userRepository.findByUsername(username).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Users user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncode.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials!");
        }

        String token = jwtUtils.generateJwtToken(username);

        return ResponseEntity.ok(new JwtResponse(token));
    }
}

class JwtResponse {
    private String token;

    public JwtResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}

