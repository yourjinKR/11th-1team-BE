package org.example.knockin.repository.payment;

import org.example.knockin.entity.payment.Point;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointRepository  extends JpaRepository<Point, Long> {
}
