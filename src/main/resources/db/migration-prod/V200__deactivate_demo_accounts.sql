UPDATE users
SET active   = FALSE,
    password = '$2a$10$disabled.demo.account.no.usable.hash.value.placeholder..',
    updated_at = CURRENT_TIMESTAMP
WHERE email IN (
    'ahmet.yilmaz@enoca.com',
    'elif.kaya@enoca.com',
    'mert.demir@enoca.com',
    'zeynep.sahin@enoca.com',
    'burak.ozturk@enoca.com',
    'selin.arslan@enoca.com',
    'kerem.dogan@enoca.com',
    'deniz.celik@enoca.com',
    'ayse.koc@enoca.com',
    'emre.aydin@enoca.com',
    'canan.yildirim@enoca.com',
    'admin@enoca.com'
);
