package org.example.knockin.auth.service;

public interface Oauth2DeleteService {
    boolean requestUnlink(String providerId);
}
