const Labels = (() => {
    const requestType = {
        LEAVE: 'İzin',
        EXPENSE: 'Masraf',
        EQUIPMENT: 'Ekipman',
        REMOTE_WORK: 'Uzaktan Çalışma',
        ACCESS_PERMISSION: 'Erişim Yetkisi'
    };

    const requestStatus = {
        DRAFT: 'Taslak',
        PENDING_APPROVAL: 'Onay Bekliyor',
        APPROVED: 'Onaylandı',
        REJECTED: 'Reddedildi',
        CANCELLED: 'İptal Edildi'
    };

    const stepStatus = {
        PENDING: 'Bekliyor',
        APPROVED: 'Onaylandı',
        REJECTED: 'Reddedildi',
        CANCELLED: 'Ulaşılmadı'
    };

    const role = {
        EMPLOYEE: 'Çalışan',
        MANAGER: 'Yönetici',
        HR: 'İnsan Kaynakları',
        FINANCE: 'Finans',
        IT: 'Bilgi Teknolojileri',
        DIRECTOR: 'Direktör',
        ADMIN: 'Sistem Yöneticisi'
    };

    const priority = { LOW: 'Düşük', MEDIUM: 'Orta', HIGH: 'Yüksek', URGENT: 'Acil' };

    const leaveType = {
        ANNUAL: 'Yıllık izin',
        SICK: 'Hastalık izni',
        UNPAID: 'Ücretsiz izin',
        MATERNITY_PATERNITY: 'Doğum izni',
        OTHER: 'Diğer'
    };

    const expenseCategory = {
        TRAVEL: 'Ulaşım',
        MEALS: 'Yemek',
        ACCOMMODATION: 'Konaklama',
        OFFICE_SUPPLIES: 'Ofis malzemesi',
        TRAINING: 'Eğitim',
        OTHER: 'Diğer'
    };

    const equipmentType = {
        LAPTOP: 'Dizüstü bilgisayar',
        MONITOR: 'Monitör',
        PHONE: 'Telefon',
        PERIPHERAL: 'Çevre birimi',
        SOFTWARE_LICENSE: 'Yazılım lisansı',
        OTHER: 'Diğer'
    };

    const accessLevel = { READ: 'Okuma', WRITE: 'Yazma', ADMIN: 'Yönetici' };

    const auditAction = {
        USER_REGISTERED: 'Kullanıcı kaydı',
        REQUEST_CREATED: 'Talep oluşturuldu',
        REQUEST_UPDATED: 'Talep güncellendi',
        REQUEST_SUBMITTED: 'Onaya gönderildi',
        REQUEST_CANCELLED: 'Talep iptal edildi',
        REQUEST_DELETED: 'Talep silindi',
        APPROVAL_APPROVED: 'Adım onaylandı',
        APPROVAL_REJECTED: 'Adım reddedildi',
        COMMENT_ADDED: 'Yorum eklendi'
    };

    const statusBadge = {
        DRAFT: 'badge-neutral',
        PENDING_APPROVAL: 'badge-pending',
        APPROVED: 'badge-approved',
        REJECTED: 'badge-rejected',
        CANCELLED: 'badge-neutral'
    };

    const stepBadge = {
        PENDING: 'badge-pending',
        APPROVED: 'badge-approved',
        REJECTED: 'badge-rejected',
        CANCELLED: 'badge-neutral'
    };

    const of = (map) => (key) => map[key] || key || '';

    return {
        requestType: of(requestType),
        requestStatus: of(requestStatus),
        stepStatus: of(stepStatus),
        role: of(role),
        priority: of(priority),
        leaveType: of(leaveType),
        expenseCategory: of(expenseCategory),
        equipmentType: of(equipmentType),
        accessLevel: of(accessLevel),
        auditAction: of(auditAction),
        statusBadge: of(statusBadge),
        stepBadge: of(stepBadge),
        options: {
            requestType, requestStatus, role, priority,
            leaveType, expenseCategory, equipmentType, accessLevel
        }
    };
})();
