package org.example.knockin.repository.inquiry;

import org.example.knockin.entity.inquiry.InquiryCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

public interface InquiryCategoryRepository extends JpaRepository<InquiryCategory, Long> {
    List<InquiryCategory> findAllByIsDeleted(Boolean isDeleted);
}