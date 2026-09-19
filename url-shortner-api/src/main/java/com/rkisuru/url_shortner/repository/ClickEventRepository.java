package com.rkisuru.url_shortner.repository;

import com.rkisuru.url_shortner.entity.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    long countByShortCode(String shortCode);

    /**
     * Daily click breakdown for the last 30 days.
     * Returns rows of [date, count].
     */
    @Query(value = """
            SELECT CAST(ce.clicked_at AS DATE) AS click_date, COUNT(*) AS click_count
            FROM url_service.click_events ce
            WHERE ce.short_code = :shortCode
              AND ce.clicked_at >= CURRENT_DATE - INTERVAL '30 days'
            GROUP BY CAST(ce.clicked_at AS DATE)
            ORDER BY click_date
            """, nativeQuery = true)
    List<Object[]> findDailyClickBreakdown(@Param("shortCode") String shortCode);

    /**
     * Top referrers by click count (limited to top N).
     * Returns rows of [referrer, count].
     */
    @Query(value = """
            SELECT COALESCE(ce.referrer, 'Direct') AS referrer, COUNT(*) AS click_count
            FROM url_service.click_events ce
            WHERE ce.short_code = :shortCode
            GROUP BY COALESCE(ce.referrer, 'Direct')
            ORDER BY click_count DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findTopReferrers(@Param("shortCode") String shortCode,
                                    @Param("limit") int limit);
}
