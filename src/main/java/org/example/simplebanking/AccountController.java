package org.example.simplebanking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/balance/{id}")
    public ResponseEntity<String> getBalance(@PathVariable("id") long id) {
        Users user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(Float.toString(user.getBalance()));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestBody Map<String, String> info) {
        long id = Long.parseLong(info.get("id"));
        float amount = Float.parseFloat(info.get("amount"));
        Users user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (amount > user.getBalance()) {
            return ResponseEntity.badRequest().body("Not enough balance");
        }
        user.setBalance(user.getBalance() - amount);
        userRepository.save(user);
        return ResponseEntity.ok().body("Successfully withdrawn");
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@RequestBody Map<String, String> info) {
        long id = Long.parseLong(info.get("id"));
        float amount = Float.parseFloat(info.get("amount"));
        Users user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setBalance(user.getBalance() + amount);
        userRepository.save(user);
        return ResponseEntity.ok().body("Successfully deposited");
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest transferRequest) {
        Users fromAccount = userRepository.findById(transferRequest.getFromAccount()).orElseThrow(() -> new RuntimeException("User not found"));
        Users toAccount = userRepository.findById(transferRequest.getToAccount()).orElseThrow(() -> new RuntimeException("User not found"));
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
}
