-- Ngày tạo: 11/07/2025
CREATE OR REPLACE FUNCTION get_trending_post_stats()
RETURNS TABLE (
    p_id BIGINT,
    p_created TIMESTAMP,
    p_likes BIGINT,
    p_comments BIGINT,
    p_shares BIGINT
) AS $$
BEGIN
    RETURN QUERY
    WITH created_posts AS (
        SELECT post_id, MIN(occurred_at) AS post_creation_time
        FROM trending_events
        WHERE event_type = 'CREATE_POST' AND occurred_at >= NOW() - INTERVAL '48 HOURS'
        GROUP BY post_id
    ),
    aggregates AS (
        SELECT
            post_id,
            SUM(CASE WHEN event_type = 'NEW_LIKE' THEN 1 ELSE 0 END) AS likes,
            SUM(CASE WHEN event_type = 'NEW_COMMENT' THEN 1 ELSE 0 END) AS comments,
            SUM(CASE WHEN event_type = 'NEW_SHARE' THEN 1 ELSE 0 END) AS shares
        FROM trending_events
        WHERE occurred_at >= NOW() - INTERVAL '48 HOURS'
        GROUP BY post_id
    )
    SELECT
        c.post_id,
        c.post_creation_time,
        COALESCE(a.likes, 0),
        COALESCE(a.comments, 0),
        COALESCE(a.shares, 0)
    FROM created_posts c
    LEFT JOIN aggregates a ON c.post_id = a.post_id;
END;
$$ LANGUAGE plpgsql;


SELECT * FROM get_trending_post_stats();

