WITH target_classes AS (
  SELECT id, row_number() over (order by id) - 1 as rm
  FROM school_classes
),
ranked_students AS (
  SELECT user_id, row_number() over (order by user_id) - 1 as rm
  FROM student_profiles
)
UPDATE student_profiles sp
SET school_class_id = (
  SELECT c.id FROM target_classes c 
  WHERE c.rm = (rs.rm / 20)
)
FROM ranked_students rs
WHERE sp.user_id = rs.user_id;
