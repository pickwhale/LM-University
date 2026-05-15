SELECT 'duplicate_admin_username' AS check_name, username AS offending_value, COUNT(*) AS total
FROM university.users
GROUP BY username
HAVING COUNT(*) > 1;

SELECT 'duplicate_student_no' AS check_name, studentID AS offending_value, COUNT(*) AS total
FROM university.student
GROUP BY studentID
HAVING COUNT(*) > 1;

SELECT 'admin_student_username_collision' AS check_name, u.username AS offending_value, COUNT(*) AS total
FROM university.users u
JOIN university.student s ON s.studentID = u.username
GROUP BY u.username
HAVING COUNT(*) > 0;

SELECT 'duplicate_university_name' AS check_name, universityName AS offending_value, COUNT(*) AS total
FROM university.universityinformation
GROUP BY universityName
HAVING COUNT(*) > 1;

SELECT 'duplicate_major_code' AS check_name, majorCode AS offending_value, COUNT(*) AS total
FROM university.professionalinformation
GROUP BY majorCode
HAVING COUNT(*) > 1;

SELECT 'duplicate_university_registration_no' AS check_name, registrationNumber AS offending_value, COUNT(*) AS total
FROM university.collegeapplication
GROUP BY registrationNumber
HAVING COUNT(*) > 1;

SELECT 'orphan_university_application_student' AS check_name, ca.id AS offending_row_id, ca.studentID AS offending_value
FROM university.collegeapplication ca
LEFT JOIN university.student s ON s.studentID = ca.studentID
WHERE ca.studentID IS NOT NULL AND s.id IS NULL;

SELECT 'orphan_university_application_university' AS check_name, ca.id AS offending_row_id, ca.universityName AS offending_value
FROM university.collegeapplication ca
LEFT JOIN university.universityinformation u ON u.universityName = ca.universityName
WHERE ca.universityName IS NOT NULL AND u.id IS NULL;

SELECT 'orphan_major_application_student' AS check_name, ma.id AS offending_row_id, ma.studentID AS offending_value
FROM university.professionalregistration ma
LEFT JOIN university.student s ON s.studentID = ma.studentID
WHERE ma.studentID IS NOT NULL AND s.id IS NULL;

SELECT 'orphan_major_application_major' AS check_name, ma.id AS offending_row_id, ma.majorCode AS offending_value
FROM university.professionalregistration ma
LEFT JOIN university.professionalinformation m ON m.majorCode = ma.majorCode
WHERE ma.majorCode IS NOT NULL AND m.id IS NULL;

SELECT 'orphan_admission_result_application' AS check_name, ar.id AS offending_row_id, ar.registrationNumber AS offending_value
FROM university.admissionresults ar
LEFT JOIN university.collegeapplication ca ON ca.registrationNumber = ar.registrationNumber
WHERE ar.registrationNumber IS NOT NULL AND ca.id IS NULL;

SELECT 'orphan_academic_result_student' AS check_name, r.id AS offending_row_id, r.studentID AS offending_value
FROM university.resultsinformation r
LEFT JOIN university.student s ON s.studentID = r.studentID
WHERE r.studentID IS NOT NULL AND s.id IS NULL;
