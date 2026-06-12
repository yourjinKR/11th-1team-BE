package org.example.knockin.repository.board.row;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.knockin.entity.room.Region;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MyRoommateBoardRow {
    private Long boardId;
    private String title;
    private Integer deposit;
    private Integer monthlyRent;
    private LocalDateTime createdAt;
    private String memberName;
    private String image;
    private String roomTypeName;
    private Region regionEntity;
}
