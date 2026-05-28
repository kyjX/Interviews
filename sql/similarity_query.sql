-- 题4：SQL 相似度查询示例
-- 场景：在商品表中按关键词模糊匹配，并按相似度排序

-- 示例表结构
-- CREATE TABLE products (
--     id          BIGINT PRIMARY KEY,
--     name        VARCHAR(255) NOT NULL,
--     description TEXT
-- );

SET @keyword = '无线蓝牙耳机';

-- MySQL：使用 LOCATE / 长度比 作为简易相似度分数（0~1）
SELECT
    id,
    name,
    description,
    (
        CASE
            WHEN name = @keyword THEN 1.0
            WHEN LOCATE(@keyword, name) > 0 THEN
                CHAR_LENGTH(@keyword) / CHAR_LENGTH(name)
            ELSE 0
        END
    ) AS name_score,
    (
        CASE
            WHEN description IS NOT NULL AND LOCATE(@keyword, description) > 0 THEN
                CHAR_LENGTH(@keyword) / CHAR_LENGTH(description)
            ELSE 0
        END
    ) AS desc_score,
    GREATEST(
        CASE WHEN name = @keyword THEN 1.0
             WHEN LOCATE(@keyword, name) > 0 THEN CHAR_LENGTH(@keyword) / CHAR_LENGTH(name)
             ELSE 0 END,
        CASE WHEN description IS NOT NULL AND LOCATE(@keyword, description) > 0
             THEN CHAR_LENGTH(@keyword) / CHAR_LENGTH(description)
             ELSE 0 END
    ) AS similarity
FROM products
WHERE name LIKE CONCAT('%', @keyword, '%')
   OR description LIKE CONCAT('%', @keyword, '%')
ORDER BY similarity DESC, id ASC
LIMIT 20;

-- PostgreSQL + pg_trgm（需扩展，生产常用）
-- CREATE EXTENSION IF NOT EXISTS pg_trgm;
-- SELECT id, name, similarity(name, '无线蓝牙耳机') AS sim
-- FROM products
-- WHERE name % '无线蓝牙耳机'
-- ORDER BY sim DESC
-- LIMIT 20;
