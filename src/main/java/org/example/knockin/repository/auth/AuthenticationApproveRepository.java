package org.example.knockin.repository.auth;

import org.example.knockin.entity.auth.AuthenticationApprove;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthenticationApproveRepository extends JpaRepository<AuthenticationApprove, Long> {
}