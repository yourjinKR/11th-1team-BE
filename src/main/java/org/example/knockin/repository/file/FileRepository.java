package org.example.knockin.repository.file;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.example.knockin.entity.file.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
    Optional<File> findFirstBySavedFileName(String savedFileName);
    List<File> findBySavedFileNameIn(Collection<String> savedFileNames);
}