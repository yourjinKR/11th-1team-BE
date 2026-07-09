package org.example.knockin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.knockin.entity.room.RoommateCalendarCategory;
import org.example.knockin.repository.room.RoommateCalendarCategoryRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoommateCalendarCategoryServiceImpl {

    private final RoommateCalendarCategoryRepository roommateCalendarCategoryRepository;

    public RoommateCalendarCategory save(String name) {
        RoommateCalendarCategory category = RoommateCalendarCategory.builder()
                .name(name)
                .build();
        return roommateCalendarCategoryRepository.save(category);
    }
}
