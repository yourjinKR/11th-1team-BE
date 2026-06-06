package org.example.knockin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;
import java.util.List;
import org.example.knockin.entity.member.Gender;
import org.springframework.web.bind.annotation.RequestParam;

@Data
public class BoardListDto {
    @Data
    public static class Request {
        Long regionId;
        Integer roomTypeId;
        Gender gender;
        Integer minDeposit;
        Integer maxDeposit;
        Integer minMounthRent;
        Integer maxMounthRent;
    }

    @Data
    public static class Response {
        private List<BoardItem> boards;

        @Data
        public static class BoardItem {
            private Long boardId;
            private String image;
        }
    }
}
