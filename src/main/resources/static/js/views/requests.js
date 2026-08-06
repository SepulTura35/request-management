const RequestViews = (() => {
    const { el, table, badge, field, select, input } = UI;

    const TYPE_FORMS = {
        LEAVE: {
            fields: () => [
                field('İzin türü', select('leaveType', Labels.options.leaveType, { value: 'ANNUAL' })),
                el('div', { class: 'form-grid two' }, [
                    field('Başlangıç tarihi', input('startDate', 'date', { required: true })),
                    field('Bitiş tarihi', input('endDate', 'date', { required: true }))
                ])
            ],
            collect: (data) => ({
                leaveType: data.leaveType,
                startDate: data.startDate,
                endDate: data.endDate
            }),
            summary: (detail) => [
                ['İzin türü', Labels.leaveType(detail.leaveType)],
                ['Başlangıç', UI.formatDate(detail.startDate)],
                ['Bitiş', UI.formatDate(detail.endDate)],
                ['Toplam gün', `${detail.totalDays} gün`]
            ]
        },

        EXPENSE: {
            fields: () => [
                el('div', { class: 'form-grid two' }, [
                    field('Tutar (TL)', input('amount', 'number', { step: '0.01', min: '0.01', required: true })),
                    field('Masraf kategorisi', select('expenseCategory', Labels.options.expenseCategory, { value: 'TRAVEL' }))
                ]),
                field('Fiş / fatura numarası', input('receiptNumber', 'text', { maxlength: 50 }), { optional: true })
            ],
            collect: (data) => ({
                amount: Number(data.amount),
                expenseCategory: data.expenseCategory,
                receiptNumber: data.receiptNumber || null
            }),
            summary: (detail) => [
                ['Tutar', UI.formatMoney(detail.amount)],
                ['Kategori', Labels.expenseCategory(detail.expenseCategory)],
                ['Fiş numarası', detail.receiptNumber || '—']
            ]
        },

        EQUIPMENT: {
            fields: () => [
                field('Ekipman türü', select('equipmentType', Labels.options.equipmentType, { value: 'LAPTOP' })),
                el('div', { class: 'form-grid two' }, [
                    field('Birim maliyet (TL)', input('estimatedCost', 'number', { step: '0.01', min: '0.01', required: true })),
                    field('Adet', input('quantity', 'number', { min: '1', max: '100', value: '1', required: true }))
                ])
            ],
            collect: (data) => ({
                equipmentType: data.equipmentType,
                estimatedCost: Number(data.estimatedCost),
                quantity: Number(data.quantity)
            }),
            summary: (detail) => [
                ['Ekipman', Labels.equipmentType(detail.equipmentType)],
                ['Birim maliyet', UI.formatMoney(detail.estimatedCost)],
                ['Adet', detail.quantity],
                ['Toplam', UI.formatMoney(detail.estimatedCost * detail.quantity)]
            ]
        },

        REMOTE_WORK: {
            fields: () => [
                el('div', { class: 'form-grid two' }, [
                    field('Başlangıç tarihi', input('startDate', 'date', { required: true })),
                    field('Bitiş tarihi', input('endDate', 'date', { required: true }))
                ]),
                field('Çalışma lokasyonu', input('workLocation', 'text', { maxlength: 150, required: true, placeholder: 'Örn. İzmir / Ev' }))
            ],
            collect: (data) => ({
                startDate: data.startDate,
                endDate: data.endDate,
                workLocation: data.workLocation
            }),
            summary: (detail) => [
                ['Başlangıç', UI.formatDate(detail.startDate)],
                ['Bitiş', UI.formatDate(detail.endDate)],
                ['Lokasyon', detail.workLocation]
            ]
        },

        ACCESS_PERMISSION: {
            fields: () => [
                el('div', { class: 'form-grid two' }, [
                    field('Sistem adı', input('systemName', 'text', { maxlength: 100, required: true, placeholder: 'Örn. Production DB' })),
                    field('Erişim seviyesi', select('accessLevel', Labels.options.accessLevel, { value: 'READ' }))
                ]),
                field('Gerekçe', el('textarea', { name: 'justification', maxlength: 500, required: true }))
            ],
            collect: (data) => ({
                systemName: data.systemName,
                accessLevel: data.accessLevel,
                justification: data.justification
            }),
            summary: (detail) => [
                ['Sistem', detail.systemName],
                ['Erişim seviyesi', Labels.accessLevel(detail.accessLevel)],
                ['Gerekçe', detail.justification]
            ]
        }
    };

    /* ---------- Liste ---------- */

    async function list(ctx) {
        ctx.setTitle('Taleplerim', 'Oluşturduğunuz tüm talepler ve mevcut durumları.');
        ctx.setActions([
            el('button', { class: 'btn btn-primary', text: 'Yeni talep', onclick: () => ctx.go('request-new') })
        ]);

        const state = { page: 0, status: '', requestType: '' };
        const body = el('div', {});
        ctx.render(body);

        const statusFilter = select('status', Labels.options.requestStatus, { placeholder: 'Tüm durumlar' });
        const typeFilter = select('requestType', Labels.options.requestType, { placeholder: 'Tüm tipler' });

        [statusFilter, typeFilter].forEach(control => control.addEventListener('change', () => {
            state.status = statusFilter.value;
            state.requestType = typeFilter.value;
            state.page = 0;
            load();
        }));

        const filters = el('div', { class: 'card' }, [
            el('div', { class: 'card-body' }, [
                el('div', { class: 'filter-bar' }, [
                    field('Durum', statusFilter),
                    field('Talep tipi', typeFilter)
                ])
            ])
        ]);

        const results = el('div', { class: 'card' }, UI.loading());
        body.append(filters, results);

        async function load() {
            UI.clear(results).append(UI.loading());

            const page = await Api.listMyRequests({
                page: state.page, size: 10, status: state.status, requestType: state.requestType
            });

            UI.clear(results);

            if (page.totalElements === 0) {
                results.append(UI.empty('Talep bulunamadı',
                    state.status || state.requestType
                        ? 'Seçtiğiniz filtrelere uyan bir talebiniz yok.'
                        : 'Henüz bir talep oluşturmadınız.'));
                return;
            }

            const rows = page.content.map(request => el('tr', {
                class: 'clickable',
                onclick: () => ctx.go('request-detail', { id: request.id })
            }, [
                el('td', { class: 'num', text: request.requestNumber }),
                el('td', { text: Labels.requestType(request.requestType) }),
                el('td', { class: 'col-secondary', text: request.description }),
                el('td', {}, badge(Labels.requestStatus(request.status), Labels.statusBadge(request.status))),
                el('td', { class: 'col-secondary' }, el('span', {
                    class: `priority priority-${request.priority}`,
                    text: Labels.priority(request.priority)
                })),
                el('td', { class: 'num col-secondary', text: UI.formatDate(request.createdAt) })
            ]));

            results.append(
                table(['Talep no', 'Tip', { label: 'Açıklama', secondary: true }, 'Durum',
                    { label: 'Öncelik', secondary: true }, { label: 'Oluşturma', secondary: true }], rows),
                UI.pager(page, next => { state.page = next; load(); })
            );
        }

        await load();
    }

    /* ---------- Yeni talep ---------- */

    function create(ctx) {
        ctx.setTitle('Yeni talep', 'Talep tipini seçin; form alanları seçtiğiniz tipe göre değişir.');
        ctx.setActions([]);

        const typeSelect = select('requestType', Labels.options.requestType, { value: 'LEAVE' });
        const typeSpecific = el('div', { class: 'form-grid' });

        function renderTypeFields() {
            UI.clear(typeSpecific).append(...TYPE_FORMS[typeSelect.value].fields());
        }

        typeSelect.addEventListener('change', renderTypeFields);
        renderTypeFields();

        const form = el('form', { class: 'form-grid' }, [
            el('div', { class: 'form-grid two' }, [
                field('Talep tipi', typeSelect),
                field('Öncelik', select('priority', Labels.options.priority, { value: 'MEDIUM' }))
            ]),
            field('Açıklama', el('textarea', {
                name: 'description', maxlength: 1000, required: true,
                placeholder: 'Talebin gerekçesini kısaca yazın.'
            })),
            typeSpecific,
            el('div', { class: 'form-foot' }, [
                el('button', { type: 'button', class: 'btn', text: 'Vazgeç', onclick: () => ctx.go('requests') }),
                el('button', { type: 'submit', class: 'btn', text: 'Taslak olarak kaydet' }),
                el('button', { type: 'button', class: 'btn btn-primary', text: 'Kaydet ve onaya gönder', onclick: () => save(true) })
            ])
        ]);

        form.addEventListener('submit', event => {
            event.preventDefault();
            save(false);
        });

        async function save(alsoSubmit) {
            const data = Object.fromEntries(new FormData(form).entries());
            const type = data.requestType;

            const payload = {
                requestType: type,
                description: data.description,
                priority: data.priority,
                ...TYPE_FORMS[type].collect(data)
            };

            try {
                const created = await Api.createRequest(payload);
                if (alsoSubmit) {
                    await Api.submitRequest(created.id);
                    UI.toast(`${created.requestNumber} oluşturuldu ve onaya gönderildi`, 'success');
                } else {
                    UI.toast(`${created.requestNumber} taslak olarak kaydedildi`, 'success');
                }
                ctx.go('request-detail', { id: created.id });
            } catch (error) {
                UI.showFieldErrors(form, error);
            }
        }

        ctx.render(el('div', { class: 'card' }, el('div', { class: 'card-body' }, form)));
    }

    /* ---------- Detay ---------- */

    async function detail(ctx, params) {
        ctx.setTitle('Talep detayı', '');
        ctx.setActions([]);

        const container = el('div', {}, UI.loading());
        ctx.render(container);

        const request = await Api.getRequest(params.id);
        const isOwner = request.requesterId === ctx.user.id;

        ctx.setTitle(`${request.requestNumber} · ${Labels.requestType(request.requestType)}`,
            `${request.requesterName} · ${request.requesterDepartment}`);

        ctx.setActions(ownerActions(ctx, request, isOwner));

        UI.clear(container).append(
            summaryCard(request),
            detailCard(request),
            chainCard(request),
            commentsCard(ctx, request),
            auditCard(request)
        );
    }

    function ownerActions(ctx, request, isOwner) {
        if (!isOwner) return [];
        const actions = [];

        if (request.status === 'DRAFT') {
            actions.push(el('button', {
                class: 'btn btn-primary', text: 'Onaya gönder',
                onclick: async () => {
                    try {
                        await Api.submitRequest(request.id);
                        UI.toast('Talep onaya gönderildi', 'success');
                        ctx.go('request-detail', { id: request.id });
                    } catch (error) {
                        UI.toast(error.message, 'error');
                    }
                }
            }));
        }

        if (request.status === 'DRAFT' || request.status === 'PENDING_APPROVAL') {
            actions.push(el('button', {
                class: 'btn', text: 'İptal et',
                onclick: () => UI.modal({
                    title: 'Talebi iptal et',
                    body: el('p', { text: `${request.requestNumber} numaralı talep iptal edilecek. Bu işlem geri alınamaz.` }),
                    confirmLabel: 'İptal et',
                    confirmVariant: 'btn-danger',
                    onConfirm: async () => {
                        await Api.cancelRequest(request.id);
                        UI.closeModal();
                        UI.toast('Talep iptal edildi', 'success');
                        ctx.go('request-detail', { id: request.id });
                    }
                })
            }));
        }

        if (request.status === 'DRAFT') {
            actions.push(el('button', {
                class: 'btn btn-ghost', text: 'Sil',
                onclick: () => UI.modal({
                    title: 'Taslağı sil',
                    body: el('p', { text: 'Taslak kalıcı olarak silinecek.' }),
                    confirmLabel: 'Sil',
                    confirmVariant: 'btn-danger',
                    onConfirm: async () => {
                        await Api.deleteRequest(request.id);
                        UI.closeModal();
                        UI.toast('Taslak silindi', 'success');
                        ctx.go('requests');
                    }
                })
            }));
        }

        return actions;
    }

    function summaryCard(request) {
        const items = [
            ['Durum', badge(Labels.requestStatus(request.status), Labels.statusBadge(request.status))],
            ['Öncelik', el('span', { class: `priority priority-${request.priority}`, text: Labels.priority(request.priority) })],
            ['Oluşturulma', UI.formatDateTime(request.createdAt)],
            ['Onaya gönderilme', UI.formatDateTime(request.submittedAt)],
            ['Sonuçlanma', UI.formatDateTime(request.resolvedAt)]
        ];

        return el('div', { class: 'card' },
            el('dl', { class: 'summary-list' }, items.map(([label, value]) =>
                el('div', { class: 'summary-item' }, [
                    el('dt', { text: label }),
                    el('dd', {}, typeof value === 'string' ? document.createTextNode(value) : value)
                ]))));
    }

    function detailCard(request) {
        const rows = TYPE_FORMS[request.requestType].summary(request.detail);

        return el('div', { class: 'card' }, [
            el('div', { class: 'card-head' }, el('h3', { text: 'Talep bilgileri' })),
            el('div', { class: 'card-body' }, [
                el('div', { class: 'form-grid' }, [
                    el('div', {}, [
                        el('div', { class: 'muted', text: 'Açıklama' }),
                        el('div', { text: request.description })
                    ])
                ])
            ]),
            el('dl', { class: 'summary-list' }, rows.map(([label, value]) =>
                el('div', { class: 'summary-item' }, [
                    el('dt', { text: label }),
                    el('dd', { text: String(value) })
                ])))
        ]);
    }

    function chainCard(request) {
        const steps = request.approvalSteps || [];

        const body = steps.length === 0
            ? UI.empty('Onay zinciri henüz oluşmadı', 'Talep onaya gönderildiğinde tipine göre zincir kurulur.')
            : el('div', { class: 'chain' }, steps.map(step => {
                const dotClass = step.status === 'APPROVED' ? 'done'
                    : step.status === 'REJECTED' ? 'rejected'
                        : step.status === 'PENDING' && step.approverName ? 'active' : '';

                const mark = step.status === 'APPROVED' ? '✓'
                    : step.status === 'REJECTED' ? '✕' : String(step.stepOrder);

                return el('div', { class: 'chain-step' }, [
                    el('div', { class: 'chain-rail' }, [
                        el('span', { class: `chain-dot ${dotClass}`, text: mark }),
                        el('span', { class: 'chain-line' })
                    ]),
                    el('div', { class: 'chain-body' }, [
                        el('div', { class: 'chain-title' }, [
                            el('strong', { text: Labels.role(step.approverRole) }),
                            badge(Labels.stepStatus(step.status), Labels.stepBadge(step.status))
                        ]),
                        el('div', {
                            class: 'chain-sub',
                            text: step.approverName
                                ? `${step.approverName}${step.actionDate ? ' · ' + UI.formatDateTime(step.actionDate) : ''}`
                                : 'Onaycı, sırası geldiğinde atanacak'
                        }),
                        step.comment ? el('div', { class: 'chain-comment', text: step.comment }) : null
                    ])
                ]);
            }));

        return el('div', { class: 'card' }, [
            el('div', { class: 'card-head' }, [
                el('h3', { text: 'Onay zinciri' }),
                el('span', { class: 'muted', text: `${steps.length} adım` })
            ]),
            el('div', { class: 'card-body tight' }, el('div', { style: 'padding:18px' }, body))
        ]);
    }

    function commentsCard(ctx, request) {
        const listNode = el('div', {}, UI.loading());
        const textarea = el('textarea', { name: 'content', placeholder: 'Yorumunuzu yazın…', maxlength: 1000 });

        const form = el('form', { class: 'comment-form' }, [
            field('Yorum ekle', textarea),
            el('div', { class: 'form-foot' }, el('button', { type: 'submit', class: 'btn btn-primary btn-sm', text: 'Gönder' }))
        ]);

        form.addEventListener('submit', async event => {
            event.preventDefault();
            try {
                await Api.addComment(request.id, textarea.value);
                textarea.value = '';
                await load();
                UI.toast('Yorum eklendi', 'success');
            } catch (error) {
                UI.showFieldErrors(form, error);
            }
        });

        async function load() {
            const comments = await Api.listComments(request.id);
            UI.clear(listNode);

            if (comments.length === 0) {
                listNode.append(UI.empty('Henüz yorum yok', 'Talep sahibi ve onaycılar burada yazışabilir.'));
                return;
            }

            comments.forEach(comment => listNode.append(el('div', { class: 'comment' }, [
                el('span', { class: 'avatar', text: UI.initials(comment.authorName) }),
                el('div', {}, [
                    el('div', { class: 'comment-head' }, [
                        el('strong', { text: comment.authorName }),
                        el('span', { class: 'muted', text: Labels.role(comment.authorRole) }),
                        el('span', { class: 'muted', text: '·' }),
                        el('span', { class: 'muted', text: UI.formatDateTime(comment.createdAt) })
                    ]),
                    el('div', { class: 'comment-body', text: comment.content })
                ])
            ])));
        }

        load();

        return el('div', { class: 'card' }, [
            el('div', { class: 'card-head' }, el('h3', { text: 'Yorumlar' })),
            el('div', { class: 'card-body tight' }, [listNode, form])
        ]);
    }

    function auditCard(request) {
        const body = el('div', {}, UI.loading());

        Api.requestAudit(request.id).then(entries => {
            UI.clear(body);
            if (entries.length === 0) {
                body.append(UI.empty('Kayıt yok'));
                return;
            }
            entries.forEach(entry => body.append(el('div', { class: 'audit-row' }, [
                el('span', { class: 'audit-time', text: UI.formatDateTime(entry.createdAt) }),
                el('span', { text: Labels.auditAction(entry.action) }),
                el('span', { class: 'muted', text: `${entry.userName || 'Sistem'} — ${entry.details || ''}` })
            ])));
        });

        return el('div', { class: 'card' }, [
            el('div', { class: 'card-head' }, [
                el('h3', { text: 'İşlem geçmişi' }),
                el('span', { class: 'muted', text: 'Denetim kaydı' })
            ]),
            el('div', { class: 'card-body tight' }, body)
        ]);
    }

    return { list, create, detail };
})();
