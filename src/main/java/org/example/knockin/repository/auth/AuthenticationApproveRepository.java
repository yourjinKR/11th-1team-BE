package org.example.knockin.repository.auth;

import org.example.knockin.entity.auth.Authentication;
import org.example.knockin.entity.auth.AuthenticationApprove;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthenticationApproveRepository extends JpaRepository<AuthenticationApprove, Long> {
    Optional<AuthenticationApprove> findByAuthentication(Authentication authentication);
}