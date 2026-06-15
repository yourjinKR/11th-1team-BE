package org.example.knockin.repository.payment;

import org.example.knockin.entity.payment.PointLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointLogRepository  extends JpaRepository<PointLog, Long> {
}
