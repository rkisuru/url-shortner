package com.rkisuru.url_shortner.repository;

import com.rkisuru.url_shortner.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);

    List<Url> findAllByOwnerUserId(String ownerUserId);

    Optional<Url> findByShortCodeAndOwnerUserId(String shortCode, String ownerUserId);

    @Modifying
    @Query("UPDATE Url u SET u.clickCount = u.clickCount + 1 WHERE u.shortCode = :shortCode")
    void incrementClickCount(@Param("shortCode") String shortCode);

    @Modifying
    @Query("DELETE FROM Url u WHERE u.shortCode = :shortCode AND u.ownerUserId = :ownerUserId")
    int deleteByShortCodeAndOwnerUserId(@Param("shortCode") String shortCode,
                                        @Param("ownerUserId") String ownerUserId);
}
