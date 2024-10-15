package com.rainy.homebudgettracker.images;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CloudfrontUrlRepository extends JpaRepository<CloudfrontUrl, Long> {
    Optional<CloudfrontUrl> findByKey(String key);
    Optional<CloudfrontUrl> findByUrl(String url);
    void deleteByKey(String key);
    void deleteByUrl(String url);
}
