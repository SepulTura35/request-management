const UI = (() => {

    function el(tag, attrs = {}, children = []) {
        const node = document.createElement(tag);

        Object.entries(attrs).forEach(([key, value]) => {
            if (value === null || value === undefined || value === false) return;
            if (key === 'class') node.className = value;
            else if (key === 'text') node.textContent = value;
            else if (key === 'html') node.innerHTML = value;
            else if (key.startsWith('on')) node.addEventListener(key.slice(2).toLowerCase(), value);
            else if (value === true) node.setAttribute(key, '');
            else node.setAttribute(key, value);
        });

        (Array.isArray(children) ? children : [children])
            .filter(Boolean)
            .forEach(child => node.append(child));

        return node;
    }

    function clear(node) {
        while (node.firstChild) node.removeChild(node.firstChild);
        return node;
    }

    function badge(text, variant) {
        return el('span', { class: `badge ${variant}`, text });
    }

    function initials(fullName) {
        return (fullName || '')
            .split(' ')
            .filter(Boolean)
            .slice(0, 2)
            .map(part => part[0].toLocaleUpperCase('tr'))
            .join('');
    }

    function formatDate(value) {
        if (!value) return '—';
        return new Date(value).toLocaleDateString('tr-TR', { day: '2-digit', month: 'short', year: 'numeric' });
    }

    function formatDateTime(value) {
        if (!value) return '—';
        return new Date(value).toLocaleString('tr-TR', {
            day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit'
        });
    }

    function formatMoney(value) {
        if (value === null || value === undefined) return '—';
        return new Intl.NumberFormat('tr-TR', { style: 'currency', currency: 'TRY' }).format(value);
    }

    function field(labelText, control, { optional = false, error = null } = {}) {
        const label = el('label', { class: error ? 'field invalid' : 'field' }, [
            el('span', {}, [
                document.createTextNode(labelText),
                optional ? el('span', { class: 'optional', text: ' (isteğe bağlı)' }) : null
            ]),
            control,
            error ? el('span', { class: 'field-error', text: error }) : null
        ]);
        label.dataset.field = control.name || '';
        return label;
    }

    function select(name, entries, { value = '', placeholder = null } = {}) {
        const node = el('select', { name });
        if (placeholder) node.append(el('option', { value: '', text: placeholder }));
        Object.entries(entries).forEach(([key, label]) => {
            node.append(el('option', { value: key, text: label, selected: key === value }));
        });
        return node;
    }

    function input(name, type = 'text', attrs = {}) {
        return el('input', { name, type, ...attrs });
    }

    function empty(title, description) {
        return el('div', { class: 'empty' }, [
            el('strong', { text: title }),
            description ? el('span', { text: description }) : null
        ]);
    }

    function loading(text = 'Yükleniyor…') {
        return el('div', { class: 'loading', text });
    }

    function table(headers, rows) {
        const headerCell = (header) => {
            const classes = [header.align === 'right' ? 'right' : null, header.secondary ? 'col-secondary' : null];
            return el('th', {
                class: classes.filter(Boolean).join(' ') || null,
                text: header.label || header
            });
        };

        return el('div', { class: 'table-scroll' }, el('table', { class: 'data-table' }, [
            el('thead', {}, el('tr', {}, headers.map(headerCell))),
            el('tbody', {}, rows)
        ]));
    }

    function pager(page, onChange) {
        if (!page || page.totalPages <= 1) return null;

        return el('div', { class: 'pager' }, [
            el('span', {
                class: 'pager-info',
                text: `${page.totalElements} kayıt · sayfa ${page.page + 1}/${page.totalPages}`
            }),
            el('div', { class: 'pager-controls' }, [
                el('button', {
                    class: 'btn btn-sm', text: 'Önceki', disabled: page.first,
                    onclick: () => onChange(page.page - 1)
                }),
                el('button', {
                    class: 'btn btn-sm', text: 'Sonraki', disabled: page.last,
                    onclick: () => onChange(page.page + 1)
                })
            ])
        ]);
    }

    function toast(message, variant = '') {
        const node = el('div', { class: `toast ${variant ? 'toast-' + variant : ''}`, text: message });
        document.getElementById('toast-root').append(node);
        setTimeout(() => node.remove(), 4200);
    }

    function closeModal() {
        const root = document.getElementById('modal-root');
        clear(root).hidden = true;
    }

    function modal({ title, body, confirmLabel = 'Kaydet', confirmVariant = 'btn-primary', onConfirm }) {
        const root = document.getElementById('modal-root');
        clear(root);

        const confirmButton = el('button', { class: `btn ${confirmVariant}`, text: confirmLabel });

        confirmButton.addEventListener('click', async () => {
            confirmButton.disabled = true;
            try {
                await onConfirm();
            } finally {
                confirmButton.disabled = false;
            }
        });

        root.append(el('div', { class: 'modal' }, [
            el('div', { class: 'modal-head' }, [
                el('h3', { text: title }),
                el('button', { class: 'btn btn-ghost btn-sm', text: 'Kapat', onclick: closeModal })
            ]),
            el('div', { class: 'modal-body' }, body),
            el('div', { class: 'modal-foot' }, [
                el('button', { class: 'btn', text: 'Vazgeç', onclick: closeModal }),
                confirmButton
            ])
        ]));

        root.hidden = false;
    }

    function showFieldErrors(container, error) {
        container.querySelectorAll('.field.invalid').forEach(node => {
            node.classList.remove('invalid');
            node.querySelector('.field-error')?.remove();
        });

        if (!(error instanceof Api.ApiError)) throw error;

        if (error.validationErrors) {
            Object.entries(error.validationErrors).forEach(([name, message]) => {
                const control = container.querySelector(`[name="${name}"]`);
                const wrapper = control?.closest('.field');
                if (!wrapper) return;
                wrapper.classList.add('invalid');
                wrapper.append(el('span', { class: 'field-error', text: message }));
            });
            toast('Formda düzeltilmesi gereken alanlar var', 'error');
        } else {
            toast(error.message, 'error');
        }
    }

    return {
        el, clear, badge, initials, field, select, input, table, pager,
        formatDate, formatDateTime, formatMoney,
        empty, loading, toast, modal, closeModal, showFieldErrors
    };
})();
