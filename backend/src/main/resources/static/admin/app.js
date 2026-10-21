/**
 * FPT Schools - Cổng Quản Trị Hệ Thống (Admin Portal)
 * Vanilla JavaScript Single Page Application Client
 */

// Global State
const AUTH_TOKEN_KEY = 'fschool_admin_jwt';
const AUTH_USER_KEY = 'fschool_admin_user';

let currentTab = 'dashboard';
let cachedUsers = [];
let cachedCourses = [];
let cachedClasses = [];
let cachedRequests = [];

// ==========================================
// TOAST NOTIFICATIONS
// ==========================================
const TOAST_ICONS = {
    success: '✅',
    error: '❌',
    warning: '⚠️',
    info: 'ℹ️'
};

function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    const icon = TOAST_ICONS[type] ?? 'ℹ️';

    toast.innerHTML = `<span>${icon}</span><span>${escapeHtml(message)}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = 'toastSlideIn 0.3s reverse forwards';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

// ==========================================
// MODAL MANAGEMENT
// ==========================================
function openModal(id) {
    const modal = document.getElementById(id);
    if (modal) {
        modal.classList.add('show');
        if (id === 'modal-create-class') {
            populateCourseSelect();
        }
    }
}

function closeModal(id) {
    const modal = document.getElementById(id);
    if (modal) {
        modal.classList.remove('show');
        const form = modal.querySelector('form');
        if (form) form.reset();
    }
}

document.addEventListener('click', (e) => {
    if (e.target.classList.contains('modal-backdrop')) {
        e.target.classList.remove('show');
        const form = e.target.querySelector('form');
        if (form) form.reset();
    }
});

// ==========================================
// USER DROPDOWN MANAGEMENT (TOP HEADER)
// ==========================================
function toggleUserDropdown() {
    const menu = document.getElementById('user-dropdown-menu');
    const btn = document.getElementById('user-profile-btn');
    if (!menu || !btn) return;

    const isExpanded = menu.classList.contains('show');
    if (isExpanded) {
        closeUserDropdown();
    } else {
        menu.classList.add('show');
        btn.setAttribute('aria-expanded', 'true');
    }
}

function closeUserDropdown() {
    const menu = document.getElementById('user-dropdown-menu');
    const btn = document.getElementById('user-profile-btn');
    if (menu) menu.classList.remove('show');
    if (btn) btn.setAttribute('aria-expanded', 'false');
}

document.addEventListener('click', (e) => {
    const container = document.querySelector('.user-profile-menu-container');
    if (container && !container.contains(e.target)) {
        closeUserDropdown();
    }
});

// ==========================================
// API CLIENT WITH BEARER TOKEN & ERROR HANDLING
// ==========================================
async function apiCall(endpoint, options = {}) {
    const token = localStorage.getItem(AUTH_TOKEN_KEY);
    const headers = options.headers || {};

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    if (!(options.body instanceof FormData) && !headers['Content-Type']) {
        headers['Content-Type'] = 'application/json';
    }

    const response = await fetch(endpoint, {
        ...options,
        headers
    });

    if (response.status === 401 || response.status === 403) {
        showToast('Phiên đăng nhập đã hết hạn hoặc bạn không có quyền ADMIN.', 'error');
        handleLogout();
        throw new Error('Unauthorized');
    }

    if (!response.ok) {
        let errorDetail;
        try {
            const errJson = await response.json();
            errorDetail = errJson.detail || errJson.message || JSON.stringify(errJson);
        } catch {
            errorDetail = await response.text();
        }
        throw new Error(errorDetail || `HTTP Error ${response.status}`);
    }

    const contentType = response.headers.get('content-type');
    if (contentType?.includes('application/json')) {
        return await response.json();
    }
    return await response.text();
}

// ==========================================
// AUTHENTICATION & SESSION
// ==========================================
function updateCurrentUserUI(userJson) {
    if (!userJson) return;
    try {
        const user = JSON.parse(userJson);
        const nameEl = document.getElementById('current-user-name');
        const roleEl = document.getElementById('current-user-role');
        const avatarEl = document.getElementById('current-user-avatar');
        const dropdownEmailEl = document.getElementById('dropdown-user-email');

        if (nameEl) nameEl.textContent = user.fullName || user.email || 'Quản Trị Viên';
        if (roleEl) roleEl.textContent = user.role === 'ADMIN' ? 'Admin' : (user.role || 'Admin');
        if (avatarEl) avatarEl.textContent = (user.fullName || 'A').charAt(0).toUpperCase();
        if (dropdownEmailEl) dropdownEmailEl.textContent = user.email || 'admin@fpt.edu.vn';
    } catch (err) {
        console.error('Error parsing user JSON:', err);
    }
}

function checkSession() {
    const token = localStorage.getItem(AUTH_TOKEN_KEY);
    const overlay = document.getElementById('login-overlay');

    if (!token) {
        if (overlay) overlay.style.display = 'flex';
        return;
    }

    if (overlay) overlay.style.display = 'none';
    updateCurrentUserUI(localStorage.getItem(AUTH_USER_KEY));
    void loadDashboardStats();
}

async function handleLogin(e) {
    e.preventDefault();
    const emailInput = document.getElementById('login-email');
    const passwordInput = document.getElementById('login-password');

    const email = emailInput?.value.trim() ?? '';
    const password = passwordInput?.value ?? '';

    try {
        const res = await fetch('/api/v1/auth/login', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({identifier: email, email, password})
        });

        if (!res.ok) {
            let errText = 'Đăng nhập không thành công';
            try {
                const errJson = await res.json();
                errText = errJson.detail || errJson.message || errText;
            } catch {
                // fallback default message
            }
            showToast(errText, 'error');
            return;
        }

        const data = await res.json();
        if (!data.accessToken) {
            showToast('Phản hồi đăng nhập không hợp lệ.', 'error');
            return;
        }

        if (data.role && data.role !== 'ADMIN' && data.role !== 'ROLE_ADMIN') {
            showToast('Tài khoản không có quyền Quản trị viên (ADMIN)!', 'warning');
            return;
        }

        localStorage.setItem(AUTH_TOKEN_KEY, data.accessToken);
        localStorage.setItem(AUTH_USER_KEY, JSON.stringify({
            email: data.email || email,
            fullName: data.fullName || 'Quản Trị Viên',
            role: data.role || 'ADMIN'
        }));

        showToast('Đăng nhập Quản trị viên thành công!', 'success');
        checkSession();
    } catch (err) {
        showToast('Lỗi kết nối máy chủ: ' + err.message, 'error');
    }
}

function handleLogout() {
    closeUserDropdown();
    localStorage.removeItem(AUTH_TOKEN_KEY);
    localStorage.removeItem(AUTH_USER_KEY);
    const overlay = document.getElementById('login-overlay');
    if (overlay) overlay.style.display = 'flex';
    showToast('Đã đăng xuất khỏi cổng Quản trị.', 'info');
}

// ==========================================
// NAVIGATION & TAB SWITCHING
// ==========================================
function switchTab(tabId) {
    currentTab = tabId;

    document.querySelectorAll('.sidebar-nav .nav-item').forEach(item => {
        if (item.dataset.tab === tabId) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });

    document.querySelectorAll('.tab-pane').forEach(pane => {
        pane.classList.remove('active');
    });

    const activePane = document.getElementById(`tab-${tabId}`);
    if (activePane) {
        activePane.classList.add('active');
    }

    const pageTitle = document.getElementById('page-title');
    const titles = {
        dashboard: 'Tổng quan hệ thống',
        users: 'Quản lý Người dùng & Tài khoản',
        courses: 'Quản lý Khóa học & Môn học',
        classes: 'Quản lý Lớp học',
        schedules: 'Xếp Lịch Học & Lịch Thi',
        requests: 'Xử lý Đơn từ Học sinh',
        import: 'Nạp Dữ liệu Hàng loạt (Bulk CSV Import)'
    };
    if (pageTitle && titles[tabId]) {
        pageTitle.textContent = titles[tabId];
    }

    switch (tabId) {
        case 'dashboard':
            void loadDashboardStats();
            break;
        case 'users':
            void loadUsers();
            break;
        case 'courses':
            void loadCourses();
            break;
        case 'classes':
        case 'schedules':
            void loadClasses();
            break;
        case 'requests':
            void loadRequests();
            break;
        default:
            break;
    }
}

// ==========================================
// TAB 1: DASHBOARD STATS & ANALYTICS WIDGETS
// ==========================================
function applySettledArray(result, onValue) {
    if (result.status === 'fulfilled' && Array.isArray(result.value)) {
        onValue(result.value);
    }
}

function updateElementText(id, text) {
    const el = document.getElementById(id);
    if (el) el.textContent = text;
}

function updateHeaderNotificationBadge(requests) {
    const pendingCount = requests.filter(r => r.status === 'PENDING').length;
    const notifBadge = document.getElementById('header-notif-badge');
    if (notifBadge) {
        notifBadge.textContent = String(pendingCount);
        notifBadge.style.display = pendingCount > 0 ? 'flex' : 'none';
    }
}

async function loadDashboardStats() {
    try {
        const [users, courses, classes, requests] = await Promise.allSettled([
            apiCall('/api/v1/admin/users?page=0&size=50'),
            apiCall('/api/v1/admin/academic/courses'),
            apiCall('/api/v1/admin/academic/classes'),
            apiCall('/api/v1/admin/academic/requests')
        ]);

        applySettledArray(users, val => {
            cachedUsers = val;
            updateElementText('stat-users-count', String(val.length));
        });

        applySettledArray(courses, val => {
            cachedCourses = val;
            updateElementText('stat-courses-count', String(val.length));
        });

        applySettledArray(classes, val => {
            cachedClasses = val;
            updateElementText('stat-classes-count', String(val.length));
        });

        applySettledArray(requests, val => {
            cachedRequests = val;
            updateElementText('stat-requests-count', String(val.length));
            updateHeaderNotificationBadge(val);
        });

        renderDashboardRecentRequests();
        renderDonutChart(cachedUsers);
        renderOperationsProgress(cachedRequests, cachedUsers, cachedClasses);
    } catch (err) {
        console.error('Error loading dashboard stats:', err);
    }
}

function matchesRecentRequestQuery(r, query) {
    if (!query) return true;
    return Boolean(
        r.studentName?.toLowerCase().includes(query) ||
        r.title?.toLowerCase().includes(query) ||
        r.studentRollNumber?.toLowerCase().includes(query) ||
        r.requestType?.toLowerCase().includes(query)
    );
}

function getRecentRequestStatusBadge(status) {
    if (status === 'APPROVED') {
        return { statusClass: 'status-delivered', statusLabel: 'Delivered' };
    }
    if (status === 'REJECTED') {
        return { statusClass: 'status-canceled', statusLabel: 'Canceled' };
    }
    return { statusClass: 'status-pending', statusLabel: 'Pending' };
}

function renderRecentRequestRow(r) {
    const { statusClass, statusLabel } = getRecentRequestStatusBadge(r.status);
    const dateStr = r.createdAt ? new Date(r.createdAt).toLocaleDateString('vi-VN') : '---';
    const actionBtn = r.status === 'PENDING'
        ? `<button class="btn btn-primary btn-sm" onclick="openProcessModal(${r.requestId})">Duyệt</button>`
        : `<span style="font-size: 11px; color: var(--text-muted);">Hoàn tất</span>`;

    return `
        <tr>
            <td style="font-weight: 700; color: var(--primary);">#${r.requestId}</td>
            <td>
                <div style="font-weight: 600;">${escapeHtml(r.studentName || 'Học sinh')}</div>
                <div style="font-size: 11px; color: var(--text-muted);">${escapeHtml(r.studentRollNumber || '')}</div>
            </td>
            <td><span class="badge badge-info">${escapeHtml(r.requestType || 'Đơn từ')}</span></td>
            <td style="max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                ${escapeHtml(r.title || '---')}
            </td>
            <td>${dateStr}</td>
            <td><span class="${statusClass}">${statusLabel}</span></td>
            <td style="text-align: right;">${actionBtn}</td>
        </tr>
    `;
}

function renderDashboardRecentRequests() {
    const tbody = document.getElementById('dashboard-requests-tbody');
    if (!tbody) return;

    const query = document.getElementById('dashboard-requests-search')?.value.toLowerCase().trim() || '';
    const statusFilter = document.getElementById('dashboard-requests-filter')?.value || 'ALL';

    const filtered = cachedRequests.filter(r => {
        const matchesStatus = (statusFilter === 'ALL' || r.status === statusFilter);
        return matchesStatus && matchesRecentRequestQuery(r, query);
    });

    if (filtered.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted" style="padding: 24px;">Không có đơn từ nào phù hợp điều kiện lọc.</td></tr>';
        return;
    }

    // Display top 8 recent requests
    tbody.innerHTML = filtered.slice(0, 8).map(renderRecentRequestRow).join('');
}

// Render SVG Donut Chart for User Roles
function renderDonutChart(users) {
    const total = users.length;
    const totalEl = document.getElementById('donut-total-count');
    if (totalEl) totalEl.textContent = String(total);

    const studentCount = users.filter(u => u.role === 'STUDENT').length;
    const lecturerCount = users.filter(u => u.role === 'LECTURER').length;
    const parentCount = users.filter(u => u.role === 'PARENT').length;
    const adminCount = users.filter(u => u.role === 'ADMIN').length;

    const setLegend = (id, count) => {
        const el = document.getElementById(id);
        if (el) el.textContent = String(count);
    };

    setLegend('legend-student-count', studentCount);
    setLegend('legend-lecturer-count', lecturerCount);
    setLegend('legend-parent-count', parentCount);
    setLegend('legend-admin-count', adminCount);

    const circumference = 2 * Math.PI * 58; // radius = 58 => C ≈ 364.42
    if (total === 0) return;

    const studentLen = (studentCount / total) * circumference;
    const lecturerLen = (lecturerCount / total) * circumference;
    const parentLen = (parentCount / total) * circumference;
    const adminLen = (adminCount / total) * circumference;

    const updateSeg = (id, strokeLen, offset) => {
        const seg = document.getElementById(id);
        if (seg) {
            seg.style.strokeDasharray = `${strokeLen} ${circumference - strokeLen}`;
            seg.style.strokeDashoffset = String(offset);
        }
    };

    let offset = 0;
    updateSeg('donut-seg-student', studentLen, offset);
    offset -= studentLen;
    updateSeg('donut-seg-lecturer', lecturerLen, offset);
    offset -= lecturerLen;
    updateSeg('donut-seg-parent', parentLen, offset);
    offset -= parentLen;
    updateSeg('donut-seg-admin', adminLen, offset);
}

// Render Operations Progress Bars
function renderOperationsProgress(requests, users, classes) {
    // 1. Requests processed %
    const totalReq = requests.length;
    const processedReq = requests.filter(r => r.status === 'APPROVED' || r.status === 'REJECTED').length;
    const reqPct = totalReq > 0 ? Math.round((processedReq / totalReq) * 100) : 100;

    const reqPctEl = document.getElementById('progress-requests-pct');
    const reqFillEl = document.getElementById('progress-requests-fill');
    if (reqPctEl) reqPctEl.textContent = `${reqPct}%`;
    if (reqFillEl) reqFillEl.style.width = `${reqPct}%`;

    // 2. Active users %
    const totalUsers = users.length;
    const activeUsers = users.filter(u => u.status === 'ACTIVE').length;
    const activePct = totalUsers > 0 ? Math.round((activeUsers / totalUsers) * 100) : 100;

    const activePctEl = document.getElementById('progress-active-pct');
    const activeFillEl = document.getElementById('progress-active-fill');
    if (activePctEl) activePctEl.textContent = `${activePct}%`;
    if (activeFillEl) activeFillEl.style.width = `${activePct}%`;

    // 3. Classes with assigned lecturer %
    const totalClasses = classes.length;
    const assignedClasses = classes.filter(c => c.lecturerName && c.lecturerName.trim() !== '').length;
    const classPct = totalClasses > 0 ? Math.round((assignedClasses / totalClasses) * 100) : 100;

    const classPctEl = document.getElementById('progress-classes-pct');
    const classFillEl = document.getElementById('progress-classes-fill');
    if (classPctEl) classPctEl.textContent = `${classPct}%`;
    if (classFillEl) classFillEl.style.width = `${classPct}%`;
}

// ==========================================
// TAB 2: USER MANAGEMENT (UC-ADM-01, 02, 03)
// ==========================================
async function loadUsers() {
    const tbody = document.getElementById('users-table-body');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">Đang tải dữ liệu người dùng...</td></tr>';

    try {
        const users = await apiCall('/api/v1/admin/users?page=0&size=50');
        cachedUsers = Array.isArray(users) ? users : [];
        renderUsersTable(cachedUsers);
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="8" class="text-center text-danger">Lỗi tải người dùng: ${escapeHtml(err.message)}</td></tr>`;
    }
}

