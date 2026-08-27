SET client_encoding = 'UTF8';

UPDATE users 
SET full_name = 'Giảng viên Mass ' || split_part(full_name, 'Mass ', 2) 
WHERE full_name LIKE '%Mass%' AND role = 'LECTURER';

UPDATE users 
SET full_name = 'Sinh viên Mass ' || split_part(full_name, 'Mass ', 2) 
WHERE full_name LIKE '%Mass%' AND role = 'STUDENT';
