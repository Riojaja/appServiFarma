package com.example.proyecto.app.service;


import com.example.proyecto.app.dto.response.ReniecData;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ReniecCacheService {

    private final Cache<String, ReniecData> cache;

    public ReniecCacheService() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.DAYS) // Los datos de DNI no cambian
                .maximumSize(1000) // Guardar hasta 1000 DNI únicos
                .build();
    }

    public ReniecData get(String dni) {
        return cache.getIfPresent(dni);
    }

    public void put(String dni, ReniecData data) {
        cache.put(dni, data);
    }

    public void invalidate(String dni) {
        cache.invalidate(dni);
    }

    public void clear() {
        cache.invalidateAll();
    }
}