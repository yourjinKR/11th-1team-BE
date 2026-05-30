package org.example.knockin.repository.inquiry;

import org.example.knockin.entity.inquiry.InquiryComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryCommentRepository extends JpaRepository<InquiryComment, Long> {
}