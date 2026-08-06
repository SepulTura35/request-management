const ApprovalViews = (() => {
    const { el, table, badge, field } = UI;

    async function pending(ctx) {
        ctx.setTitle('Bekleyen onaylarım', 'Sırası size gelmiş ve aksiyon bekleyen talepler.');
        ctx.setActions([]);

        const state = { page: 0 };
        const card = el('div', { class: 'card' }, UI.loading());
        ctx.render(card);

        async function load() {
            UI.clear(card).append(UI.loading());
            const page = await Api.pendingApprovals({ page: state.page, size: 10 });
            UI.clear(card);

            if (page.totalElements === 0) {
                card.append(UI.empty('Bekleyen onay yok', 'Size atanmış ve işlem bekleyen bir talep bulunmuyor.'));
                return;
            }

            const rows = page.content.map(item => el('tr', {}, [
                el('td', { class: 'num' }, el('a', {
                    href: '#', text: item.requestNumber,
                    onclick: event => { event.preventDefault(); ctx.go('request-detail', { id: item.requestId }); }
                })),
                el('td', { text: Labels.requestType(item.requestType) }),
                el('td', { text: item.requesterName }),
                el('td', { text: item.description }),
                el('td', {}, badge(`${item.stepOrder}. adım · ${Labels.role(item.approverRole)}`, 'badge-accent')),
                el('td', {}, el('span', {
                    class: `priority priority-${item.priority}`, text: Labels.priority(item.priority)
                })),
                el('td', { class: 'right' }, el('div', { class: 'row-actions' }, [
                    el('button', {
                        class: 'btn btn-sm btn-primary', text: 'Onayla',
                        onclick: () => openApprove(item, load)
                    }),
                    el('button', {
                        class: 'btn btn-sm', text: 'Reddet',
                        onclick: () => openReject(item, load)
                    })
                ]))
            ]));

            card.append(
                table(
                    ['Talep no', 'Tip', 'Talep sahibi', 'Açıklama', 'Adım', 'Öncelik', { label: 'İşlem', align: 'right' }],
                    rows),
                UI.pager(page, next => { state.page = next; load(); })
            );
        }

        await load();
    }

    function openApprove(item, reload) {
        const comment = el('textarea', { name: 'comment', maxlength: 1000, placeholder: 'Onay notu ekleyebilirsiniz.' });
        const body = el('div', { class: 'form-grid' }, [
            el('p', {
                class: 'muted',
                text: `${item.requestNumber} · ${Labels.requestType(item.requestType)} · ${item.requesterName}`
            }),
            el('p', { text: item.description }),
            field('Onay notu', comment, { optional: true })
        ]);

        UI.modal({
            title: 'Talebi onayla',
            body,
            confirmLabel: 'Onayla',
            onConfirm: async () => {
                try {
                    const result = await Api.approve(item.stepId, comment.value || null);
                    UI.closeModal();
                    UI.toast(result.status === 'APPROVED'
                        ? `${item.requestNumber} tümüyle onaylandı`
                        : `${item.requestNumber} onaylandı, sonraki adıma iletildi`, 'success');
                    await reload();
                    window.dispatchEvent(new CustomEvent('rm:approvals-changed'));
                } catch (error) {
                    UI.showFieldErrors(body, error);
                }
            }
        });
    }

    function openReject(item, reload) {
        const comment = el('textarea', {
            name: 'comment', maxlength: 1000, required: true,
            placeholder: 'Talebin neden reddedildiğini açıklayın.'
        });

        const body = el('div', { class: 'form-grid' }, [
            el('p', {
                class: 'muted',
                text: `${item.requestNumber} · ${Labels.requestType(item.requestType)} · ${item.requesterName}`
            }),
            el('p', { text: 'Reddetme işleminde gerekçe zorunludur. Talep sahibi bu açıklamayı görecek.' }),
            field('Red gerekçesi', comment)
        ]);

        UI.modal({
            title: 'Talebi reddet',
            body,
            confirmLabel: 'Reddet',
            confirmVariant: 'btn-danger',
            onConfirm: async () => {
                try {
                    await Api.reject(item.stepId, comment.value);
                    UI.closeModal();
                    UI.toast(`${item.requestNumber} reddedildi`, 'success');
                    await reload();
                    window.dispatchEvent(new CustomEvent('rm:approvals-changed'));
                } catch (error) {
                    UI.showFieldErrors(body, error);
                }
            }
        });
    }

    async function history(ctx) {
        ctx.setTitle('Onay geçmişim', 'Daha önce sonuçlandırdığınız adımlar.');
        ctx.setActions([]);

        const state = { page: 0 };
        const card = el('div', { class: 'card' }, UI.loading());
        ctx.render(card);

        async function load() {
            UI.clear(card).append(UI.loading());
            const page = await Api.approvalHistory({ page: state.page, size: 15 });
            UI.clear(card);

            if (page.totalElements === 0) {
                card.append(UI.empty('Geçmiş kayıt yok', 'Henüz bir onay adımı sonuçlandırmadınız.'));
                return;
            }

            const rows = page.content.map(item => el('tr', {
                class: 'clickable',
                onclick: () => ctx.go('request-detail', { id: item.requestId })
            }, [
                el('td', { class: 'num', text: item.requestNumber }),
                el('td', { text: Labels.requestType(item.requestType) }),
                el('td', { text: item.requesterName }),
                el('td', {}, badge(Labels.stepStatus(item.stepStatus), Labels.stepBadge(item.stepStatus))),
                el('td', { text: item.comment || '—' }),
                el('td', { class: 'num', text: UI.formatDateTime(item.actionDate) })
            ]));

            card.append(
                table(['Talep no', 'Tip', 'Talep sahibi', 'Kararım', 'Notum', 'Tarih'], rows),
                UI.pager(page, next => { state.page = next; load(); })
            );
        }

        await load();
    }

    return { pending, history };
})();
