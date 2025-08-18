-- FUNCTION: public.get_newsfeed_posts(character varying, character varying[], integer, integer)

-- DROP FUNCTION IF EXISTS public.get_newsfeed_posts(character varying, character varying[], integer, integer);

CREATE OR REPLACE FUNCTION public.get_newsfeed_posts(
	p_user_id character varying,
	p_friend_ids character varying[],
	p_limit integer,
	p_offset integer)
    RETURNS TABLE(post_id bigint, user_id character varying, content character varying, title character varying, created_at bigint, type_id bigint, total_reactions bigint, comments json, hashtags text[], images text[]) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
BEGIN
    RETURN QUERY
    SELECT 
        bp.post_id,
        bp.user_id,
        bp.content,
        bp.title,
        bp.created_at,
        ptc.type_id,

        COUNT(r.*) FILTER (WHERE r.type IS NOT NULL) AS total_reactions,

        (
            SELECT json_agg(c ORDER BY c.created_at DESC)
            FROM (
                SELECT cmt.comment_id, cmt.user_id, cmt.content, cmt.created_at
                FROM comments cmt
                WHERE cmt.post_id = bp.post_id
                ORDER BY cmt.created_at DESC
                LIMIT 5
            ) c
        ) AS comments,

        (
            SELECT ARRAY_AGG(h.name::text)::text[]
            FROM post_hashtags ph
            JOIN hashtags h ON ph.hashtag_id = h.id
            WHERE ph.post_id = bp.post_id
        ) AS hashtags,

        (
            SELECT ARRAY_AGG(pi.image_url::text)::text[]
            FROM post_images pi
            WHERE pi.post_id = bp.post_id
        ) AS images

    FROM posts bp
    JOIN post_type_content ptc ON bp.post_id = ptc.post_id
    JOIN user_type_preference utp ON ptc.type_id = utp.type_id
    LEFT JOIN likes r ON r.post_id = bp.post_id

    WHERE utp.user_id = p_user_id
      AND bp.user_id = ANY (p_friend_ids)

    GROUP BY bp.post_id, bp.user_id, bp.content, bp.title, bp.created_at, ptc.type_id
    ORDER BY bp.created_at DESC
    LIMIT p_limit OFFSET p_offset;
END;
$BODY$;

ALTER FUNCTION public.get_newsfeed_posts(character varying, character varying[], integer, integer)
    OWNER TO postgres;
