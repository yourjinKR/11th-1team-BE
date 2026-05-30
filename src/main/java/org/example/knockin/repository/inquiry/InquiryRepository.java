package org.example.knockin.repository.inquiry;

import org.example.knockin.entity.inquiry.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
}