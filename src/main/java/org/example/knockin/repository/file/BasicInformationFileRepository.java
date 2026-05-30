package org.example.knockin.repository.file;

import org.example.knockin.entity.file.BasicInformationFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BasicInformationFileRepository extends JpaRepository<BasicInformationFile, Long> {
}