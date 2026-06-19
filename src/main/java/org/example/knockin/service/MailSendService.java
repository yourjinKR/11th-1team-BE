package org.example.knockin.service;

import java.util.Map;

public interface MailSendService {
    Object mailSend(String to, String subject, String message);
    Map<String, Object> mailWebHook(Object payload, Map<String, String> headers);
}
