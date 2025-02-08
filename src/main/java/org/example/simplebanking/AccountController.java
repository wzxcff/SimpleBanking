package org.example.simplebanking;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final UserRepository userRepository;

    private final JdbcTemplate jdbcTemplate;

    public AccountController(JdbcTemplate jdbcTemplate, UserRepository userRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
    }

    @GetMapping("/balance")
    public ResponseEntity<String> getBalance() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(Float.toString(user.getBalance()));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestBody Map<String, String> info) {
        float amount = Float.parseFloat(info.get("amount"));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        if (amount > user.getBalance()) {
            return ResponseEntity.badRequest().body("Not enough balance");
        }
        user.setBalance(user.getBalance() - amount);
        userRepository.save(user);
        return ResponseEntity.ok().body("Successfully withdrawn");
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@RequestBody Map<String, String> info) {
        float amount = Float.parseFloat(info.get("amount"));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        user.setBalance(user.getBalance() + amount);
        userRepository.save(user);
        return ResponseEntity.ok().body("Successfully deposited");
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest transferRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users fromAccount = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        Users toAccount = userRepository.findByUsername(transferRequest.getToAccount()).orElseThrow(() -> new RuntimeException("User not found"));
        float amount = transferRequest.getAmount();
        if (amount > fromAccount.getBalance()) {
            return ResponseEntity.badRequest().body("Not enough balance");
        }
        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);
        userRepository.save(fromAccount);
        userRepository.save(toAccount);
        return ResponseEntity.ok().body("Successfully transferred");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> delete() {
        System.out.println("Point reached");
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
        jdbcTemplate.execute("ALTER TABLE USERS AUTO_INCREMENT = 1");
        return ResponseEntity.ok().body("Deleted user: " + user.getUsername());
    }
}
