const Api = (() => {
    const TOKEN_KEY = 'rm.token';
    const BASE = '/api';

    let token = localStorage.getItem(TOKEN_KEY);

    class ApiError extends Error {
        constructor(status, payload) {
            super(payload?.message || 'Beklenmeyen bir hata olustu');
            this.status = status;
            this.validationErrors = payload?.validationErrors || null;
        }
    }

    async function request(method, path, body, query) {
        const url = new URL(BASE + path, window.location.origin);
        if (query) {
            Object.entries(query)
                .filter(([, value]) => value !== null && value !== undefined && value !== '')
                .forEach(([key, value]) => url.searchParams.set(key, value));
        }

        const headers = {};
        if (body !== undefined) headers['Content-Type'] = 'application/json';
        if (token) headers['Authorization'] = `Bearer ${token}`;

        const response = await fetch(url, {
            method,
            headers,
            body: body === undefined ? undefined : JSON.stringify(body)
        });

        if (response.status === 401 && token) {
            clearToken();
            window.dispatchEvent(new CustomEvent('rm:unauthorized'));
        }

        if (response.status === 204) return null;

        const text = await response.text();
        const payload = text ? JSON.parse(text) : null;

        if (!response.ok) throw new ApiError(response.status, payload);
        return payload;
    }

    function setToken(value) {
        token = value;
        localStorage.setItem(TOKEN_KEY, value);
    }

    function clearToken() {
        token = null;
        localStorage.removeItem(TOKEN_KEY);
    }

    return {
        ApiError,
        hasToken: () => Boolean(token),
        setToken,
        clearToken,

        login: (email, password) => request('POST', '/auth/login', { email, password }),
        me: () => request('GET', '/users/me'),

        listMyRequests: (params) => request('GET', '/requests', undefined, params),
        getRequest: (id) => request('GET', `/requests/${id}`),
        createRequest: (payload) => request('POST', '/requests', payload),
        updateRequest: (id, payload) => request('PUT', `/requests/${id}`, payload),
        submitRequest: (id) => request('POST', `/requests/${id}/submit`, {}),
        cancelRequest: (id) => request('POST', `/requests/${id}/cancel`, {}),
        deleteRequest: (id) => request('DELETE', `/requests/${id}`),

        listComments: (id) => request('GET', `/requests/${id}/comments`),
        addComment: (id, content) => request('POST', `/requests/${id}/comments`, { content }),
        requestAudit: (id) => request('GET', `/requests/${id}/audit`),

        pendingApprovals: (params) => request('GET', '/approvals/pending', undefined, params),
        approvalHistory: (params) => request('GET', '/approvals/history', undefined, params),
        approve: (stepId, comment) => request('POST', `/approvals/${stepId}/approve`, { comment }),
        reject: (stepId, comment) => request('POST', `/approvals/${stepId}/reject`, { comment }),

        listUsers: (params) => request('GET', '/admin/users', undefined, params),
        createUser: (payload) => request('POST', '/admin/users', payload),
        updateUserRole: (id, role) => request('PATCH', `/admin/users/${id}/role`, { role }),
        setUserActive: (id, active) => request('PATCH', `/admin/users/${id}/status`, undefined, { active }),
        listDepartments: () => request('GET', '/admin/departments'),
        createDepartment: (payload) => request('POST', '/admin/departments', payload),
        auditLogs: (params) => request('GET', '/admin/audit-logs', undefined, params)
    };
})();
