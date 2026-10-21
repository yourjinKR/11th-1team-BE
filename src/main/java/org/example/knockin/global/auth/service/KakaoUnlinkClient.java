package org.example.knockin.global.auth.service;


import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.PostExchange;

public interface KakaoUnlinkClient {
    @PostExchange(url = "/v1/user/unlink", contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String unlink(@RequestHeader("Authorization") String authorization, @RequestParam("target_id_type") String targetIdType, @RequestParam("target_id") String targetId);
}
