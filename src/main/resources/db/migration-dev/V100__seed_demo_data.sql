INSERT INTO departments (name, code, created_at, updated_at) VALUES
    ('Bilgi Teknolojileri', 'IT',   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Insan Kaynaklari',    'HR',   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Finans',              'FIN',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Satis',               'SLS',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Yonetim',             'MGMT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (first_name, last_name, email, password, role, department_id, active, created_at, updated_at) VALUES
    ('Ahmet',  'Yilmaz',   'ahmet.yilmaz@enoca.com',   '$2a$10$83rTHTwPJuGUARBXLksiZuY0NawFAcnH.reFr4uq8TBZsBj3QCd/G', 'MANAGER',  (SELECT id FROM departments WHERE code = 'IT'),   TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Elif',   'Kaya',     'elif.kaya@enoca.com',      '$2a$10$83rTHTwPJuGUARBXLksiZuY0NawFAcnH.reFr4uq8TBZsBj3QCd/G', 'IT',       (SELECT id FROM departments WHERE code = 'IT'),   TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Mert',   'Demir',    'mert.demir@enoca.com',     '$2a$10$83rTHTwPJuGUARBXLksiZuY0NawFAcnH.reFr4uq8TBZsBj3QCd/G', 'EMPLOYEE', (SELECT id FROM departments WHERE code = 'IT'),   TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Zeynep', 'Sahin',    'zeynep.sahin@enoca.com',   '$2a$10$83rTHTwPJuGUARBXLksiZuY0NawFAcnH.reFr4uq8TBZsBj3QCd/G', 'MANAGER',  (SELECT id FROM departments WHERE code = 'HR'),   TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Burak',  'Ozturk',   'burak.ozturk@enoca.com',   '$2a$10$83rTHTwPJuGUARBXLksiZuY0NawFAcnH.reFr4uq8TBZsBj3QCd/G', 'HR',       (SELECT id FROM departments WHERE code = 'HR'),   TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Selin',  'Arslan',   'selin.arslan@enoca.com',   '$2a$10$83rTHTwPJuGUARBXLksiZuY0NawFAcnH.reFr4uq8TBZsBj3QCd/G', 'MANAGER',  (SELECT id FROM departments WHERE code = 'FIN'),  TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Kerem',  'Dogan',    'kerem.dogan@enoca.com',    '$2a$10$83rTHTwPJuGUARBXLksiZuY0NawFAcnH.reFr4uq8TBZsBj3QCd/G', 'FINANCE',  (SELECT id FROM departments WHERE code = 'FIN'),  TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Deniz',  'Celik',    'deniz.celik@enoca.com',    '$2a$10$83rTHTwPJuGUARBXLksiZuY0NawFAcnH.reFr4uq8TBZsBj3QCd/G', 'MANAGER',  (SELECT id FROM departments WHERE code = 'SLS'),  TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Ayse',   'Koc',      'ayse.koc@enoca.com',       '$2a$10$83rTHTwPJuGUARBXLksiZuY0NawFAcnH.reFr4uq8TBZsBj3QCd/G', 'EMPLOYEE', (SELECT id FROM departments WHERE code = 'SLS'),  TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Emre',   'Aydin',    'emre.aydin@enoca.com',     '$2a$10$83rTHTwPJuGUARBXLksiZuY0NawFAcnH.reFr4uq8TBZsBj3QCd/G', 'EMPLOYEE', (SELECT id FROM departments WHERE code = 'SLS'),  TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Canan',  'Yildirim', 'canan.yildirim@enoca.com', '$2a$10$83rTHTwPJuGUARBXLksiZuY0NawFAcnH.reFr4uq8TBZsBj3QCd/G', 'DIRECTOR', (SELECT id FROM departments WHERE code = 'MGMT'), TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Sistem', 'Yoneticisi', 'admin@enoca.com',        '$2a$10$83rTHTwPJuGUARBXLksiZuY0NawFAcnH.reFr4uq8TBZsBj3QCd/G', 'ADMIN',    (SELECT id FROM departments WHERE code = 'MGMT'), TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
