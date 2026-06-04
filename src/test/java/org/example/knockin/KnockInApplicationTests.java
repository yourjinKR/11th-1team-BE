package org.example.knockin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("애플리케이션 컨텍스트")
class KnockInApplicationTests {

    @Test
    @DisplayName("테스트 프로필로 애플리케이션 컨텍스트를 로드한다")
    void contextLoads() {
    }

}
