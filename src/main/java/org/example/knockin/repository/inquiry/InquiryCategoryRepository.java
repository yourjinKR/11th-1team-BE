package org.example.knockin.repository.inquiry;

import org.example.knockin.entity.inquiry.InquiryCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryCategoryRepository extends JpaRepository<InquiryCategory, Long> {
}