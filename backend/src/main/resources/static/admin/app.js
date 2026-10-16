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
    toast.style.animation = 'slideOut 0.3s forwards';
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
    // Pre-populate dropdowns if applicable
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

// Close modal when clicking on backdrop
document.addEventListener('click', (e) => {
  if (e.target.classList.contains('modal-backdrop')) {
    e.target.classList.remove('show');
    const form = e.target.querySelector('form');
    if (form) form.reset();
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

  // Set default JSON Content-Type if not sending FormData
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
function checkSession() {
  const token = localStorage.getItem(AUTH_TOKEN_KEY);
  const userJson = localStorage.getItem(AUTH_USER_KEY);
  const overlay = document.getElementById('login-overlay');

  if (!token) {
    if (overlay) overlay.style.display = 'flex';
    return;
  }

  if (overlay) overlay.style.display = 'none';
  if (userJson) {
    try {
      const user = JSON.parse(userJson);
      const nameEl = document.getElementById('current-user-name');
      const roleEl = document.getElementById('current-user-role');
      const avatarEl = document.getElementById('current-user-avatar');
      if (nameEl) nameEl.textContent = user.fullName || user.email || 'Quản Trị Viên';
      if (roleEl) roleEl.textContent = user.role || 'ADMIN';
      if (avatarEl) avatarEl.textContent = (user.fullName || 'A').charAt(0).toUpperCase();
    } catch (err) {
      console.error('Error parsing user JSON:', err);
    }
  }
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
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ identifier: email, email, password })
    });

    if (!res.ok) {
      let errText = 'Đăng nhập không thành công';
      try {
        const errJson = await res.json();
        errText = errJson.detail || errJson.message || errText;
      } catch {
        // use default message
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

  // Update sidebar active item
  document.querySelectorAll('.sidebar-nav .nav-item').forEach(item => {
    if (item.dataset.tab === tabId) {
      item.classList.add('active');
    } else {
      item.classList.remove('active');
    }
  });

  // Update tab pane active
  document.querySelectorAll('.tab-pane').forEach(pane => {
    pane.classList.remove('active');
  });

  const activePane = document.getElementById(`tab-${tabId}`);
  if (activePane) {
    activePane.classList.add('active');
  }

  // Update page title
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

  // Fetch tab data
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
// TAB 1: DASHBOARD STATS
// ==========================================
async function loadDashboardStats() {
  try {
    const [users, courses, classes, requests] = await Promise.allSettled([
      apiCall('/api/v1/admin/users?page=0&size=50'),
      apiCall('/api/v1/admin/academic/courses'),
      apiCall('/api/v1/admin/academic/classes'),
      apiCall('/api/v1/admin/academic/requests')
    ]);

    if (users.status === 'fulfilled' && Array.isArray(users.value)) {
      document.getElementById('stat-users-count').textContent = users.value.length;
      cachedUsers = users.value;
    }
    if (courses.status === 'fulfilled' && Array.isArray(courses.value)) {
      document.getElementById('stat-courses-count').textContent = courses.value.length;
      cachedCourses = courses.value;
    }
    if (classes.status === 'fulfilled' && Array.isArray(classes.value)) {
      document.getElementById('stat-classes-count').textContent = classes.value.length;
      cachedClasses = classes.value;
    }
    if (requests.status === 'fulfilled' && Array.isArray(requests.value)) {
      document.getElementById('stat-requests-count').textContent = requests.value.length;
    }
  } catch (err) {
    console.error('Error loading dashboard stats:', err);
  }
}

// ==========================================
// TAB 2: USER MANAGEMENT (UC-ADM-01, 02, 03)
// ==========================================
async function loadUsers() {
  const tbody = document.getElementById('users-table-body');
  if (!tbody) return;
  tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: var(--text-muted);">Đang tải dữ liệu người dùng...</td></tr>';

  try {
    const users = await apiCall('/api/v1/admin/users?page=0&size=50');
    cachedUsers = Array.isArray(users) ? users : [];
    renderUsersTable(cachedUsers);
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--danger);">Lỗi tải người dùng: ${escapeHtml(err.message)}</td></tr>`;
  }
}

function renderUsersTable(users) {
  const tbody = document.getElementById('users-table-body');
  if (!tbody) return;

  if (!users || users.length === 0) {
    tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: var(--text-muted);">Không tìm thấy người dùng nào.</td></tr>';
    return;
  }

  tbody.innerHTML = users.map(u => {
    const isAct = u.status === 'ACTIVE';
    const statusBadge = isAct
      ? '<span class="badge badge-success">ACTIVE</span>'
      : '<span class="badge badge-danger">INACTIVE</span>';

    const nextStatus = isAct ? 'INACTIVE' : 'ACTIVE';
    const actionBtnClass = isAct ? 'btn-secondary' : 'btn-primary';
    const actionLabel = isAct ? 'Khóa' : 'Kích hoạt';

    return `
      <tr>
        <td>#${u.userId}</td>
        <td style="font-weight: 600;">${escapeHtml(u.fullName)}</td>
        <td>${escapeHtml(u.email)}</td>
        <td><span class="badge badge-info">${escapeHtml(u.role)}</span></td>
        <td>${escapeHtml(u.rollNumber || '---')}</td>
        <td>${escapeHtml(u.phoneNumber || '---')}</td>
        <td>${statusBadge}</td>
        <td>
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
      body: JSON.stringify({ status: newStatus })
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

// User Filter
document.addEventListener('DOMContentLoaded', () => {
  const searchInput = document.getElementById('user-search-input');
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
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
});

// ==========================================
// TAB 3: COURSE MANAGEMENT (UC-ADM-04)
// ==========================================
async function loadCourses() {
  const tbody = document.getElementById('courses-table-body');
  if (!tbody) return;
  tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: var(--text-muted);">Đang tải danh sách khóa học...</td></tr>';

  try {
    const courses = await apiCall('/api/v1/admin/academic/courses');
    cachedCourses = Array.isArray(courses) ? courses : [];
    
    if (cachedCourses.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: var(--text-muted);">Chưa có khóa học nào trong hệ thống.</td></tr>';
      return;
    }

    tbody.innerHTML = cachedCourses.map(c => `
      <tr>
        <td>#${c.courseId}</td>
        <td style="font-weight: 700; color: var(--primary);">${escapeHtml(c.courseCode)}</td>
        <td style="font-weight: 600;">${escapeHtml(c.courseName)}</td>
        <td><span class="badge badge-info">${c.credits || 3} Tín chỉ</span></td>
        <td style="color: var(--text-muted);">${escapeHtml(c.description || '---')}</td>
      </tr>
    `).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--danger);">Lỗi tải môn học: ${escapeHtml(err.message)}</td></tr>`;
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
      body: JSON.stringify({ courseCode, courseName, credits, description })
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
  tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: var(--text-muted);">Đang tải danh sách lớp học...</td></tr>';

  try {
    const classes = await apiCall('/api/v1/admin/academic/classes');
    cachedClasses = Array.isArray(classes) ? classes : [];

    if (cachedClasses.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: var(--text-muted);">Chưa có lớp học nào.</td></tr>';
      return;
    }

    tbody.innerHTML = cachedClasses.map(c => `
      <tr>
        <td>#${c.classId}</td>
        <td style="font-weight: 700; color: var(--primary);">${escapeHtml(c.classCode)}</td>
        <td>${escapeHtml(c.courseName || c.courseCode || '---')}</td>
        <td>${escapeHtml(c.lecturerName || 'Chưa phân công')}</td>
        <td><span class="badge badge-warning">${escapeHtml(c.semester || '---')}</span></td>
      </tr>
    `).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--danger);">Lỗi tải lớp học: ${escapeHtml(err.message)}</td></tr>`;
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
  1: { start: '07:30:00', end: '09:50:00' },
  2: { start: '10:00:00', end: '12:20:00' },
  3: { start: '12:50:00', end: '15:10:00' },
  4: { start: '15:20:00', end: '17:40:00' },
  5: { start: '18:00:00', end: '20:20:00' }
};

async function handleCreateSchedule(e) {
  e.preventDefault();
  const classCodeInput = document.getElementById('sched-class-code').value.trim();
  const date = document.getElementById('sched-date').value;
  const slotNumber = Number.parseInt(document.getElementById('sched-slot').value, 10);
  const room = document.getElementById('sched-room').value.trim();

  // Find classId from cachedClasses
  const targetClass = cachedClasses.find(c => c.classCode.toLowerCase() === classCodeInput.toLowerCase());
  if (!targetClass) {
    showToast(`Không tìm thấy lớp có mã "${classCodeInput}". Hãy đảm bảo lớp đã được tạo ở Tab "Quản lý Lớp học".`, 'error');
    return;
  }

  const times = slotTimes[slotNumber] || { start: '08:00:00', end: '10:00:00' };

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

    // Add to preview table
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
      if (tbody.innerHTML.includes('Chọn "Thêm Lịch Học Mới"')) {
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
async function loadRequests() {
  const tbody = document.getElementById('requests-table-body');
  if (!tbody) return;
  tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: var(--text-muted);">Đang tải danh sách đơn từ...</td></tr>';

  try {
    const requests = await apiCall('/api/v1/admin/academic/requests');
    if (!requests || requests.length === 0) {
      tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: var(--text-muted);">Không có đơn từ nào cần xử lý.</td></tr>';
      return;
    }

    tbody.innerHTML = requests.map(r => {
      let badgeClass = 'badge-warning';
      if (r.status === 'APPROVED') badgeClass = 'badge-success';
      else if (r.status === 'REJECTED') badgeClass = 'badge-danger';

      return `
        <tr>
          <td>#${r.requestId}</td>
          <td>
            <strong>${escapeHtml(r.studentName || '---')}</strong><br>
            <small style="color: var(--text-muted);">${escapeHtml(r.studentRollNumber || '')}</small>
          </td>
          <td><span class="badge badge-info">${escapeHtml(r.requestType)}</span></td>
          <td>
            <strong>${escapeHtml(r.title)}</strong>
            <p style="font-size: 12px; color: var(--text-muted); margin-top: 4px;">${escapeHtml(r.content || '')}</p>
          </td>
          <td style="font-size: 12px;">${r.createdAt ? new Date(r.createdAt).toLocaleDateString('vi-VN') : '---'}</td>
          <td><span class="badge ${badgeClass}">${escapeHtml(r.status)}</span></td>
          <td>
            ${r.status === 'PENDING' ? `
              <button class="btn btn-primary btn-sm" onclick="openProcessModal(${r.requestId})">
                Xử lý đơn
              </button>
            ` : `<span style="font-size: 12px; color: var(--text-muted);">${escapeHtml(r.responseNote || 'Đã giải quyết')}</span>`}
          </td>
        </tr>
      `;
    }).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--danger);">Lỗi tải đơn từ: ${escapeHtml(err.message)}</td></tr>`;
  }
}

function openProcessModal(requestId) {
  document.getElementById('process-req-id').value = requestId;
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
      headers: { 'Authorization': `Bearer ${token}` }
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
document.addEventListener('DOMContentLoaded', () => {
  // Check auth session
  checkSession();

  // Sidebar navigation
  document.querySelectorAll('.sidebar-nav .nav-item').forEach(item => {
    item.addEventListener('click', () => {
      const tab = item.dataset.tab;
      if (tab) switchTab(tab);
    });
  });

  // Logout button
  const btnLogout = document.getElementById('btn-logout');
  if (btnLogout) btnLogout.addEventListener('click', handleLogout);

  // Forms
  const loginForm = document.getElementById('login-form');
  if (loginForm) loginForm.addEventListener('submit', (e) => { void handleLogin(e); });

  const formCreateUser = document.getElementById('form-create-user');
  if (formCreateUser) formCreateUser.addEventListener('submit', (e) => { void handleCreateUser(e); });

  const formCreateCourse = document.getElementById('form-create-course');
  if (formCreateCourse) formCreateCourse.addEventListener('submit', (e) => { void handleCreateCourse(e); });

  const formCreateClass = document.getElementById('form-create-class');
  if (formCreateClass) formCreateClass.addEventListener('submit', (e) => { void handleCreateClass(e); });

  const formCreateSchedule = document.getElementById('form-create-schedule');
  if (formCreateSchedule) formCreateSchedule.addEventListener('submit', (e) => { void handleCreateSchedule(e); });

  const formProcessRequest = document.getElementById('form-process-request');
  if (formProcessRequest) formProcessRequest.addEventListener('submit', (e) => { void handleProcessRequest(e); });

  // Refresh buttons
  const btnRefreshUsers = document.getElementById('btn-refresh-users');
  if (btnRefreshUsers) btnRefreshUsers.addEventListener('click', () => { void loadUsers(); });

  const btnRefreshRequests = document.getElementById('btn-refresh-requests');
  if (btnRefreshRequests) btnRefreshRequests.addEventListener('click', () => { void loadRequests(); });

  // Drag and drop CSV upload
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
});
