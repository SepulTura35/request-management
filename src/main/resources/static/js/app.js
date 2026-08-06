(() => {
    const { el } = UI;

    const loginScreen = document.getElementById('login-screen');
    const appShell = document.getElementById('app-shell');
    const pageTitle = document.getElementById('page-title');
    const pageSubtitle = document.getElementById('page-subtitle');
    const pageActions = document.getElementById('page-actions');
    const pageBody = document.getElementById('page-body');
    const mainNav = document.getElementById('main-nav');
    const titlebarUser = document.getElementById('titlebar-user');

    const APPROVER_ROLES = ['MANAGER', 'HR', 'FINANCE', 'IT', 'DIRECTOR'];

    const ROUTES = {
        'requests': { render: (ctx) => RequestViews.list(ctx) },
        'request-new': { render: (ctx) => RequestViews.create(ctx) },
        'request-detail': { render: (ctx, params) => RequestViews.detail(ctx, params) },
        'approvals': { render: (ctx) => ApprovalViews.pending(ctx) },
        'approval-history': { render: (ctx) => ApprovalViews.history(ctx) },
        'admin-users': { render: (ctx) => AdminViews.users(ctx) },
        'admin-departments': { render: (ctx) => AdminViews.departments(ctx) },
        'admin-audit': { render: (ctx) => AdminViews.audit(ctx) }
    };

    let currentUser = null;
    let currentRoute = null;

    const ctx = {
        get user() { return currentUser; },
        go: (route, params) => navigate(route, params),
        setTitle(title, subtitle = '') {
            pageTitle.textContent = title;
            pageSubtitle.textContent = subtitle;
            pageSubtitle.hidden = !subtitle;
        },
        setActions(nodes) {
            UI.clear(pageActions).append(...nodes);
        },
        render(node) {
            UI.clear(pageBody).append(node);
        }
    };

    /* ---------- Gezinme ---------- */

    function navigationItems() {
        const items = [
            { section: 'Taleplerim' },
            { route: 'requests', label: 'Taleplerim' },
            { route: 'request-new', label: 'Yeni talep' }
        ];

        if (APPROVER_ROLES.includes(currentUser.role)) {
            items.push(
                { section: 'Onay işlemleri' },
                { route: 'approvals', label: 'Bekleyen onaylarım', badge: 'pending' },
                { route: 'approval-history', label: 'Onay geçmişim' }
            );
        }

        if (currentUser.role === 'ADMIN') {
            items.push(
                { section: 'Yönetim' },
                { route: 'admin-users', label: 'Kullanıcılar' },
                { route: 'admin-departments', label: 'Departmanlar' },
                { route: 'admin-audit', label: 'Denetim kaydı' }
            );
        }

        return items;
    }

    function renderNav() {
        UI.clear(mainNav);

        navigationItems().forEach(item => {
            if (item.section) {
                mainNav.append(el('div', { class: 'nav-section', text: item.section }));
                return;
            }

            const button = el('button', {
                class: `nav-item ${currentRoute === item.route ? 'active' : ''}`,
                onclick: () => navigate(item.route)
            }, [el('span', { text: item.label })]);

            if (item.badge === 'pending') button.dataset.pendingBadge = 'true';
            mainNav.append(button);
        });

        refreshPendingCount();
    }

    async function refreshPendingCount() {
        const holder = mainNav.querySelector('[data-pending-badge]');
        if (!holder) return;

        try {
            const page = await Api.pendingApprovals({ page: 0, size: 1 });
            holder.querySelector('.nav-count')?.remove();
            if (page.totalElements > 0) {
                holder.append(el('span', { class: 'nav-count', text: String(page.totalElements) }));
            }
        } catch {
            /* sayaç kritik değil, hata sessizce yutulur */
        }
    }

    async function navigate(route, params = {}) {
        if (!ROUTES[route]) route = 'requests';
        currentRoute = route;
        renderNav();

        UI.clear(pageActions);
        UI.clear(pageBody).append(UI.loading());

        try {
            await ROUTES[route].render(ctx, params);
        } catch (error) {
            ctx.setTitle('Bir sorun oluştu', '');
            ctx.render(el('div', { class: 'card' },
                UI.empty('İstek tamamlanamadı', error.message || 'Bilinmeyen hata')));
        }

        refreshPendingCount();
    }

    /* ---------- Oturum ---------- */

    function applyUser(user) {
        currentUser = user;
        document.getElementById('user-name').textContent = user.fullName;
        document.getElementById('user-role').textContent =
            `${Labels.role(user.role)} · ${user.departmentName}`;
        document.getElementById('user-initials').textContent = UI.initials(user.fullName);

        titlebarUser.hidden = false;
        loginScreen.hidden = true;
        appShell.hidden = false;
    }

    function showLogin() {
        currentUser = null;
        titlebarUser.hidden = true;
        appShell.hidden = true;
        loginScreen.hidden = false;
    }

    const loginForm = document.getElementById('login-form');
    const loginError = document.getElementById('login-error');

    loginForm.addEventListener('submit', async event => {
        event.preventDefault();
        loginError.hidden = true;

        const data = Object.fromEntries(new FormData(loginForm).entries());

        try {
            const auth = await Api.login(data.email, data.password);
            Api.setToken(auth.token);
            applyUser({
                id: auth.userId,
                fullName: auth.fullName,
                role: auth.role,
                departmentName: auth.departmentName
            });
            loginForm.reset();
            await navigate('requests');
        } catch (error) {
            loginError.textContent = error.status === 401
                ? 'E-posta veya parola hatalı.'
                : error.message;
            loginError.hidden = false;
        }
    });

    document.getElementById('logout-button').addEventListener('click', () => {
        Api.clearToken();
        showLogin();
    });

    window.addEventListener('rm:approvals-changed', refreshPendingCount);

    window.addEventListener('rm:unauthorized', () => {
        showLogin();
        UI.toast('Oturumunuz sona erdi, tekrar giriş yapın', 'error');
    });

    document.getElementById('modal-root').addEventListener('click', event => {
        if (event.target.id === 'modal-root') UI.closeModal();
    });

    document.addEventListener('keydown', event => {
        if (event.key === 'Escape') UI.closeModal();
    });

    /* ---------- Başlangıç ---------- */

    (async () => {
        if (!Api.hasToken()) {
            showLogin();
            return;
        }

        try {
            applyUser(await Api.me());
            await navigate('requests');
        } catch {
            Api.clearToken();
            showLogin();
        }
    })();
})();
