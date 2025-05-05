package com.vigverse.stack.auth.repositories;


import com.vigverse.stack.auth.entities.ForgotPassword;
import com.vigverse.stack.auth.entities.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, Integer> {

    @Modifying
    @Transactional
    @Query("DELETE FROM ForgotPassword fp WHERE fp.user = :user")
    void deleteByUser(@Param("user") User user);

    Optional<ForgotPassword> findByOtpAndUser_UserId(Integer otp, Integer userId);
}
