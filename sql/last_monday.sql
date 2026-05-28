-- 题3：获取「上周一」的日期（以 MySQL 8 为例）
-- 思路：先得到本周一，再减 7 天即为上周一

-- 写法一（推荐，可读性好）
SELECT DATE_SUB(
           DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY),
           INTERVAL 7 DAY
       ) AS last_monday;

-- 写法二：使用 DATE_FORMAT + 周偏移
SELECT DATE_SUB(CURDATE(), INTERVAL (WEEKDAY(CURDATE()) + 7) DAY) AS last_monday;

-- PostgreSQL 等价写法
-- SELECT (date_trunc('week', CURRENT_DATE)::date - INTERVAL '7 days')::date AS last_monday;

-- SQL Server 等价写法
-- SELECT DATEADD(DAY, -7, DATEADD(DAY, 1 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE))) AS last_monday;
