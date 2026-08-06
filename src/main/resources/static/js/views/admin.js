const AdminViews = (() => {
    const { el, table, badge, field, select, input } = UI;

    async function users(ctx) {
        ctx.setTitle('Kullanıcılar', 'Sistemdeki tüm kullanıcılar, rolleri ve durumları.');

        const state = { page: 0 };
        const card = el('div', { class: 'card' }, UI.loading());

        ctx.setActions([
            el('button', { class: 'btn btn-primary', text: 'Yeni kullanıcı', onclick: () => openCreateUser(load) })
        ]);
        ctx.render(card);

        async function load() {
            UI.clear(card).append(UI.loading());
            const page = await Api.listUsers({ page: state.page, size: 15 });
            UI.clear(card);

            const rows = page.content.map(user => el('tr', {}, [
                el('td', {}, el('div', { style: 'display:flex;align-items:center;gap:10px' }, [
                    el('span', { class: 'avatar', text: UI.initials(user.fullName) }),
                    el('div', {}, [
                        el('div', { text: user.fullName }),
                        el('small', { class: 'muted', text: user.email })
                    ])
                ])),
                el('td', {}, badge(Labels.role(user.role), 'badge-accent')),
                el('td', { text: user.departmentName }),
                el('td', {}, user.active
                    ? badge('Aktif', 'badge-approved')
                    : badge('Pasif', 'badge-neutral')),
                el('td', { class: 'right' }, el('div', { class: 'row-actions' }, [
                    el('button', {
                        class: 'btn btn-sm', text: 'Rol değiştir',
                        onclick: () => openChangeRole(user, load)
                    }),
                    el('button', {
                        class: 'btn btn-sm', text: user.active ? 'Pasifleştir' : 'Aktifleştir',
                        onclick: async () => {
                            await Api.setUserActive(user.id, !user.active);
                            UI.toast(`${user.fullName} ${user.active ? 'pasifleştirildi' : 'aktifleştirildi'}`, 'success');
                            await load();
                        }
                    })
                ]))
            ]));

            card.append(
                table(['Kullanıcı', 'Rol', 'Departman', 'Durum', { label: 'İşlem', align: 'right' }], rows),
                UI.pager(page, next => { state.page = next; load(); })
            );
        }

        await load();
    }

    async function openCreateUser(reload) {
        const departments = await Api.listDepartments();
        const departmentOptions = Object.fromEntries(
            departments.map(department => [department.code, `${department.name} (${department.code})`]));

        const body = el('div', { class: 'form-grid' }, [
            el('div', { class: 'form-grid two' }, [
                field('Ad', input('firstName', 'text', { required: true })),
                field('Soyad', input('lastName', 'text', { required: true }))
            ]),
            field('E-posta', input('email', 'email', { required: true })),
            field('Parola', input('password', 'password', { required: true, minlength: 8 })),
            el('div', { class: 'form-grid two' }, [
                field('Rol', select('role', Labels.options.role, { value: 'EMPLOYEE' })),
                field('Departman', select('departmentCode', departmentOptions, { value: departments[0]?.code }))
            ])
        ]);

        UI.modal({
            title: 'Yeni kullanıcı',
            body,
            confirmLabel: 'Oluştur',
            onConfirm: async () => {
                const payload = {};
                body.querySelectorAll('input, select').forEach(control => {
                    payload[control.name] = control.value;
                });

                try {
                    const created = await Api.createUser(payload);
                    UI.closeModal();
                    UI.toast(`${created.fullName} oluşturuldu`, 'success');
                    await reload();
                } catch (error) {
                    UI.showFieldErrors(body, error);
                }
            }
        });
    }

    function openChangeRole(user, reload) {
        const roleSelect = select('role', Labels.options.role, { value: user.role });
        const body = el('div', { class: 'form-grid' }, [
            el('p', { class: 'muted', text: `${user.fullName} · ${user.email}` }),
            field('Yeni rol', roleSelect)
        ]);

        UI.modal({
            title: 'Rol değiştir',
            body,
            confirmLabel: 'Güncelle',
            onConfirm: async () => {
                await Api.updateUserRole(user.id, roleSelect.value);
                UI.closeModal();
                UI.toast('Rol güncellendi', 'success');
                await reload();
            }
        });
    }

    async function departments(ctx) {
        ctx.setTitle('Departmanlar', 'Onay zincirinde yönetici ataması departman üzerinden yapılır.');

        const card = el('div', { class: 'card' }, UI.loading());
        ctx.setActions([
            el('button', { class: 'btn btn-primary', text: 'Yeni departman', onclick: () => openCreateDepartment(load) })
        ]);
        ctx.render(card);

        async function load() {
            UI.clear(card).append(UI.loading());
            const list = await Api.listDepartments();
            UI.clear(card);

            card.append(table(['Departman', 'Kod'], list.map(department => el('tr', {}, [
                el('td', { text: department.name }),
                el('td', { class: 'num', text: department.code })
            ]))));
        }

        await load();
    }

    function openCreateDepartment(reload) {
        const body = el('div', { class: 'form-grid' }, [
            field('Departman adı', input('name', 'text', { required: true, maxlength: 100 })),
            field('Kod', input('code', 'text', {
                required: true, maxlength: 20, placeholder: 'Örn. MKT',
                style: 'text-transform:uppercase'
            }))
        ]);

        UI.modal({
            title: 'Yeni departman',
            body,
            confirmLabel: 'Oluştur',
            onConfirm: async () => {
                const payload = {
                    name: body.querySelector('[name="name"]').value,
                    code: body.querySelector('[name="code"]').value.toLocaleUpperCase('tr')
                };

                try {
                    await Api.createDepartment(payload);
                    UI.closeModal();
                    UI.toast('Departman oluşturuldu', 'success');
                    await reload();
                } catch (error) {
                    UI.showFieldErrors(body, error);
                }
            }
        });
    }

    async function audit(ctx) {
        ctx.setTitle('Denetim kaydı', 'Sistemde yapılan tüm işlemlerin değiştirilemez kaydı.');
        ctx.setActions([]);

        const state = { page: 0 };
        const card = el('div', { class: 'card' }, UI.loading());
        ctx.render(card);

        async function load() {
            UI.clear(card).append(UI.loading());
            const page = await Api.auditLogs({ page: state.page, size: 25 });
            UI.clear(card);

            if (page.totalElements === 0) {
                card.append(UI.empty('Kayıt yok'));
                return;
            }

            const rows = page.content.map(entry => el('tr', {}, [
                el('td', { class: 'num', text: UI.formatDateTime(entry.createdAt) }),
                el('td', { text: Labels.auditAction(entry.action) }),
                el('td', { text: entry.userName || 'Sistem' }),
                el('td', { class: 'num', text: `${entry.entityType}#${entry.entityId}` }),
                el('td', { class: 'muted', text: entry.details || '—' })
            ]));

            card.append(
                table(['Zaman', 'İşlem', 'Kullanıcı', 'Kayıt', 'Ayrıntı'], rows),
                UI.pager(page, next => { state.page = next; load(); })
            );
        }

        await load();
    }

    return { users, departments, audit };
})();
