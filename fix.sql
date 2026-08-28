WITH ranked_classes AS (
  SELECT id, row_number() over (order by id) - 1 as rm
  FROM school_classes
),
ranked_lecturers AS (
  SELECT user_id, row_number() over (order by user_id) - 1 as rm
  FROM users
  WHERE role = 'LECTURER'
)
UPDATE school_classes sc
SET homeroom_lecturer_id = (
  SELECT rl.user_id FROM ranked_lecturers rl
  WHERE rl.rm = (rc.rm % (SELECT count(*) FROM users WHERE role = 'LECTURER'))
)
FROM ranked_classes rc
WHERE sc.id = rc.id;
