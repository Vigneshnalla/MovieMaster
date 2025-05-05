package com.vigverse.stack.controllers;

import com.vigverse.stack.auth.entities.ForgotPassword;
import com.vigverse.stack.auth.entities.User;
import com.vigverse.stack.auth.repositories.ForgotPasswordRepository;
import com.vigverse.stack.auth.repositories.UserRepository;
import com.vigverse.stack.auth.utils.ChangePassword;
import com.vigverse.stack.dto.MailBody;
import com.vigverse.stack.service.EmailService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

@RestController
@RequestMapping("/forgotPassword")
public class ForgotPasswordController {

    private final UserRepository userRepository;
    private final EmailService emailService;

    private final ForgotPasswordRepository forgotPasswordRepository;

    private final PasswordEncoder passwordEncoder;

    public ForgotPasswordController(UserRepository userRepository, EmailService emailService, ForgotPasswordRepository forgotPasswordRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.forgotPasswordRepository = forgotPasswordRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // send mail for email verification
    @Transactional
    @PostMapping("/verifyMail")
    public ResponseEntity<String> verifyEmail(@RequestParam String email) {
        System.out.println("verifyEmail "+email);
        email = email.trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide a valid email! " ));

        int otp = otpGenerator();
        System.out.println("otp");
        MailBody mailBody = MailBody.builder()
                .to(email)
                .subject("OTP for Forgot Password request")
                .text("This is the OTP for your Forgot Password request : " + otp)
                .from("vigneshnalla888@gmail.com") // ✅ Must match spring.mail.username
                .build();


        System.out.println("mailBody ");
        if (user != null) {
            System.out.println("Found user: " + user.getUserId());
            forgotPasswordRepository.deleteByUser(user);
        } else {
            System.out.println("User not found for email: " + email);
        }
        ForgotPassword fp = ForgotPassword.builder()
                .otp(otp)
                .expirationTime(new Date(System.currentTimeMillis() + 2 * 60 * 1000)) // Adding 2 minutes
                .expirationTime(new Date(System.currentTimeMillis() + 2 * 60 * 1000)) // Adding 2 minutes
                .user(user)
                .build();

        emailService.sendSimpleMessage(mailBody);

        forgotPasswordRepository.save(fp);

        return ResponseEntity.ok("Email sent for verification!");
    }

    @PostMapping("/verifyOtp/{otp}/{email}")
    public ResponseEntity<String> verifyOtp(@PathVariable Integer otp, @PathVariable String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide an valid email!"));

        System.out.println("Fp ++++++++++>");
        ForgotPassword fp = forgotPasswordRepository.findByOtpAndUser_UserId(otp, user.getUserId())
                .orElseThrow(() -> new RuntimeException("Invalid OTP for email: " + email));
        if (user.getUserId() == null) {
            userRepository.save(user);
        }

        if (fp.getExpirationTime().before(Date.from(Instant.now()))) {
            forgotPasswordRepository.deleteById(fp.getFpid());
            return new ResponseEntity<>("OTP has expired!", HttpStatus.EXPECTATION_FAILED);
        }

        return ResponseEntity.ok("OTP verified!");
    }


    @PostMapping("/changePassword/{email}")
    public ResponseEntity<String> changePasswordHandler(@RequestBody ChangePassword changePassword,
                                                        @PathVariable String email) {
        if (!Objects.equals(changePassword.password(), changePassword.repeatPassword())) {
            return new ResponseEntity<>("Please enter the password again!", HttpStatus.EXPECTATION_FAILED);
        }

        String encodedPassword = passwordEncoder.encode(changePassword.password());
        userRepository.updatePassword(email, encodedPassword);

        return ResponseEntity.ok("Password has been changed!");
    }

    private Integer otpGenerator() {
        Random random = new Random();
        return random.nextInt(100_000, 999_999);
    }
}
