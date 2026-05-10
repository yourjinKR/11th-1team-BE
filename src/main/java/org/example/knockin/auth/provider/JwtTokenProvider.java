package org.example.knockin.auth.provider;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.knockin.entity.MemberEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final Clock clock = Clock.systemUTC();

    @Value("${auth.jwt.secret}")
    private String secretKey;

    @Value("${auth.jwt.access-token-exp-seconds}")
    private long accessTokenExpSec;

    @PostConstruct
    protected void init() {
        log.info("[init] JwtTokenProvider: Start init secretKey");
        System.out.println(secretKey);
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
        System.out.println(secretKey);
        log.info("[init] JwtTokenProvider: Finish init secretKey");
    }

    public String createToken(MemberEntity member) {
        Instant now = clock.instant();
        Instant accessTokenExpiresAt = now.plusSeconds(accessTokenExpSec);

        return Jwts.builder()
                .subject(String.valueOf(member.getMemberId()))
                .claim("role", member.getRole().name())
                .issuedAt(Date.from(now))
                .signWith(signingKey())
                .expiration(Date.from(accessTokenExpiresAt))
                .compact();
    }

    private Key signingKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}
