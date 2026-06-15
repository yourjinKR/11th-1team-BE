package org.example.knockin.repository.utils;

import org.example.knockin.entity.utils.AuthEmail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthEmailRepository extends JpaRepository<AuthEmail,Long> {
}
