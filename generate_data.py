import uuid
import random
from datetime import datetime

# $2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy (Hash for 123456)
BCRYPT = "$2a$10$wE8F.hL1X4y98A.tP9C.c.bXX1yVqyG0qfMh9J0vH1pQ1XQ.D4tVy"

def generate_users():
    sql = "\n-- HÀNG LOẠT USERS MỚI\n"
    # Lecturers
    for i in range(1, 50):
        sql += f"INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('gv_mass_{i}@edu.vn', '{BCRYPT}', 'Giảng viên Mass {i}', 'LECTURER', true, NOW(), '0901{i:05d}', 'GVMASS{i}');\n"
    # Students
    for i in range(1, 200):
        sql += f"INSERT INTO users (email, password, full_name, role, is_active, created_at, phone, code) VALUES ('sv_mass_{i}@edu.vn', '{BCRYPT}', 'Sinh viên Mass {i}', 'STUDENT', true, NOW(), '0902{i:05d}', 'SVMASS{i}');\n"
    return sql

def generate_classes_and_courses():
    sql = "\n-- HÀNG LOẠT MAJORS, CLASSES, COURSES\n"
    # Use hardcoded existing major IDs from the database to avoid unique constraint violations
    majors = [
        "5f3c892b-10e7-44a0-ac4e-4662096e5f83",
        "ac2103ab-fe2b-4974-bdab-e95367cc91c3",
        "d2ca4dda-1e73-4d76-a8e5-08ee1aa97688",
        "6add087e-8745-48c2-8890-6627e3d90a8c",
        "7b4a4d87-a9cb-4546-83d1-d8569c89a166"
    ]

    # Classes
    classes = []
    for c in range(1, 40):
        c_id = str(uuid.uuid4())
        classes.append(c_id)
        m_id = random.choice(majors)
        # Assuming lecturer user_id 4 exists as admin/lecturer from standard mock data
        sql += f"INSERT INTO school_classes (id, class_name, major_id, entry_year, homeroom_lecturer_id) VALUES ('{c_id}', 'CLASS-MASS-{c}', '{m_id}', 2026, 4);\n"

    # Courses
    for c in range(1, 50):
        c_id = str(uuid.uuid4())
        sql += f"INSERT INTO courses (id, title, description, is_published, created_at) VALUES ('{c_id}', 'Khóa học Mass chuyên sâu {c}', 'Nội dung khóa {c}', true, NOW());\n"

    return sql

with open("massive_data.sql", "w", encoding="utf-8") as f:
    # f.write(generate_users())
    f.write(generate_classes_and_courses())

print("Generated massive_data.sql")