function renderUsersTable(users) {
    const tbody = document.getElementById('users-table-body');
    if (!tbody) return;

    if (!users || users.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">Không tìm thấy người dùng nào.</td></tr>';
        return;
    }

    tbody.innerHTML = users.map(u => {
        const isAct = u.status === 'ACTIVE';
        const statusSpan = isAct
            ? '<span class="status-active">ACTIVE</span>'
            : '<span class="status-inactive">INACTIVE</span>';

        const nextStatus = isAct ? 'INACTIVE' : 'ACTIVE';
        const actionBtnClass = isAct ? 'btn-secondary' : 'btn-primary';
        const actionLabel = isAct ? 'Khóa' : 'Kích hoạt';

        return `
            <tr>
                <td style="font-weight: 700; color: var(--primary);">#${u.userId}</td>
                <td style="font-weight: 600;">${escapeHtml(u.fullName)}</td>
                <td>${escapeHtml(u.email)}</td>
                <td><span class="badge badge-info">${escapeHtml(u.role)}</span></td>
                <td>${escapeHtml(u.rollNumber || '---')}</td>
                <td>${escapeHtml(u.phoneNumber || '---')}</td>
                <td>${statusSpan}</td>
                <td style="text-align: right;">
                    <button class="btn btn-sm ${actionBtnClass}" onclick="toggleUserStatus(${u.userId}, '${nextStatus}')">
                        ${actionLabel}
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

async function toggleUserStatus(userId, newStatus) {
    try {
        await apiCall(`/api/v1/admin/users/${userId}/status`, {
            method: 'PATCH',
            body: JSON.stringify({status: newStatus})
        });
        showToast(`Đã chuyển trạng thái tài khoản #${userId} thành ${newStatus}`, 'success');
        await loadUsers();
    } catch (err) {
        showToast('Lỗi cập nhật trạng thái: ' + err.message, 'error');
    }
}

async function handleCreateUser(e) {
    e.preventDefault();
    const fullName = document.getElementById('user-fullname').value.trim();
    const email = document.getElementById('user-email').value.trim();
    const password = document.getElementById('user-password').value;
    const role = document.getElementById('user-role').value;
    const phoneNumber = document.getElementById('user-phone').value.trim();
    const rollNumber = document.getElementById('user-rollnumber').value.trim();

    const payload = {
        fullName,
        email,
        password,
        role,
        phoneNumber: phoneNumber || null,
        rollNumber: rollNumber || null
    };

    try {
        await apiCall('/api/v1/admin/users', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        showToast('Tạo tài khoản người dùng thành công!', 'success');
        closeModal('modal-create-user');
        await loadUsers();
    } catch (err) {
        showToast('Lỗi tạo người dùng: ' + err.message, 'error');
    }
}

// ==========================================
// TAB 3: COURSE MANAGEMENT (UC-ADM-04)
// ==========================================
async function loadCourses() {
    const tbody = document.getElementById('courses-table-body');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Đang tải danh sách khóa học...</td></tr>';

    try {
        const courses = await apiCall('/api/v1/admin/academic/courses');
        cachedCourses = Array.isArray(courses) ? courses : [];

        if (cachedCourses.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Chưa có khóa học nào trong hệ thống.</td></tr>';
            return;
        }

        tbody.innerHTML = cachedCourses.map(c => `
            <tr>
                <td style="font-weight: 700; color: var(--primary);">#${c.courseId}</td>
                <td style="font-weight: 700;">${escapeHtml(c.courseCode)}</td>
                <td style="font-weight: 600;">${escapeHtml(c.courseName)}</td>
                <td><span class="badge badge-info">${c.credits || 3} Tín chỉ</span></td>
                <td style="color: var(--text-muted);">${escapeHtml(c.description || '---')}</td>
            </tr>
        `).join('');
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center text-danger">Lỗi tải môn học: ${escapeHtml(err.message)}</td></tr>`;
    }
}

async function handleCreateCourse(e) {
    e.preventDefault();
    const courseCode = document.getElementById('course-code').value.trim();
    const courseName = document.getElementById('course-name').value.trim();
    const credits = Number.parseInt(document.getElementById('course-credits').value, 10) || 3;
    const description = document.getElementById('course-desc').value.trim();

    try {
        await apiCall('/api/v1/admin/academic/courses', {
            method: 'POST',
            body: JSON.stringify({courseCode, courseName, credits, description})
        });
        showToast('Tạo khóa học / môn học thành công!', 'success');
        closeModal('modal-create-course');
        await loadCourses();
    } catch (err) {
        showToast('Lỗi tạo môn học: ' + err.message, 'error');
    }
}

function populateCourseSelect() {
    const select = document.getElementById('class-course-id');
    if (!select) return;
    select.innerHTML = '<option value="">-- Chọn môn học --</option>' +
        cachedCourses.map(c => `<option value="${c.courseId}">${escapeHtml(c.courseCode)} - ${escapeHtml(c.courseName)}</option>`).join('');
}

// ==========================================
// TAB 4: CLASS MANAGEMENT (UC-ADM-05)
// ==========================================
async function loadClasses() {
    const tbody = document.getElementById('classes-table-body');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Đang tải danh sách lớp học...</td></tr>';

    try {
        const classes = await apiCall('/api/v1/admin/academic/classes');
        cachedClasses = Array.isArray(classes) ? classes : [];

        if (cachedClasses.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Chưa có lớp học nào.</td></tr>';
            return;
        }

        tbody.innerHTML = cachedClasses.map(c => `
            <tr>
                <td style="font-weight: 700; color: var(--primary);">#${c.classId}</td>
                <td style="font-weight: 700;">${escapeHtml(c.classCode)}</td>
                <td>${escapeHtml(c.courseName || c.courseCode || '---')}</td>
                <td>${escapeHtml(c.lecturerName || 'Chưa phân công')}</td>
                <td><span class="badge badge-warning">${escapeHtml(c.semester || '---')}</span></td>
            </tr>
        `).join('');
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center text-danger">Lỗi tải lớp học: ${escapeHtml(err.message)}</td></tr>`;
    }
}

async function handleCreateClass(e) {
    e.preventDefault();
    const classCode = document.getElementById('class-code').value.trim();
    const courseId = Number.parseInt(document.getElementById('class-course-id').value, 10);
    const lecturerId = Number.parseInt(document.getElementById('class-teacher-id').value, 10);
    const semester = document.getElementById('class-semester').value.trim();

    try {
        await apiCall('/api/v1/admin/academic/classes', {
            method: 'POST',
            body: JSON.stringify({
                classCode,
                courseId,
                lecturerId: Number.isNaN(lecturerId) ? null : lecturerId,
                semester,
                academicYear: 2026,
                roomDefault: 'Campus Hoa Lac'
            })
        });
        showToast('Tạo lớp học mới thành công!', 'success');
        closeModal('modal-create-class');
        await loadClasses();
    } catch (err) {
        showToast('Lỗi tạo lớp: ' + err.message, 'error');
    }
}

// ==========================================
// TAB 5: SCHEDULES (UC-ADM-06)
// ==========================================
const slotTimes = {
    1: {start: '07:30:00', end: '09:50:00'},
    2: {start: '10:00:00', end: '12:20:00'},
    3: {start: '12:50:00', end: '15:10:00'},
    4: {start: '15:20:00', end: '17:40:00'},
    5: {start: '17:50:00', end: '20:10:00'},
    6: {start: '20:20:00', end: '22:40:00'}
};

async function handleCreateSchedule(e) {
    e.preventDefault();
    const classCodeInput = document.getElementById('sched-class-code').value.trim();
    const date = document.getElementById('sched-date').value;
    const slotNumber = Number.parseInt(document.getElementById('sched-slot').value, 10);
    const room = document.getElementById('sched-room').value.trim();

    const targetClass = cachedClasses.find(c => c.classCode.toLowerCase() === classCodeInput.toLowerCase());
    if (!targetClass) {
        showToast(`Không tìm thấy lớp có mã "${classCodeInput}". Hãy đảm bảo lớp đã được tạo ở Tab "Quản lý Lớp học".`, 'error');
        return;
    }

    const times = slotTimes[slotNumber] || {start: '08:00:00', end: '10:00:00'};

    try {
        const res = await apiCall('/api/v1/admin/academic/schedules', {
            method: 'POST',
            body: JSON.stringify({
                classId: targetClass.classId,
                slotNumber,
                sessionNo: 1,
                date,
                startTime: times.start,
                endTime: times.end,
                room,
                lecturerId: null,
                note: 'Xếp lịch qua Admin Portal'
            })
        });

        showToast('Xếp lịch học thành công!', 'success');
        closeModal('modal-create-schedule');

        const tbody = document.getElementById('schedules-preview-body');
        if (tbody) {
            const row = `
                <tr>
                    <td style="font-weight: 700; color: var(--primary);">${escapeHtml(res.classCode || targetClass.classCode)}</td>
                    <td>${escapeHtml(res.date)}</td>
                    <td><span class="badge badge-info">Slot ${res.slotNumber} (${times.start.substring(0, 5)} - ${times.end.substring(0, 5)})</span></td>
                    <td>${escapeHtml(res.room)}</td>
                    <td><span class="badge badge-success">Lịch học chính thức</span></td>
                </tr>
            `;
            if (tbody.innerHTML.includes('Chọn "Thêm Lịch Học')) {
                tbody.innerHTML = row;
            } else {
                tbody.innerHTML = row + tbody.innerHTML;
            }
        }
    } catch (err) {
        showToast('Lỗi xếp lịch: ' + err.message, 'error');
    }
}

// ==========================================
// TAB 6: REQUESTS QUEUE (UC-ADM-07)
// ==========================================
function renderRequestManagementRow(r) {
    let statusBadge = '<span class="status-pending">PENDING</span>';
    if (r.status === 'APPROVED') {
        statusBadge = '<span class="status-delivered">APPROVED</span>';
    } else if (r.status === 'REJECTED') {
        statusBadge = '<span class="status-canceled">REJECTED</span>';
    }

    const actionCell = r.status === 'PENDING'
        ? `<button class="btn btn-primary btn-sm" onclick="openProcessModal(${r.requestId})">Xử lý đơn</button>`
        : `<span style="font-size: 12px; color: var(--text-muted);">${escapeHtml(r.responseNote || 'Đã giải quyết')}</span>`;

    const dateStr = r.createdAt ? new Date(r.createdAt).toLocaleDateString('vi-VN') : '---';

    return `
        <tr>
            <td style="font-weight: 700; color: var(--primary);">#${r.requestId}</td>
            <td>
                <div style="font-weight: 600;">${escapeHtml(r.studentName || '---')}</div>
                <div style="font-size: 11px; color: var(--text-muted);">${escapeHtml(r.studentRollNumber || '')}</div>
            </td>
            <td><span class="badge badge-info">${escapeHtml(r.requestType)}</span></td>
            <td>
                <div style="font-weight: 600;">${escapeHtml(r.title)}</div>
                <div style="font-size: 12px; color: var(--text-muted); margin-top: 2px;">${escapeHtml(r.content || '')}</div>
            </td>
            <td style="font-size: 12px;">${dateStr}</td>
            <td>${statusBadge}</td>
            <td style="text-align: right;">${actionCell}</td>
        </tr>
    `;
}

async function loadRequests() {
    const tbody = document.getElementById('requests-table-body');
    if (!tbody) return;
    tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">Đang tải danh sách đơn từ...</td></tr>';

    try {
        const requests = await apiCall('/api/v1/admin/academic/requests');
        cachedRequests = Array.isArray(requests) ? requests : [];

        if (cachedRequests.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">Không có đơn từ nào cần xử lý.</td></tr>';
            return;
        }

        tbody.innerHTML = cachedRequests.map(renderRequestManagementRow).join('');
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-center text-danger">Lỗi tải đơn từ: ${escapeHtml(err.message)}</td></tr>`;
    }
}

function openProcessModal(requestId) {
    const idField = document.getElementById('process-req-id');
    if (idField) idField.value = requestId;
    openModal('modal-process-request');
}

async function handleProcessRequest(e) {
    e.preventDefault();
    const requestId = document.getElementById('process-req-id').value;
    const status = document.getElementById('process-status').value;
    const note = document.getElementById('process-note').value.trim();

    try {
        await apiCall(`/api/v1/admin/academic/requests/${requestId}/process`, {
            method: 'PATCH',
            body: JSON.stringify({
                status,
                responseNote: note
            })
        });
        showToast(`Đã cập nhật trạng thái đơn #${requestId} thành ${status}!`, 'success');
        closeModal('modal-process-request');
        await loadRequests();
        void loadDashboardStats();
    } catch (err) {
        showToast('Lỗi xử lý đơn: ' + err.message, 'error');
    }
}

// ==========================================
// TAB 7: BULK IMPORT & TEMPLATES (UC-ADM-08 -> 12)
// ==========================================
async function downloadTemplate(type) {
    try {
        const token = localStorage.getItem(AUTH_TOKEN_KEY);
        const res = await fetch(`/api/v1/admin/import/templates/${type}`, {
            headers: {'Authorization': `Bearer ${token}`}
        });
        if (!res.ok) {
            showToast('Không thể tải template CSV', 'error');
            return;
        }

        const blob = await res.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${type}_template.csv`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
        showToast(`Đã tải mẫu ${type}_template.csv thành công!`, 'success');
    } catch (err) {
        showToast('Lỗi tải template: ' + err.message, 'error');
    }
}

/**
 * @typedef {Object} ImportResult
 * @property {number} [totalRecords]
 * @property {number} [successfulRecords]
 * @property {number} [failedRecords]
 * @property {string[]} [errors]
 */

function renderImportStats(result) {
    const resContainer = document.getElementById('import-result-container');
    if (resContainer) resContainer.style.display = 'block';

    const totalEl = document.getElementById('res-total');
    const successEl = document.getElementById('res-success');
    const failedEl = document.getElementById('res-failed');

    if (totalEl) totalEl.textContent = String(result.totalRecords ?? 0);
    if (successEl) successEl.textContent = String(result.successfulRecords ?? 0);
    if (failedEl) failedEl.textContent = String(result.failedRecords ?? 0);
}

function renderImportErrors(errors) {
    const errorsBox = document.getElementById('res-errors-box');
    const errorsList = document.getElementById('res-errors-list');
    if (!errorsBox || !errorsList) return;

    if (Array.isArray(errors) && errors.length > 0) {
        errorsBox.style.display = 'block';
        errorsList.innerHTML = errors.map(err => `<li>${escapeHtml(err)}</li>`).join('');
    } else {
        errorsBox.style.display = 'none';
    }
}

async function uploadCsvFile(file) {
    const typeSelect = document.getElementById('import-type-select');
    const type = typeSelect?.value ?? 'users';

    const formData = new FormData();
    formData.append('file', file);

    showToast(`Đang nạp file CSV cho phân hệ "${type}"...`, 'info');

    try {
        /** @type {ImportResult} */
        const result = await apiCall(`/api/v1/admin/import/${type}`, {
            method: 'POST',
            body: formData
        });

        renderImportStats(result);
        renderImportErrors(result.errors);

        const total = result.totalRecords ?? 0;
        const success = result.successfulRecords ?? 0;
        const failed = result.failedRecords ?? 0;

        if (failed === 0) {
            showToast(`Nhập dữ liệu thành công ${success}/${total} bản ghi!`, 'success');
        } else {
            showToast(`Nạp xong với ${success} thành công, ${failed} thất bại.`, 'warning');
        }
    } catch (err) {
        showToast('Lỗi khi nạp dữ liệu CSV: ' + err.message, 'error');
    }
}

// ==========================================
// INITIALIZATION & EVENT LISTENERS
// ==========================================
function setupHeaderAndNav() {
    const userProfileBtn = document.getElementById('user-profile-btn');
    if (userProfileBtn) {
        userProfileBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            toggleUserDropdown();
        });
    }

    const notifBtn = document.getElementById('header-notif-btn');
    if (notifBtn) {
        notifBtn.addEventListener('click', () => {
            switchTab('requests');
        });
    }

    document.querySelectorAll('.sidebar-nav .nav-item').forEach(item => {
        item.addEventListener('click', () => {
            const tab = item.dataset.tab;
            if (tab) switchTab(tab);
        });
    });

    const btnLogout = document.getElementById('btn-logout');
    if (btnLogout) btnLogout.addEventListener('click', handleLogout);
}

function setupFilterListeners() {
    const dashboardSearch = document.getElementById('dashboard-requests-search');
    if (dashboardSearch) {
        dashboardSearch.addEventListener('input', () => {
            renderDashboardRecentRequests();
        });
    }

    const dashboardFilter = document.getElementById('dashboard-requests-filter');
    if (dashboardFilter) {
        dashboardFilter.addEventListener('change', () => {
            renderDashboardRecentRequests();
        });
    }

    const userSearchInput = document.getElementById('user-search-input');
    if (userSearchInput) {
        userSearchInput.addEventListener('input', (e) => {
            const q = e.target.value.toLowerCase().trim();
            if (!q) {
                renderUsersTable(cachedUsers);
            } else {
                const filtered = cachedUsers.filter(u =>
                    u.fullName?.toLowerCase().includes(q) ||
                    u.email?.toLowerCase().includes(q) ||
                    u.rollNumber?.toLowerCase().includes(q) ||
                    u.role?.toLowerCase().includes(q)
                );
                renderUsersTable(filtered);
            }
        });
    }
}

function setupFormListeners() {
    const loginForm = document.getElementById('login-form');
    if (loginForm) loginForm.addEventListener('submit', (e) => {
        void handleLogin(e);
    });

    const formCreateUser = document.getElementById('form-create-user');
    if (formCreateUser) formCreateUser.addEventListener('submit', (e) => {
        void handleCreateUser(e);
    });

    const formCreateCourse = document.getElementById('form-create-course');
    if (formCreateCourse) formCreateCourse.addEventListener('submit', (e) => {
        void handleCreateCourse(e);
    });

    const formCreateClass = document.getElementById('form-create-class');
    if (formCreateClass) formCreateClass.addEventListener('submit', (e) => {
        void handleCreateClass(e);
    });

    const formCreateSchedule = document.getElementById('form-create-schedule');
    if (formCreateSchedule) formCreateSchedule.addEventListener('submit', (e) => {
        void handleCreateSchedule(e);
    });

    const formProcessRequest = document.getElementById('form-process-request');
    if (formProcessRequest) formProcessRequest.addEventListener('submit', (e) => {
        void handleProcessRequest(e);
    });
}

function setupRefreshButtons() {
    const btnRefreshUsers = document.getElementById('btn-refresh-users');
    if (btnRefreshUsers) btnRefreshUsers.addEventListener('click', () => {
        void loadUsers();
    });

    const btnRefreshRequests = document.getElementById('btn-refresh-requests');
    if (btnRefreshRequests) btnRefreshRequests.addEventListener('click', () => {
        void loadRequests();
    });
}

function setupCsvDropZone() {
    const dropZone = document.getElementById('drop-zone');
    const fileInput = document.getElementById('csv-file-input');

    if (dropZone && fileInput) {
        dropZone.addEventListener('click', () => fileInput.click());

        dropZone.addEventListener('dragover', (e) => {
            e.preventDefault();
            dropZone.classList.add('dragover');
        });

        dropZone.addEventListener('dragleave', () => {
            dropZone.classList.remove('dragover');
        });

        dropZone.addEventListener('drop', (e) => {
            e.preventDefault();
            dropZone.classList.remove('dragover');
            if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
                void uploadCsvFile(e.dataTransfer.files[0]);
            }
        });

        fileInput.addEventListener('change', (e) => {
            if (e.target.files && e.target.files.length > 0) {
                void uploadCsvFile(e.target.files[0]);
            }
        });
    }
}

document.addEventListener('DOMContentLoaded', () => {
    checkSession();
    setupHeaderAndNav();
    setupFilterListeners();
    setupFormListeners();
    setupRefreshButtons();
    setupCsvDropZone();
});
