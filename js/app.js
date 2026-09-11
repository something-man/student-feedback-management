/**
 * College Feedback Management System — Complete Application JavaScript
 * Connects frontend UI to Spring Boot REST API & PostgreSQL Database.
 * Handles authentication, role guard, interactive submissions, real data binding, and modals.
 */

document.addEventListener('DOMContentLoaded', () => {
  initRoleSelector();
  initPasswordToggles();
  initModals();
  initStarRatings();
  initMobileSidebar();
  initAuthGuard();
  initPageData();
});

/* ==========================================================================
   ROLE SELECTOR & AUTHENTICATION (Page 2: Login Page)
   ========================================================================== */
function initRoleSelector() {
  const roleCards = document.querySelectorAll('.role-card-option');
  const emailInput = document.getElementById('loginEmail');
  const passwordInput = document.getElementById('loginPassword');
  const roleDisplay = document.getElementById('activeRoleHint');
  const loginForm = document.getElementById('loginForm');

  if (!roleCards.length) return;

  const roleConfigs = {
    student: {
      email: 'student@example.com',
      password: 'Student@123',
      label: 'Student Portal',
      redirect: 'student-dashboard.html',
      placeholder: 'student@example.com or Roll No'
    },
    faculty: {
      email: 'faculty@example.com',
      password: 'Faculty@123',
      label: 'Faculty Portal',
      redirect: 'faculty-dashboard.html',
      placeholder: 'faculty@example.com or Employee ID'
    },
    admin: {
      email: 'admin@example.com',
      password: 'Admin@123',
      label: 'Admin Control Center',
      redirect: 'admin-dashboard.html',
      placeholder: 'admin@example.com or Admin ID'
    }
  };

  let selectedRole = 'student';

  roleCards.forEach(card => {
    card.addEventListener('click', () => {
      roleCards.forEach(c => c.classList.remove('active'));
      card.classList.add('active');
      selectedRole = card.getAttribute('data-role');

      const config = roleConfigs[selectedRole];
      if (emailInput) {
        emailInput.placeholder = config.placeholder;
        emailInput.value = config.email;
      }
      if (passwordInput) {
        passwordInput.value = config.password;
      }
      if (roleDisplay) {
        roleDisplay.textContent = config.label;
      }
    });
  });

  if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const email = emailInput ? emailInput.value.trim() : '';
      const password = passwordInput ? passwordInput.value : '';
      const submitBtn = loginForm.querySelector('button[type="submit"]');

      if (!email || !password) {
        showToast('Validation Error', 'Please enter both email and password.', 'error');
        return;
      }

      try {
        if (submitBtn) {
          submitBtn.disabled = true;
          submitBtn.innerHTML = `<span>Logging in...</span>`;
        }

        showToast('Authenticating', 'Verifying credentials with server...', 'info');

        let authData;
        try {
          authData = await authAPI.login(email, password, true);
        } catch (apiErr) {
          // If the backend is unreachable, fall back to demo credentials
          if (apiErr.isNetworkError) {
            authData = getDemoAuthData(email, password, selectedRole);
            if (!authData) {
              showToast('Login Failed', 'Server is offline. Use the demo credentials shown in the input fields.', 'error');
              return;
            }
            AuthStore.setToken('demo-token-' + authData.role.toLowerCase(), true);
            AuthStore.setUser(authData, true);
            showToast('Demo Mode', 'Backend offline — signed in with demo account.', 'info');
          } else {
            throw apiErr;
          }
        }

        showToast('Login Successful', `Welcome, ${authData.fullName}! Redirecting...`, 'success');

        // Role-based redirection from backend authority
        setTimeout(() => {
          if (authData.role === 'ADMIN') {
            window.location.href = 'admin-dashboard.html';
          } else if (authData.role === 'FACULTY') {
            window.location.href = 'faculty-dashboard.html';
          } else {
            window.location.href = 'student-dashboard.html';
          }
        }, 800);

      } catch (err) {
        console.error('Login failed:', err);
        showToast('Login Failed', err.message || 'Invalid email or password. Please try again.', 'error');
      } finally {
        if (submitBtn) {
          submitBtn.disabled = false;
          submitBtn.innerHTML = `Login <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>`;
        }
      }
    });
  }
}

/**
 * Returns demo user data when the backend is offline.
 * Validates email + password against known demo credentials.
 */
function getDemoAuthData(email, password, role) {
  const demoAccounts = {
    student: { email: 'student@example.com', password: 'Student@123', fullName: 'Aarav Sharma', role: 'STUDENT', identifier: 'CS-2024-042', department: 'Computer Science' },
    faculty: { email: 'faculty@example.com', password: 'Faculty@123', fullName: 'Dr. Vikram Malhotra', role: 'FACULTY', identifier: 'FAC-001', department: 'Computer Science' },
    admin:   { email: 'admin@example.com',   password: 'Admin@123',   fullName: 'Admin Office',        role: 'ADMIN',   identifier: 'ADM-001', department: 'Academic Administration' }
  };

  const account = demoAccounts[role];
  if (!account) return null;
  if (email.toLowerCase() !== account.email) return null;
  if (password !== account.password) return null;
  return account;
}

/* ==========================================================================
   AUTH GUARD (Protects Dashboard Pages)
   ========================================================================== */
function initAuthGuard() {
  const path = window.location.pathname;
  const isStudentPage = path.includes('student-dashboard.html');
  const isFacultyPage = path.includes('faculty-dashboard.html');
  const isAdminPage = path.includes('admin-dashboard.html');

  if (!isStudentPage && !isFacultyPage && !isAdminPage) return;

  if (!AuthStore.isAuthenticated()) {
    showToast('Authentication Required', 'Please login to access this dashboard.', 'error');
    setTimeout(() => {
      window.location.href = 'login.html';
    }, 1000);
    return;
  }

  const role = AuthStore.getRole();
  // Accept demo tokens as valid (demo-token-student / demo-token-faculty / demo-token-admin)
  const token = AuthStore.getToken() || '';
  const isDemoToken = token.startsWith('demo-token-');

  if (isStudentPage && role !== 'STUDENT') {
    showToast('Unauthorized', 'Access restricted to Student accounts.', 'error');
    redirectToUserDashboard(role);
  } else if (isFacultyPage && role !== 'FACULTY') {
    showToast('Unauthorized', 'Access restricted to Faculty accounts.', 'error');
    redirectToUserDashboard(role);
  } else if (isAdminPage && role !== 'ADMIN') {
    showToast('Unauthorized', 'Access restricted to Administrator accounts.', 'error');
    redirectToUserDashboard(role);
  }
}

function redirectToUserDashboard(role) {
  setTimeout(() => {
    if (role === 'ADMIN') window.location.href = 'admin-dashboard.html';
    else if (role === 'FACULTY') window.location.href = 'faculty-dashboard.html';
    else window.location.href = 'student-dashboard.html';
  }, 1000);
}

/* ==========================================================================
   PAGE DATA INITIALIZATION
   ========================================================================== */
function initPageData() {
  const path = window.location.pathname;
  if (path.includes('student-dashboard.html')) {
    loadStudentDashboard();
    syncUnreadNotifications();
  } else if (path.includes('faculty-dashboard.html')) {
    loadFacultyDashboard();
    syncUnreadNotifications();
  } else if (path.includes('admin-dashboard.html')) {
    loadAdminDashboard();
    initAdminQuestionBuilder();
    syncUnreadNotifications();
  } else if (path.includes('index.html') || path === '/' || path.endsWith('/')) {
    loadPublicComplaintsFeed();
  }
}

/* ==========================================================================
   STUDENT DASHBOARD DATA BINDING & WORKFLOW
   ========================================================================== */
let currentStudentFormDetails = null;

async function loadStudentDashboard() {
  try {
    const user = AuthStore.getUser();
    if (user) {
      const nameEl = document.querySelector('.user-display-name');
      const roleEl = document.querySelector('.user-role-sub');
      const avatarEl = document.querySelector('.user-avatar-circle');
      const welcomeH1 = document.querySelector('.welcome-header h1');

      if (nameEl) nameEl.textContent = user.fullName || 'Student';
      if (roleEl) roleEl.textContent = `Roll No: ${user.identifier || 'CS-2024-042'}`;
      if (avatarEl && user.fullName) {
        avatarEl.textContent = user.fullName.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
      }
      if (welcomeH1) welcomeH1.textContent = `Welcome, ${user.fullName || 'Student'}!`;
    }

    // Fetch live dashboard data — fall back to demo data if backend is offline
    let dashboardData;
    try {
      dashboardData = await studentAPI.getDashboard();
    } catch (fetchErr) {
      if (fetchErr.isNetworkError) {
        dashboardData = getDemoStudentDashboard();
      } else {
        throw fetchErr;
      }
    }

    if (!dashboardData) return;

    // 1. Update KPI Summary Cards
    const summaryValues = document.querySelectorAll('.summary-card .summary-value');
    if (summaryValues.length >= 3) {
      summaryValues[0].textContent = `${dashboardData.pendingFeedbackCount || 0} Pending`;
      summaryValues[1].textContent = `${dashboardData.activeComplaintsCount || 0} Active`;
      summaryValues[2].textContent = `${dashboardData.pendingRequestsCount || 0} Pending`;
    }

    // 2. Render Pending Feedback Cards
    const feedbackContainer = document.querySelector('.feedback-cards-stack');
    if (feedbackContainer && dashboardData.assignedFeedbacks) {
      renderStudentFeedbackCards(feedbackContainer, dashboardData.assignedFeedbacks);
    }

    // 3. Render Recent Complaints & Requests Table
    const tableBody = document.querySelector('.custom-table tbody');
    if (tableBody) {
      renderStudentIssuesTable(tableBody, dashboardData.recentComplaints || [], dashboardData.recentRequests || []);
    }

  } catch (err) {
    console.error('Failed to load student dashboard:', err);
    showToast('Dashboard Sync', 'Could not refresh live data. Using cached state.', 'info');
  }
}

/** Demo data returned when the Spring Boot backend is offline */
function getDemoStudentDashboard() {
  return {
    pendingFeedbackCount: 3,
    activeComplaintsCount: 2,
    pendingRequestsCount: 1,
    assignedFeedbacks: [
      { id: 'demo-1', title: 'Data Science – Faculty Feedback',              category: 'FACULTY',   deadline: '2026-09-15', userStatus: 'PENDING' },
      { id: 'demo-2', title: 'Infrastructure Feedback',                       category: 'FACILITY',  deadline: '2026-09-18', userStatus: 'PENDING' },
      { id: 'demo-3', title: 'Elective Course Review: AI & Machine Learning', category: 'ACADEMIC',  deadline: '2026-09-22', userStatus: 'PENDING' }
    ],
    recentComplaints: [
      { id: 'c1', ticketNumber: 'TICK-0101', category: 'INFRASTRUCTURE', title: 'AC Malfunction in Lab 3',     status: 'IN_PROGRESS', createdAt: new Date(Date.now() - 2 * 86400000).toISOString() },
      { id: 'c2', ticketNumber: 'TICK-0098', category: 'IT',             title: 'Wi-Fi connectivity issues',   status: 'PENDING',     createdAt: new Date(Date.now() - 5 * 86400000).toISOString() }
    ],
    recentRequests: [
      { id: 'r1', requestNumber: 'REQ-0045', category: 'DOCUMENT', title: 'Bonafide Certificate for Internship', status: 'APPROVED', createdAt: new Date(Date.now() - 3 * 86400000).toISOString() }
    ]
  };
}

function renderStudentFeedbackCards(container, forms) {
  if (!forms || forms.length === 0) {
    container.innerHTML = `
      <div style="text-align:center; padding:32px; color:var(--text-muted); background:var(--bg-card); border-radius:var(--radius-lg); border:1px dashed var(--border-subtle);">
        <p style="margin:0; font-weight:600;">🎉 No pending feedback forms assigned right now.</p>
        <small>Check back later when new evaluations are published.</small>
      </div>`;
    return;
  }

  container.innerHTML = forms.map(f => {
    const isCompleted = f.userStatus === 'COMPLETED';
    const deadlineText = f.deadline ? new Date(f.deadline).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' }) : 'No deadline';

    return `
      <div class="feedback-item-card">
        <div class="feedback-item-main">
          <div class="feedback-item-title">${escapeHtml(f.title)}</div>
          <div class="feedback-meta-row">
            <span class="meta-pill">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
              Category: <strong>${escapeHtml(f.category || 'General')}</strong>
            </span>
            <span>•</span>
            <span class="meta-pill" style="color:var(--status-yellow-text); font-weight:600;">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
              Deadline: ${deadlineText}
            </span>
          </div>
        </div>
        ${isCompleted 
          ? `<span class="badge badge-green" style="padding:8px 14px; font-weight:700;">✓ Completed</span>` 
          : `<button class="btn btn-accent btn-sm" onclick="openStudentRespondModal('${f.id}')">
              Respond
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>
            </button>`
        }
      </div>
    `;
  }).join('');
}

function renderStudentIssuesTable(tbody, complaints, requests) {
  const allIssues = [
    ...complaints.map(c => ({
      id: c.id,
      ticket: c.ticketNumber,
      type: 'Complaint',
      badgeClass: 'badge-yellow',
      category: c.category,
      title: c.title,
      status: c.status,
      date: c.createdAt
    })),
    ...requests.map(r => ({
      id: r.id,
      ticket: r.requestNumber,
      type: 'Request',
      badgeClass: 'badge-green',
      category: r.category,
      title: r.title,
      status: r.status,
      date: r.createdAt
    }))
  ].sort((a, b) => new Date(b.date) - new Date(a.date));

  if (allIssues.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding:24px; color:var(--text-muted);">No recent complaints or requests lodged.</td></tr>`;
    return;
  }

  tbody.innerHTML = allIssues.map(issue => {
    const formattedTime = formatRelativeTime(issue.date);
    let statusBadgeClass = 'badge-yellow';
    if (issue.status === 'RESOLVED' || issue.status === 'COMPLETED') statusBadgeClass = 'badge-green';
    else if (issue.status === 'CLOSED') statusBadgeClass = 'badge-neutral';
    else if (issue.status === 'REJECTED' || issue.status === 'ESCALATED') statusBadgeClass = 'badge-red';
    else if (issue.status === 'IN_PROGRESS') statusBadgeClass = 'badge-blue';

    return `
      <tr>
        <td><strong>#${escapeHtml(issue.ticket)}</strong></td>
        <td><span class="badge ${issue.badgeClass}">${issue.type}</span></td>
        <td><span class="badge badge-neutral">${escapeHtml(issue.category)}</span></td>
        <td><strong>${escapeHtml(issue.title)}</strong></td>
        <td>
          <span class="badge ${statusBadgeClass}">
            <span class="badge-dot"></span> ${escapeHtml(issue.status)}
          </span>
        </td>
        <td style="color:var(--text-muted); font-size:0.8125rem;">${formattedTime}</td>
        <td>
          <button class="btn btn-secondary btn-sm" onclick="openStudentTrackModal('${issue.id}')" title="Track Lifecycle & Timeline">
            🔍 Track
          </button>
        </td>
      </tr>
    `;
  }).join('');
}

// Student Live Issue Tracking Modal
async function openStudentTrackModal(issueId) {
  try {
    showToast('Loading Tracker', 'Fetching live issue lifecycle & timeline...', 'info');
    let issue;
    try {
      issue = await studentAPI.getIssue(issueId);
    } catch (apiErr) {
      if (apiErr.isNetworkError) {
        issue = getDemoIssue(issueId);
      } else { throw apiErr; }
    }
    if (!issue) return;

    const ticketEl = document.getElementById('trackModalTicketNumber');
    const metaEl = document.getElementById('trackModalMeta');
    const titleEl = document.getElementById('trackModalTitle');
    const descEl = document.getElementById('trackModalDescription');
    const statusEl = document.getElementById('trackModalStatusBadge');
    const priorityEl = document.getElementById('trackModalPriorityBadge');
    const assignedEl = document.getElementById('trackModalAssignedCell');
    const timelineEl = document.getElementById('trackTimelineList');

    const ticketNumber = issue.ticketNumber || issue.requestNumber || 'Ticket';
    if (ticketEl) ticketEl.textContent = `Track #${ticketNumber}`;
    if (metaEl) metaEl.textContent = `Category: ${issue.category || 'General'} • Lodged ${formatRelativeTime(issue.createdAt)}`;
    if (titleEl) titleEl.textContent = issue.title || issue.subject || 'Untitled Issue';
    if (descEl) descEl.textContent = issue.description || issue.details || 'No detailed description provided.';

    // Status Badge
    let statusClass = 'badge-yellow';
    if (issue.status === 'RESOLVED' || issue.status === 'COMPLETED') statusClass = 'badge-green';
    else if (issue.status === 'CLOSED') statusClass = 'badge-neutral';
    else if (issue.status === 'REJECTED' || issue.status === 'ESCALATED') statusClass = 'badge-red';
    else if (issue.status === 'IN_PROGRESS' || issue.status === 'APPROVED') statusClass = 'badge-blue';
    if (statusEl) statusEl.innerHTML = `<span class="badge ${statusClass}"><span class="badge-dot"></span> ${escapeHtml(issue.status)}</span>`;

    // Priority Badge
    let prioClass = 'badge-yellow';
    if (issue.priority === 'HIGH' || issue.priority === 'CRITICAL') prioClass = 'badge-red';
    else if (issue.priority === 'LOW') prioClass = 'badge-green';
    if (priorityEl) priorityEl.innerHTML = `<span class="badge ${prioClass}">${escapeHtml(issue.priority || 'MEDIUM')}</span>`;

    // Assigned Cell
    if (assignedEl) assignedEl.textContent = issue.assignedCell || 'Pending Cell Assignment';

    // Timeline Rendering
    if (timelineEl) {
      if (!issue.updates || issue.updates.length === 0) {
        timelineEl.innerHTML = `
          <div class="timeline-step">
            <div class="timeline-dot completed"></div>
            <div class="timeline-content">
              <div class="timeline-header">
                <span class="timeline-title">${escapeHtml(issue.status || 'PENDING')}</span>
                <span class="timeline-time">${formatRelativeTime(issue.createdAt)}</span>
              </div>
              <div class="timeline-body">Ticket #${escapeHtml(ticketNumber)} registered.</div>
            </div>
          </div>
        `;
      } else {
        timelineEl.innerHTML = issue.updates.map((u, idx) => {
          const isLast = idx === issue.updates.length - 1;
          const statusText = u.statusUpdate || u.newStatus || issue.status || 'UPDATE';
          const isResolved = statusText === 'RESOLVED' || statusText === 'CLOSED' || statusText === 'COMPLETED';
          const dotClass = isResolved ? 'completed' : (isLast ? 'active' : 'completed');
          const timeStr = u.createdAt ? formatRelativeTime(u.createdAt) : 'Recently';
          const authorText = u.updatedByName ? `${u.updatedByName}${u.updatedByRole ? ` (${u.updatedByRole})` : ''}` : '';

          return `
            <div class="timeline-step">
              <div class="timeline-dot ${dotClass}"></div>
              <div class="timeline-content">
                <div class="timeline-header">
                  <span class="timeline-title">${escapeHtml(statusText)}</span>
                  <span class="timeline-time">${timeStr}</span>
                </div>
                <div class="timeline-body">${escapeHtml(u.comment || 'Status updated')}</div>
                ${authorText ? `<div class="timeline-meta" style="margin-top:4px; font-size:0.75rem; color:var(--text-muted);">Updated by: <strong>${escapeHtml(authorText)}</strong></div>` : ''}
              </div>
            </div>
          `;
        }).join('');
      }
    }

    openModal('trackIssueModal');
  } catch (err) {
    console.error('Failed to open student track modal:', err);
    showToast('Lookup Error', err.message || 'Could not fetch issue details.', 'error');
  }
}

// Student Respond Modal with Dynamic Questions
async function openStudentRespondModal(formId) {
  try {
    showToast('Loading Form', 'Fetching questions from server...', 'info');
    let form;
    try {
      form = await studentAPI.getFeedbackDetails(formId);
    } catch (apiErr) {
      if (apiErr.isNetworkError) {
        form = getDemoFeedbackForm(formId);
      } else { throw apiErr; }
    }
    currentStudentFormDetails = form;

    const modalTitle = document.getElementById('modalFormTitle');
    const modalMeta = document.getElementById('modalFormMeta');
    const modalBody = document.querySelector('#respondFeedbackModal .modal-body');

    if (modalTitle) modalTitle.textContent = form.title;
    if (modalMeta) modalMeta.textContent = `Category: ${form.category || 'General'} • Deadline: ${form.deadline ? new Date(form.deadline).toLocaleDateString() : 'Active'}`;

    if (modalBody && form.questions) {
      let questionsHtml = `
        <div style="background:var(--blue-50); border:1px solid var(--blue-100); border-radius:var(--radius-md); padding:12px 16px; margin-bottom:20px; font-size:0.8125rem; color:var(--blue-600); display:flex; align-items:center; gap:8px;">
          <span>🔒 Responses are processed confidentially. Double-blind anonymity is enforced.</span>
        </div>
      `;

      form.questions.forEach((q, idx) => {
        questionsHtml += `
          <div class="form-group question-item" data-question-id="${q.id}" data-type="${q.questionType}" data-required="${q.required ? 'true' : 'false'}" data-question-text="${escapeHtml(q.questionText)}">
            <label class="form-label">${idx + 1}. ${escapeHtml(q.questionText)} ${q.required ? '<span style="color:red;">*</span>' : ''}</label>
            ${renderDynamicQuestionInput(q, idx)}
          </div>
        `;
      });

      questionsHtml += `
        <div class="form-group" style="margin-bottom:0;">
          <label class="checkbox-label">
            <input type="checkbox" id="studentAnonymousToggle" ${form.allowAnonymous ? 'checked' : 'disabled'} style="accent-color:var(--blue-600);">
            <span style="font-size:0.875rem;">Submit anonymously (Hide Student ID &amp; details from faculty and analytics)</span>
          </label>
        </div>
      `;

      modalBody.innerHTML = questionsHtml;
      initStarRatings(); // Rebind star click handlers
    }

    openModal('respondFeedbackModal');
  } catch (err) {
    console.error('Failed to load feedback form:', err);
    showToast('Error', 'Unable to load feedback questions. Please try again.', 'error');
  }
}

function renderDynamicQuestionInput(q, idx) {
  if (q.questionType === 'STAR_RATING' || q.questionType === 'RATING') {
    return `
      <div class="star-rating-input" data-rating="5">
        <span class="selected">★</span>
        <span class="selected">★</span>
        <span class="selected">★</span>
        <span class="selected">★</span>
        <span class="selected">★</span>
      </div>
      <small style="color:var(--text-muted); font-size:0.75rem; margin-top:4px; display:block;">Click stars to rate (5 / 5)</small>
    `;
  } else if (q.questionType === 'YES_NO') {
    return `
      <select class="form-control question-answer-input">
        <option value="Yes">Yes</option>
        <option value="No">No</option>
      </select>
    `;
  } else if (q.questionType === 'MULTIPLE_CHOICE' || q.questionType === 'CRITERIA_SELECT') {
    return `
      <select class="form-control question-answer-input">
        <option value="Excellent">Excellent — Very clear explanations & examples</option>
        <option value="Good">Good — Understandable with minor questions</option>
        <option value="Average">Average — Requires more practical demonstrations</option>
        <option value="Needs Improvement">Needs Improvement</option>
      </select>
    `;
  } else {
    return `
      <textarea class="form-control question-answer-input" rows="3" placeholder="Share specific recommendations or feedback..."></textarea>
    `;
  }
}

// Student Submit Feedback
async function submitFeedbackResponse() {
  if (!currentStudentFormDetails) return;

  const questionElements = document.querySelectorAll('#respondFeedbackModal .question-item');
  const anonymousToggle = document.getElementById('studentAnonymousToggle');
  const isAnonymous = anonymousToggle ? anonymousToggle.checked : false;

  const answers = [];
  let ratingSum = 0;
  let ratingCount = 0;

  for (const el of questionElements) {
    const questionId = el.getAttribute('data-question-id');
    const type = el.getAttribute('data-type');
    const isRequired = el.getAttribute('data-required') === 'true';
    const qText = el.getAttribute('data-question-text') || 'Question';
    const starInput = el.querySelector('.star-rating-input');
    const textInput = el.querySelector('.question-answer-input');

    if (starInput) {
      const selectedStars = starInput.querySelectorAll('.selected').length;
      if (isRequired && selectedStars === 0) {
        showToast('Validation Error', `Please provide a rating for: "${qText}"`, 'error');
        return;
      }
      answers.push({
        questionId: questionId,
        ratingValue: selectedStars > 0 ? selectedStars : 5,
        textAnswer: null
      });
      ratingSum += (selectedStars > 0 ? selectedStars : 5);
      ratingCount++;
    } else if (textInput) {
      const val = textInput.value.trim();
      if (isRequired && !val) {
        showToast('Validation Error', `Please answer required question: "${qText}"`, 'error');
        textInput.focus();
        return;
      }
      answers.push({
        questionId: questionId,
        ratingValue: null,
        textAnswer: val || (type === 'YES_NO' ? 'Yes' : '')
      });
    }
  }

  const overallRating = ratingCount > 0 ? Math.round((ratingSum / ratingCount) * 10.0) / 10.0 : 5.0;

  const payload = {
    isAnonymous: isAnonymous,
    overallRating: overallRating,
    answers: answers
  };

  try {
    showToast('Submitting', 'Recording feedback securely...', 'info');
    try {
      await studentAPI.submitFeedback(currentStudentFormDetails.id, payload);
    } catch (apiErr) {
      if (!apiErr.isNetworkError) throw apiErr;
      // Demo mode: treat as successful offline submission
    }
    closeModal('respondFeedbackModal');
    showToast('Feedback Submitted', `Your response for "${currentStudentFormDetails.title}" has been recorded.`, 'success');
    loadStudentDashboard();
  } catch (err) {
    console.error('Submit response failed:', err);
    if (err.message && err.message.includes('already submitted')) {
      showToast('Duplicate Submission', 'You have already submitted feedback for this campaign.', 'error');
    } else {
      showToast('Submission Failed', err.message || 'Unable to submit feedback.', 'error');
    }
  }
}

// Student Submit Complaint
async function submitComplaint(e) {
  if (e) e.preventDefault();
  const form = document.querySelector('#raiseComplaintModal form');
  if (!form) return;

  const category = form.querySelector('select[required]').value;
  const subject = form.querySelector('input[type="text"]').value.trim();
  const urgencySelect = form.querySelectorAll('select')[1];
  const priority = urgencySelect ? urgencySelect.value.split(' — ')[0].toUpperCase() : 'MEDIUM';
  const description = form.querySelector('textarea').value.trim();

  if (!subject || !description) {
    showToast('Validation Error', 'Please complete all required fields.', 'error');
    return;
  }

  try {
    showToast('Lodging Complaint', 'Registering grievance ticket...', 'info');
    const payload = {
      category: category.toUpperCase(),
      subject: subject,
      description: description,
      priority: priority
    };

    let result;
    try {
      result = await studentAPI.createComplaint(payload);
    } catch (apiErr) {
      if (apiErr.isNetworkError) {
        result = { ticketNumber: 'DEMO-' + Math.floor(1000 + Math.random() * 9000) };
      } else { throw apiErr; }
    }
    closeModal('raiseComplaintModal');
    form.reset();
    showToast('Complaint Lodged #' + result.ticketNumber, 'Your grievance has been submitted to the Admin Grievance Cell.', 'success');
    loadStudentDashboard();
  } catch (err) {
    console.error('Create complaint failed:', err);
    showToast('Complaint Failed', err.message || 'Could not register complaint.', 'error');
  }
}

// Student Submit Request
async function submitRequest(e) {
  if (e) e.preventDefault();
  const form = document.querySelector('#raiseRequestModal form');
  if (!form) return;

  const category = form.querySelector('select[required]').value;
  const title = form.querySelector('input[type="text"]').value.trim();
  const details = form.querySelector('textarea').value.trim();

  if (!title || !details) {
    showToast('Validation Error', 'Please complete all required fields.', 'error');
    return;
  }

  try {
    showToast('Submitting Request', 'Sending request for review...', 'info');
    const payload = {
      category: category.toUpperCase(),
      title: title,
      details: details
    };

    let result;
    try {
      result = await studentAPI.createRequest(payload);
    } catch (apiErr) {
      if (apiErr.isNetworkError) {
        result = { requestNumber: 'REQ-' + Math.floor(1000 + Math.random() * 9000) };
      } else { throw apiErr; }
    }
    closeModal('raiseRequestModal');
    form.reset();
    showToast('Request Submitted #' + result.requestNumber, 'Your service request has been sent for administrative review.', 'success');
    loadStudentDashboard();
  } catch (err) {
    console.error('Create request failed:', err);
    showToast('Request Failed', err.message || 'Could not submit request.', 'error');
  }
}

/* ==========================================================================
   FACULTY DASHBOARD DATA BINDING & WORKFLOW
   ========================================================================== */
let currentFacultyFormDetails = null;

async function loadFacultyDashboard() {
  try {
    const user = AuthStore.getUser();
    if (user) {
      const nameEl = document.querySelector('.user-display-name');
      const roleEl = document.querySelector('.user-role-sub');
      const avatarEl = document.querySelector('.user-avatar-circle');

      if (nameEl) nameEl.textContent = user.fullName || 'Faculty Member';
      if (roleEl) roleEl.textContent = `${user.department || 'Computer Science'}`;
      if (avatarEl && user.fullName) {
        avatarEl.textContent = user.fullName.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
      }
    }

    const dashboard = await facultyAPI.getDashboard().catch(err => {
      if (err.isNetworkError) return getDemoFacultyDashboard();
      throw err;
    });
    if (dashboard) {
      const summaryValues = document.querySelectorAll('.summary-card .summary-value');
      if (summaryValues.length >= 4) {
        summaryValues[0].textContent = `${dashboard.pendingFeedbackCount || 0} Pending`;
        summaryValues[1].textContent = `${dashboard.averageRating || 4.4} / 5`;
        summaryValues[2].textContent = `${dashboard.studentResponseRate || 82}% Active`;
        summaryValues[3].textContent = `${dashboard.syllabusMilestones || 96}% Met`;
      }
    }

    // Load Assigned Feedback items for Faculty
    try {
      const assignedFeedbacks = await facultyAPI.getFeedback().catch(err => {
        if (err.isNetworkError) return getDemoFacultyFeedbacks();
        throw err;
      });
      const container = document.querySelector('#admin-assigned-feedback .feedback-cards-stack');
      if (container && assignedFeedbacks) {
        renderFacultyFeedbackCards(container, assignedFeedbacks);
      }
    } catch (fErr) {
      console.warn('Faculty feedback load fallback:', fErr);
    }

  } catch (err) {
    console.error('Failed to load faculty dashboard:', err);
  }
}

function renderFacultyFeedbackCards(container, forms) {
  if (!forms || forms.length === 0) {
    container.innerHTML = `
      <div style="text-align:center; padding:24px; color:var(--text-muted); background:var(--bg-subtle); border-radius:var(--radius-md);">
        <p style="margin:0; font-weight:600;">✨ No pending administrative feedback forms assigned.</p>
      </div>`;
    return;
  }

  container.innerHTML = forms.map(f => {
    const isCompleted = f.userStatus === 'COMPLETED';
    const deadlineText = f.deadline ? new Date(f.deadline).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' }) : 'Active';

    return `
      <div class="feedback-item-card">
        <div class="feedback-item-main">
          <div class="feedback-item-title">${escapeHtml(f.title)}</div>
          <div class="feedback-meta-row">
            <span class="meta-pill">
              Category: <strong>${escapeHtml(f.category || 'Academic')}</strong>
            </span>
            <span>•</span>
            <span class="meta-pill" style="color:var(--status-yellow-text); font-weight:600;">
              Deadline: ${deadlineText}
            </span>
          </div>
        </div>
        ${isCompleted 
          ? `<span class="badge badge-green" style="padding:8px 14px; font-weight:700;">✓ Completed</span>` 
          : `<button class="btn btn-accent btn-sm" onclick="openFacultyRespondModal('${f.id}')">
              Respond
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>
            </button>`
        }
      </div>
    `;
  }).join('');
}

async function openFacultyRespondModal(formId) {
  try {
    showToast('Loading Form', 'Fetching evaluation criteria...', 'info');
    let form;
    try {
      form = await facultyAPI.getFeedbackDetails(formId);
    } catch (apiErr) {
      if (apiErr.isNetworkError) {
        form = getDemoFeedbackForm(formId);
      } else { throw apiErr; }
    }
    currentFacultyFormDetails = form;

    const modalTitle = document.getElementById('facModalFormTitle');
    const modalMeta = document.getElementById('facModalFormMeta');
    const modalBody = document.getElementById('facModalBody');

    if (modalTitle) modalTitle.textContent = form.title;
    if (modalMeta) modalMeta.textContent = `Category: ${form.category || 'Academic'} • Deadline: ${form.deadline ? new Date(form.deadline).toLocaleDateString() : 'Active'}`;

    if (modalBody && form.questions) {
      let questionsHtml = ``;
      form.questions.forEach((q, idx) => {
        questionsHtml += `
          <div class="form-group fac-question-item" data-question-id="${q.id}" data-type="${q.questionType}" data-required="${q.required ? 'true' : 'false'}" data-question-text="${escapeHtml(q.questionText)}">
            <label class="form-label">${idx + 1}. ${escapeHtml(q.questionText)} ${q.required ? '<span style="color:red;">*</span>' : ''}</label>
            ${renderDynamicQuestionInput(q, idx)}
          </div>
        `;
      });
      modalBody.innerHTML = questionsHtml;
      initStarRatings();
    }

    openModal('facultyRespondModal');
  } catch (err) {
    console.error('Failed to load faculty feedback form:', err);
    showToast('Error', 'Unable to load feedback questions. Please try again.', 'error');
  }
}

async function submitFacultyFeedbackResponse() {
  if (!currentFacultyFormDetails) {
    closeModal('facultyRespondModal');
    showToast('Submitted', 'Faculty evaluation recorded.', 'success');
    return;
  }

  const questionElements = document.querySelectorAll('#facultyRespondModal .fac-question-item');
  const answers = [];
  let ratingSum = 0;
  let ratingCount = 0;

  for (const el of questionElements) {
    const questionId = el.getAttribute('data-question-id');
    const type = el.getAttribute('data-type');
    const isRequired = el.getAttribute('data-required') === 'true';
    const qText = el.getAttribute('data-question-text') || 'Question';
    const starInput = el.querySelector('.star-rating-input');
    const textInput = el.querySelector('.question-answer-input');

    if (starInput) {
      const selectedStars = starInput.querySelectorAll('.selected').length;
      if (isRequired && selectedStars === 0) {
        showToast('Validation Error', `Please provide a rating for: "${qText}"`, 'error');
        return;
      }
      answers.push({
        questionId: questionId,
        ratingValue: selectedStars > 0 ? selectedStars : 5,
        textAnswer: null
      });
      ratingSum += (selectedStars > 0 ? selectedStars : 5);
      ratingCount++;
    } else if (textInput) {
      const val = textInput.value.trim();
      if (isRequired && !val) {
        showToast('Validation Error', `Please answer required question: "${qText}"`, 'error');
        textInput.focus();
        return;
      }
      answers.push({
        questionId: questionId,
        ratingValue: null,
        textAnswer: val || (type === 'YES_NO' ? 'Yes' : '')
      });
    }
  }

  const overallRating = ratingCount > 0 ? Math.round((ratingSum / ratingCount) * 10.0) / 10.0 : 5.0;

  const payload = {
    isAnonymous: false,
    overallRating: overallRating,
    answers: answers
  };

  try {
    showToast('Submitting', 'Submitting faculty evaluation...', 'info');
    try {
      await facultyAPI.submitFeedback(currentFacultyFormDetails.id, payload);
    } catch (apiErr) {
      if (!apiErr.isNetworkError) throw apiErr;
      // Demo mode: treat as successful offline submission
    }
    closeModal('facultyRespondModal');
    showToast('Evaluation Submitted', `Your feedback for "${currentFacultyFormDetails.title}" has been saved.`, 'success');
    loadFacultyDashboard();
  } catch (err) {
    console.error('Faculty submit response failed:', err);
    if (err.message && err.message.includes('already submitted')) {
      showToast('Duplicate Submission', 'You have already submitted feedback for this campaign.', 'error');
    } else {
      showToast('Submission Failed', err.message || 'Unable to submit feedback.', 'error');
    }
  }
}

/* ==========================================================================
   ADMIN DASHBOARD: FORM BUILDER, ASSIGNMENT & REAL ANALYTICS
   ========================================================================== */
function initAdminQuestionBuilder() {
  const container = document.getElementById('adminQuestionListContainer');
  if (!container) return;

  container.innerHTML = '';
  // Populate with 3 initial practical questions
  addQuestionBuilderRow("Rate overall course comprehension and lecture pacing (1-5 Stars)", "RATING", true);
  addQuestionBuilderRow("Were all syllabus learning outcomes and lab exercises clearly achieved?", "YES_NO", true);
  addQuestionBuilderRow("Please provide constructive recommendations for future improvements.", "TEXT", false);

  // Set default deadline date to +14 days
  const deadlineInput = document.getElementById('createFormDeadline');
  if (deadlineInput && !deadlineInput.value) {
    const d = new Date(Date.now() + 14 * 86400000);
    deadlineInput.value = d.toISOString().split('T')[0];
  }
}

function addQuestionBuilderRow(text = '', type = 'RATING', required = true) {
  const container = document.getElementById('adminQuestionListContainer');
  if (!container) return;

  const rowCount = container.querySelectorAll('.question-builder-row').length + 1;
  const row = document.createElement('div');
  row.className = 'question-builder-row';
  row.innerHTML = `
    <div class="question-builder-header">
      <strong style="font-size:0.875rem; color:var(--navy-900);" class="question-num-label">Question #${rowCount}</strong>
      <div style="display:flex; align-items:center; gap:12px;">
        <label class="checkbox-label" style="font-size:0.8125rem;">
          <input type="checkbox" class="question-required-check" ${required ? 'checked' : ''} style="accent-color:var(--blue-600);">
          <span>Required</span>
        </label>
        <button type="button" class="btn btn-ghost btn-sm" onclick="removeQuestionBuilderRow(this)" style="color:var(--status-red-text); padding:2px 6px;" title="Delete Question">
          🗑 Remove
        </button>
      </div>
    </div>
    <div style="display:grid; grid-template-columns: 2fr 1fr; gap:10px;">
      <input type="text" class="form-control question-text-input" placeholder="Enter question text..." value="${escapeHtml(text)}" required>
      <select class="form-control question-type-select">
        <option value="RATING" ${type === 'RATING' || type === 'STAR_RATING' ? 'selected' : ''}>⭐ Rating (1-5 Stars)</option>
        <option value="TEXT" ${type === 'TEXT' ? 'selected' : ''}>📝 Descriptive Text</option>
        <option value="YES_NO" ${type === 'YES_NO' ? 'selected' : ''}>✅ Yes / No</option>
        <option value="MULTIPLE_CHOICE" ${type === 'MULTIPLE_CHOICE' ? 'selected' : ''}>📋 Multiple Choice</option>
      </select>
    </div>
  `;

  container.appendChild(row);
  updateQuestionCount();
}

function removeQuestionBuilderRow(btn) {
  const row = btn.closest('.question-builder-row');
  if (row) {
    row.remove();
    // Re-index remaining questions
    const container = document.getElementById('adminQuestionListContainer');
    if (container) {
      container.querySelectorAll('.question-builder-row').forEach((r, idx) => {
        const lbl = r.querySelector('.question-num-label');
        if (lbl) lbl.textContent = `Question #${idx + 1}`;
      });
    }
    updateQuestionCount();
  }
}

function updateQuestionCount() {
  const container = document.getElementById('adminQuestionListContainer');
  const display = document.getElementById('questionCountDisplay');
  if (container && display) {
    display.textContent = container.querySelectorAll('.question-builder-row').length;
  }
}

// Admin Submit Feedback Form Creation (DRAFT or PUBLISHED)
async function submitCreateForm(status = 'PUBLISHED') {
  const titleInput = document.getElementById('createFormTitle');
  const descTextarea = document.getElementById('createFormDesc');
  const catSelect = document.getElementById('createFormCategory');
  const audienceSelect = document.getElementById('createFormAudience');
  const deptSelect = document.getElementById('createFormDepartment');
  const deadlineInput = document.getElementById('createFormDeadline');
  const anonymousCheck = document.getElementById('createFormAnonymous');

  const title = titleInput ? titleInput.value.trim() : '';
  const description = descTextarea ? descTextarea.value.trim() : '';
  const category = catSelect ? catSelect.value : 'COURSE';
  const targetAudience = audienceSelect ? audienceSelect.value : 'STUDENTS';
  const targetDepartment = deptSelect && deptSelect.value ? deptSelect.value : null;
  const deadlineVal = deadlineInput ? deadlineInput.value : '';
  const allowAnonymous = anonymousCheck ? anonymousCheck.checked : true;

  if (!title) {
    showToast('Validation Error', 'Please enter a feedback form title.', 'error');
    if (titleInput) titleInput.focus();
    return;
  }

  // Collect questions
  const questionRows = document.querySelectorAll('#adminQuestionListContainer .question-builder-row');
  const questions = [];

  let idx = 1;
  for (const row of questionRows) {
    const textInput = row.querySelector('.question-text-input');
    const typeSelect = row.querySelector('.question-type-select');
    const reqCheck = row.querySelector('.question-required-check');

    const qText = textInput ? textInput.value.trim() : '';
    if (!qText) {
      showToast('Validation Error', `Question #${idx} text cannot be empty.`, 'error');
      if (textInput) textInput.focus();
      return;
    }

    questions.push({
      questionText: qText,
      questionType: typeSelect ? typeSelect.value : 'RATING',
      required: reqCheck ? reqCheck.checked : false,
      displayOrder: idx
    });
    idx++;
  }

  if (status === 'PUBLISHED' && questions.length === 0) {
    showToast('Validation Error', 'A published form must contain at least 1 question.', 'error');
    return;
  }

  const payload = {
    title: title,
    description: description,
    category: category,
    targetAudience: targetAudience,
    targetDepartment: targetDepartment,
    status: status,
    deadline: deadlineVal ? `${deadlineVal}T23:59:59` : new Date(Date.now() + 14 * 86400000).toISOString(),
    allowAnonymous: allowAnonymous,
    questions: questions
  };

  try {
    showToast(status === 'DRAFT' ? 'Saving Draft' : 'Publishing Form', 'Processing campaign in database...', 'info');
    let created;
    try {
      created = await adminAPI.createFeedbackForm(payload);
    } catch (apiErr) {
      if (apiErr.isNetworkError) {
        created = { title: payload.title, id: 'demo-' + Date.now() };
      } else { throw apiErr; }
    }
    closeModal('createFeedbackModal');

    // Reset builder form
    if (titleInput) titleInput.value = '';
    if (descTextarea) descTextarea.value = '';
    initAdminQuestionBuilder();

    const successMsg = status === 'DRAFT'
      ? `Feedback draft "${created.title}" saved successfully.`
      : `Feedback campaign "${created.title}" published and assigned!`;
    showToast(status === 'DRAFT' ? 'Draft Saved' : 'Form Published', successMsg, 'success');

    loadAdminDashboard();
  } catch (err) {
    console.error('Create feedback failed:', err);
    showToast('Creation Failed', err.message || 'Could not create feedback form.', 'error');
  }
}

let cachedAdminComplaints = [];
let cachedAdminRequests = [];
let currentAdminIssueFilter = 'ALL';

async function loadAdminDashboard() {
  try {
    const user = AuthStore.getUser();
    if (user) {
      const nameEl = document.querySelector('.user-display-name');
      const roleEl = document.querySelector('.user-role-sub');
      if (nameEl) nameEl.textContent = user.fullName || 'Admin Office';
      if (roleEl) roleEl.textContent = user.department || 'Chief Academic Registrar';
    }

    // 1. Fetch Real Live Dashboard Metrics from backend
    try {
      const dashboard = await adminAPI.getDashboard().catch(err => {
        if (err.isNetworkError) return getDemoAdminDashboard();
        throw err;
      });
      if (dashboard) {
        const kpiFeedback = document.getElementById('kpiTotalFeedback');
        const kpiRating = document.getElementById('kpiAvgRating');
        const kpiComplaints = document.getElementById('kpiActiveComplaints');
        const kpiRequests = document.getElementById('kpiPendingRequests');
        const kpiHigh = document.getElementById('kpiHighPriority');

        if (kpiFeedback) kpiFeedback.textContent = dashboard.totalResponses ? `${dashboard.totalResponses.toLocaleString()} Responses` : '0 Responses';
        if (kpiRating) kpiRating.textContent = `${(dashboard.averageRating || 0.0).toFixed(1)} / 5`;
        if (kpiComplaints) kpiComplaints.textContent = dashboard.activeComplaints ?? dashboard.activeComplaintsCount ?? 0;
        if (kpiRequests) kpiRequests.textContent = dashboard.pendingRequests ?? dashboard.pendingRequestsCount ?? 0;
        if (kpiHigh) kpiHigh.textContent = (dashboard.highPriorityIssues ?? dashboard.highPriorityCount ?? 0) + (dashboard.escalatedIssues ?? dashboard.escalatedCount ?? 0);

        // Update sidebar nav badges
        const compNav = document.getElementById('adminComplaintsNavBadge');
        const reqNav = document.getElementById('adminRequestsNavBadge');
        if (compNav) compNav.textContent = dashboard.activeComplaints ?? dashboard.activeComplaintsCount ?? 0;
        if (reqNav) reqNav.textContent = dashboard.pendingRequests ?? dashboard.pendingRequestsCount ?? 0;
      }
    } catch (dErr) {
      console.warn('Dashboard metrics fetch fallback:', dErr);
    }

    // 2. Fetch All Feedback Forms
    try {
      const forms = await adminAPI.getAllFeedbackForms().catch(err => {
        if (err.isNetworkError) return getDemoAdminFeedbackForms();
        throw err;
      });
      renderAdminFeedbackFormsTable(forms || []);
      const countBadge = document.getElementById('adminFormsCountBadge');
      if (countBadge) countBadge.textContent = forms ? forms.length : 0;
    } catch (fErr) {
      console.error('Failed to load feedback forms:', fErr);
    }

    // 3. Fetch All Complaints & Requests for Issue Tracking
    try {
      const [complaints, requests] = await Promise.all([
        adminAPI.getAllComplaints().catch(err => err.isNetworkError ? getDemoAdminComplaints() : []),
        adminAPI.getAllRequests().catch(err => err.isNetworkError ? getDemoAdminRequests() : [])
      ]);

      cachedAdminComplaints = complaints || [];
      cachedAdminRequests = requests || [];

      renderAdminIssuesTable();
    } catch (iErr) {
      console.error('Failed to load complaints & requests:', iErr);
    }

  } catch (err) {
    console.error('Failed to load admin dashboard:', err);
  }
}

function filterAdminIssues(filterType) {
  currentAdminIssueFilter = filterType;

  // Update button active styles
  const btnMap = {
    'ALL': 'filterBtnAll',
    'COMPLAINT': 'filterBtnComplaint',
    'REQUEST': 'filterBtnRequest',
    'HIGH_PRIORITY': 'filterBtnHigh',
    'IN_PROGRESS': 'filterBtnInProgress',
    'RESOLVED': 'filterBtnResolved'
  };

  Object.keys(btnMap).forEach(key => {
    const btn = document.getElementById(btnMap[key]);
    if (btn) {
      if (key === filterType) {
        btn.classList.remove('btn-secondary');
        btn.classList.add('btn-primary');
      } else {
        btn.classList.remove('btn-primary');
        btn.classList.add('btn-secondary');
      }
    }
  });

  renderAdminIssuesTable();
}

function renderAdminIssuesTable() {
  const tbody = document.getElementById('adminRecentIssuesTableBody');
  if (!tbody) return;

  const unifiedList = [
    ...cachedAdminComplaints.map(c => ({
      id: c.id,
      type: 'COMPLAINT',
      ticketNumber: c.ticketNumber,
      category: c.category,
      title: c.subject || c.title || 'Untitled Grievance',
      description: c.description,
      priority: c.priority || 'MEDIUM',
      status: c.status,
      assignedCell: c.assignedCell || 'Pending Assignment',
      createdAt: c.createdAt,
      isPublic: c.publicVisible
    })),
    ...cachedAdminRequests.map(r => ({
      id: r.id,
      type: 'REQUEST',
      ticketNumber: r.requestNumber,
      category: r.category,
      title: r.title || 'Untitled Service Request',
      description: r.details || r.description,
      priority: 'MEDIUM',
      status: r.status,
      assignedCell: r.assignedCell || r.assignedDepartment || 'Pending Assignment',
      createdAt: r.createdAt,
      isPublic: false
    }))
  ].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

  // Apply Filter
  const filtered = unifiedList.filter(item => {
    if (currentAdminIssueFilter === 'COMPLAINT') return item.type === 'COMPLAINT';
    if (currentAdminIssueFilter === 'REQUEST') return item.type === 'REQUEST';
    if (currentAdminIssueFilter === 'HIGH_PRIORITY') return item.priority === 'HIGH' || item.priority === 'CRITICAL' || item.status === 'ESCALATED';
    if (currentAdminIssueFilter === 'IN_PROGRESS') return item.status === 'IN_PROGRESS' || item.status === 'APPROVED';
    if (currentAdminIssueFilter === 'RESOLVED') return item.status === 'RESOLVED' || item.status === 'CLOSED' || item.status === 'COMPLETED';
    return true; // 'ALL'
  });

  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="9" style="text-align:center; padding:24px; color:var(--text-muted);">No issues matching "${currentAdminIssueFilter}" filter.</td></tr>`;
    return;
  }

  tbody.innerHTML = filtered.map(issue => {
    const formattedTime = formatRelativeTime(issue.createdAt);
    
    // Status Badge
    let statusClass = 'badge-yellow';
    if (issue.status === 'RESOLVED' || issue.status === 'COMPLETED') statusClass = 'badge-green';
    else if (issue.status === 'CLOSED') statusClass = 'badge-neutral';
    else if (issue.status === 'REJECTED' || issue.status === 'ESCALATED') statusClass = 'badge-red';
    else if (issue.status === 'IN_PROGRESS') statusClass = 'badge-blue';

    // Priority Badge
    let prioClass = 'badge-yellow';
    if (issue.priority === 'HIGH' || issue.priority === 'CRITICAL') prioClass = 'badge-red';
    else if (issue.priority === 'LOW') prioClass = 'badge-green';

    const typeBadge = issue.type === 'COMPLAINT' 
      ? `<span class="badge badge-yellow" style="font-weight:700;">⚠ Complaint</span>` 
      : `<span class="badge badge-green" style="font-weight:700;">📩 Request</span>`;

    return `
      <tr>
        <td><strong>#${escapeHtml(issue.ticketNumber)}</strong></td>
        <td>${typeBadge}</td>
        <td><span class="badge badge-neutral">${escapeHtml(issue.category)}</span></td>
        <td>
          <strong>${escapeHtml(issue.title)}</strong>
          ${issue.description ? `<div style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(issue.description.substring(0, 45))}...</div>` : ''}
        </td>
        <td><span class="badge ${prioClass}">${escapeHtml(issue.priority)}</span></td>
        <td>
          <span class="badge ${statusClass}">
            <span class="badge-dot"></span> ${escapeHtml(issue.status)}
          </span>
        </td>
        <td><span style="font-size:0.8125rem; font-weight:600; color:var(--navy-900);">${escapeHtml(issue.assignedCell)}</span></td>
        <td style="color:var(--text-muted); font-size:0.8125rem;">${formattedTime}</td>
        <td>
          <button class="btn btn-secondary btn-sm" onclick="openAdminIssueModal('${issue.id}', '${issue.type}')" title="Manage Issue &amp; Verify">
            ⚙ Manage
          </button>
        </td>
      </tr>
    `;
  }).join('');
}

function renderAdminFeedbackFormsTable(forms) {
  const tbody = document.getElementById('adminFeedbackFormsTableBody');
  if (!tbody) return;

  if (!forms || forms.length === 0) {
    tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; padding:24px; color:var(--text-muted);">No feedback forms created yet. Click "+ Create New Form" to get started.</td></tr>`;
    return;
  }

  tbody.innerHTML = forms.map(f => {
    let statusBadge = '';
    if (f.status === 'PUBLISHED') {
      statusBadge = `<span class="badge badge-green"><span class="badge-dot"></span> Published</span>`;
    } else if (f.status === 'DRAFT') {
      statusBadge = `<span class="badge badge-yellow"><span class="badge-dot"></span> Draft</span>`;
    } else {
      statusBadge = `<span class="badge badge-neutral"><span class="badge-dot"></span> Closed</span>`;
    }

    const deadlineStr = f.deadline ? new Date(f.deadline).toLocaleDateString() : 'No deadline';
    const avgScore = f.averageRating ? f.averageRating.toFixed(1) : '—';
    const respCount = f.responseCount || 0;

    // Action buttons based on status
    let actionButtons = '';
    if (f.status === 'DRAFT') {
      actionButtons = `
        <div style="display:flex; gap:6px;">
          <button class="btn btn-accent btn-sm" onclick="publishDraftForm('${f.id}')" title="Publish this Draft">
            🚀 Publish
          </button>
          <button class="btn btn-ghost btn-sm" onclick="deleteFeedbackForm('${f.id}')" style="color:var(--status-red-text);" title="Delete Draft">
            🗑
          </button>
        </div>
      `;
    } else if (f.status === 'PUBLISHED') {
      actionButtons = `
        <div style="display:flex; gap:6px; flex-wrap:wrap;">
          <button class="btn btn-secondary btn-sm" onclick="openAssignModal('${f.id}', '${escapeHtml(f.title)}')" title="Assign to Users">
            👥 Assign
          </button>
          <button class="btn btn-secondary btn-sm" onclick="openFormResponsesModal('${f.id}', '${escapeHtml(f.title)}')" title="View Responses">
            💬 Responses (${respCount})
          </button>
          <button class="btn btn-secondary btn-sm" onclick="openFormAnalyticsModal('${f.id}', '${escapeHtml(f.title)}')" title="View Analytics">
            📊 Analytics
          </button>
          <button class="btn btn-ghost btn-sm" onclick="closeFeedbackCampaign('${f.id}')" style="color:var(--status-red-text);" title="Close Campaign">
            ⏹ Close
          </button>
        </div>
      `;
    } else {
      actionButtons = `
        <div style="display:flex; gap:6px;">
          <button class="btn btn-secondary btn-sm" onclick="openFormResponsesModal('${f.id}', '${escapeHtml(f.title)}')">
            💬 Responses (${respCount})
          </button>
          <button class="btn btn-secondary btn-sm" onclick="openFormAnalyticsModal('${f.id}', '${escapeHtml(f.title)}')">
            📊 Analytics
          </button>
        </div>
      `;
    }

    return `
      <tr>
        <td>
          <strong>${escapeHtml(f.title)}</strong>
          ${f.description ? `<div style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(f.description.substring(0, 50))}...</div>` : ''}
        </td>
        <td><span class="badge badge-blue">${escapeHtml(f.category || 'GENERAL')}</span></td>
        <td>${statusBadge}</td>
        <td><small>${escapeHtml(f.targetAudience || 'ALL')}${f.targetDepartment ? ` (${escapeHtml(f.targetDepartment)})` : ''}</small></td>
        <td><strong>${respCount}</strong></td>
        <td><span style="color:#F59E0B; font-weight:700;">★ ${avgScore}</span></td>
        <td style="font-size:0.8125rem; color:var(--text-muted);">${deadlineStr}</td>
        <td>${actionButtons}</td>
      </tr>
    `;
  }).join('');
}

// Admin Publish Draft Form
async function publishDraftForm(formId) {
  try {
    showToast('Publishing', 'Validating questions & activating campaign...', 'info');
    try { await adminAPI.publishFeedbackForm(formId); } catch (e) { if (!e.isNetworkError) throw e; }
    showToast('Campaign Active', 'The feedback form has been published and assigned to eligible users.', 'success');
    loadAdminDashboard();
  } catch (err) {
    console.error('Publish form failed:', err);
    showToast('Publish Failed', err.message || 'Cannot publish form.', 'error');
  }
}

// Admin Close Feedback Campaign
async function closeFeedbackCampaign(formId) {
  try {
    showToast('Closing', 'Ending submissions for this campaign...', 'info');
    try { await adminAPI.closeFeedbackForm(formId); } catch (e) { if (!e.isNetworkError) throw e; }
    showToast('Campaign Closed', 'Form status updated to CLOSED. No further submissions will be accepted.', 'success');
    loadAdminDashboard();
  } catch (err) {
    console.error('Close form failed:', err);
    showToast('Action Failed', err.message || 'Could not close feedback form.', 'error');
  }
}

// Admin Delete Form
async function deleteFeedbackForm(formId) {
  try {
    showToast('Deleting', 'Removing draft...', 'info');
    try { await adminAPI.deleteFeedbackForm(formId); } catch (e) { if (!e.isNetworkError) throw e; }
    showToast('Deleted', 'Feedback form deleted.', 'success');
    loadAdminDashboard();
  } catch (err) {
    console.error('Delete form failed:', err);
    showToast('Delete Failed', err.message || 'Could not delete feedback form.', 'error');
  }
}

// Admin Assign Modal
function openAssignModal(formId, title) {
  const idInput = document.getElementById('assignModalFormId');
  const titleDisplay = document.getElementById('assignModalFormTitle');
  if (idInput) idInput.value = formId;
  if (titleDisplay) titleDisplay.textContent = title;
  openModal('assignFeedbackModal');
}

async function submitFeedbackAssignment() {
  const formId = document.getElementById('assignModalFormId').value;
  const audience = document.getElementById('assignModalAudience').value;
  const department = document.getElementById('assignModalDepartment').value;
  const deadlineVal = document.getElementById('assignModalDeadline').value;

  if (!formId) return;

  const payload = {
    targetAudience: audience,
    targetDepartment: department || null,
    deadline: deadlineVal ? `${deadlineVal}T23:59:59` : null
  };

  try {
    showToast('Assigning', 'Dispatching assignments in PostgreSQL...', 'info');
    let result;
    try {
      result = await adminAPI.assignFeedback(formId, payload);
    } catch (apiErr) {
      if (apiErr.isNetworkError) {
        result = { assignedCount: 'target' };
      } else { throw apiErr; }
    }
    closeModal('assignFeedbackModal');
    showToast('Assigned', `Form assigned to ${result.assignedCount || 'target'} users.`, 'success');
    loadAdminDashboard();
  } catch (err) {
    console.error('Assign failed:', err);
    showToast('Assignment Failed', err.message || 'Could not assign feedback form.', 'error');
  }
}

// Admin View Responses Modal (with Anonymity Masking)
async function openFormResponsesModal(formId, title) {
  const titleEl = document.getElementById('viewResponsesModalTitle');
  const metaEl = document.getElementById('viewResponsesModalMeta');
  const container = document.getElementById('responsesListContainer');

  if (titleEl) titleEl.textContent = `Responses: ${title}`;
  if (metaEl) metaEl.textContent = 'Loading live responses from PostgreSQL...';
  if (container) container.innerHTML = `<div style="text-align:center; padding:20px; color:var(--text-muted);">Fetching submitted feedback...</div>`;

  openModal('viewResponsesModal');

  try {
    let responses;
    try {
      responses = await adminAPI.getFormResponses(formId);
    } catch (apiErr) {
      if (apiErr.isNetworkError) {
        responses = getDemoFormResponses(formId);
      } else { throw apiErr; }
    }
    if (!responses || responses.length === 0) {
      if (metaEl) metaEl.textContent = '0 Responses recorded';
      if (container) {
        container.innerHTML = `
          <div style="text-align:center; padding:40px; color:var(--text-muted);">
            <p style="font-weight:600;">No responses have been submitted for this campaign yet.</p>
          </div>`;
      }
      return;
    }

    if (metaEl) metaEl.textContent = `${responses.length} Total Submissions (Double-blind anonymity active)`;

    if (container) {
      container.innerHTML = responses.map((r, idx) => {
        const submittedDate = r.submittedAt ? formatRelativeTime(r.submittedAt) : 'Recently';
        const userHeader = r.isAnonymous 
          ? `<span class="badge badge-purple" style="font-weight:700;">🔒 Anonymous Response</span>` 
          : `<strong>${escapeHtml(r.userName || r.userIdentifier || 'Verified User')}</strong> <span style="font-size:0.75rem; color:var(--text-muted);">(${escapeHtml(r.userEmail || '')})</span>`;

        const stars = r.overallRating ? `★ ${r.overallRating.toFixed(1)} / 5.0` : '—';

        let answersHtml = '';
        if (r.answers && r.answers.length > 0) {
          answersHtml = r.answers.map((a, aIdx) => {
            const answerDisplay = a.ratingValue 
              ? `<span style="color:#F59E0B; font-weight:700;">${'★'.repeat(a.ratingValue)}${'☆'.repeat(5 - a.ratingValue)} (${a.ratingValue}/5)</span>`
              : `<span style="color:var(--navy-900);">${escapeHtml(a.textAnswer || '—')}</span>`;

            return `
              <div style="margin-top:8px; padding-top:8px; border-top:1px dashed var(--border-light); font-size:0.8125rem;">
                <div style="color:var(--text-secondary); font-weight:600;">${aIdx + 1}. ${escapeHtml(a.questionText || 'Criteria')}</div>
                <div style="margin-top:2px;">${answerDisplay}</div>
              </div>
            `;
          }).join('');
        }

        return `
          <div class="response-card-item">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px;">
              <div>${userHeader}</div>
              <div style="display:flex; align-items:center; gap:8px;">
                <span class="badge badge-yellow">${stars}</span>
                <span style="font-size:0.75rem; color:var(--text-muted);">${submittedDate}</span>
              </div>
            </div>
            ${answersHtml}
          </div>
        `;
      }).join('');
    }

  } catch (err) {
    console.error('Fetch responses failed:', err);
    if (container) {
      container.innerHTML = `<div style="text-align:center; padding:20px; color:var(--status-red-text);">Failed to load responses: ${escapeHtml(err.message)}</div>`;
    }
  }
}

// Admin View Form Analytics Modal
async function openFormAnalyticsModal(formId, title) {
  const titleEl = document.getElementById('formAnalyticsTitle');
  const subtitleEl = document.getElementById('formAnalyticsSubtitle');
  const totalEl = document.getElementById('analyticsTotalResponses');
  const rateEl = document.getElementById('analyticsResponseRate');
  const avgEl = document.getElementById('analyticsAvgRating');
  const distEl = document.getElementById('analyticsRatingDistribution');
  const qBreakdownEl = document.getElementById('analyticsQuestionsBreakdown');

  if (titleEl) titleEl.textContent = `Analytics: ${title}`;
  if (subtitleEl) subtitleEl.textContent = 'Calculating PostgreSQL metrics...';
  if (distEl) distEl.innerHTML = 'Loading distribution...';
  if (qBreakdownEl) qBreakdownEl.innerHTML = 'Loading questions breakdown...';

  openModal('formAnalyticsModal');

  try {
    let analytics;
    try {
      analytics = await adminAPI.getFeedbackAnalytics(formId);
    } catch (apiErr) {
      if (apiErr.isNetworkError) {
        analytics = getDemoFormAnalytics(formId);
      } else { throw apiErr; }
    }
    if (!analytics) return;

    if (subtitleEl) subtitleEl.textContent = `Status: ${analytics.status || 'PUBLISHED'} • Live PostgreSQL Aggregations`;
    if (totalEl) totalEl.textContent = analytics.responseCount || 0;
    if (rateEl) rateEl.textContent = `${analytics.responseRate ? analytics.responseRate.toFixed(1) : 0}%`;
    if (avgEl) avgEl.textContent = analytics.averageRating ? `${analytics.averageRating.toFixed(2)} / 5` : '0.0 / 5';

    // Rating distribution
    const dist = analytics.ratingDistribution || { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 };
    const totalResp = analytics.responseCount || 1;

    let distHtml = '';
    for (let star = 5; star >= 1; star--) {
      const count = dist[star] || 0;
      const pct = Math.round((count / (analytics.responseCount || 1)) * 100);
      distHtml += `
        <div class="rating-distribution-bar">
          <span style="width:30px; font-weight:700; color:#F59E0B;">${star} ★</span>
          <div class="rating-distribution-track">
            <div class="rating-distribution-fill" style="width:${pct}%;"></div>
          </div>
          <span style="width:50px; text-align:right; color:var(--text-muted);">${count} (${pct}%)</span>
        </div>
      `;
    }
    if (distEl) distEl.innerHTML = distHtml;

    // Questions breakdown
    if (analytics.questionBreakdown && analytics.questionBreakdown.length > 0) {
      let qHtml = '';
      analytics.questionBreakdown.forEach((q, qIdx) => {
        const scoreDisplay = q.averageRating 
          ? `<strong style="color:#F59E0B;">★ ${q.averageRating.toFixed(2)} / 5.0</strong>` 
          : `<span style="color:var(--blue-600);">${q.responseCount || 0} answers</span>`;

        qHtml += `
          <div style="background:var(--bg-subtle); border-radius:var(--radius-sm); padding:10px 14px; margin-bottom:8px; display:flex; justify-content:space-between; align-items:center;">
            <div style="font-size:0.875rem; color:var(--navy-900);">${qIdx + 1}. ${escapeHtml(q.questionText)}</div>
            <div style="font-size:0.875rem; margin-left:12px; white-space:nowrap;">${scoreDisplay}</div>
          </div>
        `;
      });
      if (qBreakdownEl) qBreakdownEl.innerHTML = qHtml;
    } else {
      if (qBreakdownEl) qBreakdownEl.innerHTML = `<p style="font-size:0.8125rem; color:var(--text-muted);">No question statistics available yet.</p>`;
    }

  } catch (err) {
    console.error('Fetch form analytics failed:', err);
    if (subtitleEl) subtitleEl.textContent = 'Failed to load analytics.';
  }
}

// Admin Update Complaint / Issue Status
let currentAdminIssueId = null;
let currentAdminIssueType = 'COMPLAINT';

async function openAdminIssueModal(id, type = 'COMPLAINT') {
  currentAdminIssueId = id;
  currentAdminIssueType = (type || 'COMPLAINT').toUpperCase();

  const idInput = document.getElementById('issueModalId');
  const typeInput = document.getElementById('issueModalType');
  const titleEl = document.getElementById('issueModalTitle');
  const subtitleEl = document.getElementById('issueModalSubtitle');
  const subjectEl = document.getElementById('issueModalSubject');
  const descEl = document.getElementById('issueModalDesc');
  const metaEl = document.getElementById('issueModalMeta');
  const statusSelect = document.getElementById('issueStatusUpdate');
  const prioritySelect = document.getElementById('issuePriorityUpdate');
  const assignedSelect = document.getElementById('issueAssignedCell');
  const publicContainer = document.getElementById('issuePublicVisibilityContainer');
  const publicToggle = document.getElementById('issuePublicToggle');
  const noteInput = document.getElementById('issueResolutionNote');
  const timelineEl = document.getElementById('adminIssueTimelineList');

  if (idInput) idInput.value = id;
  if (typeInput) typeInput.value = currentAdminIssueType;
  if (noteInput) noteInput.value = '';

  try {
    showToast('Loading Details', 'Fetching issue lifecycle data...', 'info');

    let issue = null;
    if (currentAdminIssueType === 'REQUEST') {
      try {
        issue = await adminAPI.getRequest(id);
      } catch (e) {
        if (e.isNetworkError) issue = getDemoIssue(id);
        else throw e;
      }
    } else {
      try {
        issue = await adminAPI.getComplaint(id);
      } catch (e) {
        if (e.isNetworkError) issue = getDemoIssue(id);
        else throw e;
      }
    }

    if (!issue) return;

    const ticketNumber = issue.ticketNumber || issue.requestNumber || id;
    if (titleEl) titleEl.innerText = `Issue #${ticketNumber} Management`;
    if (subtitleEl) subtitleEl.innerText = `Category: ${issue.category || 'General'} • Type: ${currentAdminIssueType}`;
    if (subjectEl) subjectEl.innerText = issue.title || issue.subject || 'Untitled Issue';
    if (descEl) descEl.innerText = issue.description || issue.details || 'No detailed description provided.';
    
    const studentInfo = issue.studentName ? `Lodged by ${issue.studentName} (${issue.studentIdentifier || ''})` : 'Lodged by Student';
    const slaInfo = issue.targetResolutionTime ? ` • Target SLA: ${new Date(issue.targetResolutionTime).toLocaleString()}` : '';
    if (metaEl) metaEl.innerText = `${studentInfo} • ${formatRelativeTime(issue.createdAt)}${slaInfo}`;

    // Status Select
    if (statusSelect && issue.status) {
      statusSelect.value = issue.status;
    }

    // Priority Select (Complaints)
    if (prioritySelect) {
      if (issue.priority) {
        prioritySelect.value = issue.priority;
      }
      prioritySelect.disabled = currentAdminIssueType === 'REQUEST';
    }

    // Assigned Cell
    if (assignedSelect && issue.assignedCell) {
      assignedSelect.value = issue.assignedCell;
    }

    // Public Toggle (Complaints only)
    if (publicContainer) {
      publicContainer.style.display = currentAdminIssueType === 'COMPLAINT' ? 'block' : 'none';
    }
    if (publicToggle) {
      publicToggle.checked = Boolean(issue.publicVisible);
    }

    // Timeline
    if (timelineEl) {
      if (!issue.updates || issue.updates.length === 0) {
        timelineEl.innerHTML = `
          <div class="timeline-step">
            <div class="timeline-dot completed"></div>
            <div class="timeline-content">
              <div class="timeline-header">
                <span class="timeline-title">${escapeHtml(issue.status || 'PENDING')}</span>
                <span class="timeline-time">${formatRelativeTime(issue.createdAt)}</span>
              </div>
              <div class="timeline-body">Ticket #${escapeHtml(ticketNumber)} registered.</div>
            </div>
          </div>
        `;
      } else {
        timelineEl.innerHTML = issue.updates.map((u, idx) => {
          const isLast = idx === issue.updates.length - 1;
          const statusText = u.statusUpdate || u.newStatus || issue.status || 'UPDATE';
          const isResolved = statusText === 'RESOLVED' || statusText === 'CLOSED' || statusText === 'COMPLETED';
          const dotClass = isResolved ? 'completed' : (isLast ? 'active' : 'completed');
          const timeStr = u.createdAt ? formatRelativeTime(u.createdAt) : 'Recently';
          const authorText = u.updatedByName ? `${u.updatedByName}${u.updatedByRole ? ` (${u.updatedByRole})` : ''}` : 'Admin';

          return `
            <div class="timeline-step">
              <div class="timeline-dot ${dotClass}"></div>
              <div class="timeline-content">
                <div class="timeline-header">
                  <span class="timeline-title">${escapeHtml(statusText)}</span>
                  <span class="timeline-time">${timeStr}</span>
                </div>
                <div class="timeline-body">${escapeHtml(u.comment || 'Status update logged')}</div>
                <div class="timeline-meta" style="margin-top:4px; font-size:0.75rem; color:var(--text-muted);">Author: <strong>${escapeHtml(authorText)}</strong></div>
              </div>
            </div>
          `;
        }).join('');
      }
    }

    openModal('issueDetailModal');
  } catch (err) {
    console.error('Failed to load issue details:', err);
    showToast('Lookup Error', err.message || 'Could not fetch issue details.', 'error');
  }
}

async function saveAdminIssueUpdate() {
  const statusSelect = document.getElementById('issueStatusUpdate');
  const prioritySelect = document.getElementById('issuePriorityUpdate');
  const cellSelect = document.getElementById('issueAssignedCell');
  const publicToggle = document.getElementById('issuePublicToggle');
  const noteTextarea = document.getElementById('issueResolutionNote');

  if (!currentAdminIssueId) {
    closeModal('issueDetailModal');
    return;
  }

  const newStatus = statusSelect ? statusSelect.value : 'IN_PROGRESS';
  const newPriority = prioritySelect ? prioritySelect.value : 'MEDIUM';
  const assignedCell = cellSelect ? cellSelect.value : 'Campus Facilities Cell';
  const isPublic = publicToggle ? publicToggle.checked : false;
  const note = noteTextarea ? noteTextarea.value.trim() : '';

  try {
    showToast('Saving Update', 'Recording issue status and audit log...', 'info');

    if (currentAdminIssueType === 'REQUEST') {
      try {
        await adminAPI.updateRequest(currentAdminIssueId, { status: newStatus, assignedCell: assignedCell, adminNote: note });
      } catch (e) { if (!e.isNetworkError) throw e; }
    } else {
      try {
        await adminAPI.updateComplaint(currentAdminIssueId, { status: newStatus, priority: newPriority, assignedCell: assignedCell, publicVisible: isPublic, adminNote: note });
      } catch (e) { if (!e.isNetworkError) throw e; }
    }

    closeModal('issueDetailModal');
    showToast('Changes Saved', `Issue updated successfully to "${newStatus}".`, 'success');
    loadAdminDashboard();
  } catch (err) {
    console.error('Save issue update failed:', err);
    showToast('Update Failed', err.message || 'Could not update issue.', 'error');
  }
}

async function verifyAndCloseAdminIssue() {
  const cellSelect = document.getElementById('issueAssignedCell');
  const noteTextarea = document.getElementById('issueResolutionNote');

  if (!currentAdminIssueId) {
    closeModal('issueDetailModal');
    return;
  }

  const assignedCell = cellSelect ? cellSelect.value : 'Campus Facilities Cell';
  const note = noteTextarea && noteTextarea.value.trim() 
    ? noteTextarea.value.trim() 
    : 'Resolution verified by administrative authority. Ticket closed.';

  try {
    showToast('Verifying Resolution', 'Closing ticket & archiving...', 'info');

    if (currentAdminIssueType === 'REQUEST') {
      try {
        await adminAPI.updateRequest(currentAdminIssueId, { status: 'COMPLETED', assignedCell: assignedCell, adminNote: note });
      } catch (e) { if (!e.isNetworkError) throw e; }
    } else {
      try {
        await adminAPI.updateComplaint(currentAdminIssueId, { status: 'CLOSED', assignedCell: assignedCell, publicVisible: false, adminNote: note });
      } catch (e) { if (!e.isNetworkError) throw e; }
    }

    closeModal('issueDetailModal');
    showToast('Verified & Closed', `Issue has been verified and closed.`, 'success');
    loadAdminDashboard();
  } catch (err) {
    console.error('Resolution verification failed:', err);
    showToast('Verification Failed', err.message || 'Could not close issue.', 'error');
  }
}

// Alias for backward compatibility
const saveIssueUpdate = saveAdminIssueUpdate;

/* ==========================================================================
   PASSWORD VISIBILITY TOGGLE
   ========================================================================== */
function initPasswordToggles() {
  const toggleBtns = document.querySelectorAll('.password-toggle-btn');
  toggleBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      const targetId = btn.getAttribute('data-target');
      const input = document.getElementById(targetId);
      if (!input) return;

      if (input.type === 'password') {
        input.type = 'text';
        btn.innerHTML = `
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
            <line x1="1" y1="1" x2="23" y2="23"></line>
          </svg>`;
      } else {
        input.type = 'password';
        btn.innerHTML = `
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
            <circle cx="12" cy="12" r="3"></circle>
          </svg>`;
      }
    });
  });
}

/* ==========================================================================
   MODAL MANAGEMENT
   ========================================================================== */
function initModals() {
  document.querySelectorAll('[data-close-modal]').forEach(btn => {
    btn.addEventListener('click', () => {
      const modal = btn.closest('.modal-overlay');
      if (modal) closeModal(modal.id);
    });
  });

  document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) {
        closeModal(overlay.id);
      }
    });
  });

  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      const activeModal = document.querySelector('.modal-overlay.active');
      if (activeModal) closeModal(activeModal.id);
    }
  });
}

function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
  }
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.remove('active');
    document.body.style.overflow = '';
  }
}

/* ==========================================================================
   INTERACTIVE STAR RATINGS
   ========================================================================== */
function initStarRatings() {
  document.querySelectorAll('.star-rating-input').forEach(group => {
    const stars = group.querySelectorAll('span');
    stars.forEach((star, idx) => {
      star.addEventListener('click', () => {
        stars.forEach((s, i) => {
          if (i <= idx) {
            s.classList.add('selected');
            s.textContent = '★';
          } else {
            s.classList.remove('selected');
            s.textContent = '☆';
          }
        });
        group.setAttribute('data-rating', idx + 1);
        const feedbackSmall = group.parentElement ? group.parentElement.querySelector('small') : null;
        if (feedbackSmall) {
          feedbackSmall.textContent = `Click stars to rate (${idx + 1} / 5)`;
        }
      });
    });
  });
}

/* ==========================================================================
   MOBILE SIDEBAR TOGGLE
   ========================================================================== */
function initMobileSidebar() {
  const toggleBtn = document.getElementById('mobileSidebarToggle');
  const sidebar = document.querySelector('.dashboard-sidebar');
  if (toggleBtn && sidebar) {
    toggleBtn.addEventListener('click', () => {
      sidebar.classList.toggle('mobile-open');
    });
  }
}

/* ==========================================================================
   TOAST NOTIFICATION HELPER
   ========================================================================== */
function showToast(title, message, type = 'success') {
  let container = document.getElementById('toastContainer');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toastContainer';
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  const iconSvg = type === 'success' 
    ? `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>`
    : type === 'error'
    ? `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#EF4444" stroke-width="2.5"><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg>`
    : `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#2563EB" stroke-width="2.5"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>`;

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `
    <div style="flex-shrink:0;">${iconSvg}</div>
    <div style="flex:1;">
      <div style="font-weight:700; font-size:0.875rem; color:var(--navy-900);">${escapeHtml(title)}</div>
      <div style="font-size:0.8125rem; color:var(--text-secondary); margin-top:2px;">${escapeHtml(message)}</div>
    </div>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

/* ==========================================================================
   UTILITY HELPERS
   ========================================================================== */
function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

function formatRelativeTime(dateStr) {
  if (!dateStr) return 'Recently';
  const diff = Date.now() - new Date(dateStr).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'Just now';
  if (mins < 60) return `${mins} min${mins > 1 ? 's' : ''} ago`;
  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours} hour${hours > 1 ? 's' : ''} ago`;
  const days = Math.floor(hours / 24);
  if (days === 1) return 'Yesterday';
  if (days < 7) return `${days} days ago`;
  return new Date(dateStr).toLocaleDateString();
}

/* ==========================================================================
   PUBLIC COMPLAINT PORTAL (Landing Page Feed)
   ========================================================================== */
async function loadPublicComplaintsFeed() {
  const tbody = document.getElementById('publicComplaintsTableBody');
  if (!tbody) return;

  try {
    const complaints = await publicAPI.getPublicComplaints();
    if (!complaints || complaints.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="8" style="text-align:center; padding:32px; color:var(--text-muted);">
            ✨ No active public grievances. All campus SLA standards are currently met!
          </td>
        </tr>`;
      return;
    }

    tbody.innerHTML = complaints.map(c => {
      const createdDate = c.createdAt ? new Date(c.createdAt).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' }) : 'Recent';
      const targetSla = c.targetResolutionTime ? new Date(c.targetResolutionTime).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' }) : '48 Hours';
      
      let priorityBadge = 'badge-yellow';
      if (c.priority === 'HIGH') priorityBadge = 'badge-yellow';
      else if (c.priority === 'CRITICAL') priorityBadge = 'badge-red';
      else if (c.priority === 'LOW') priorityBadge = 'badge-green';

      let statusBadge = 'badge-blue';
      if (c.status === 'RESOLVED') statusBadge = 'badge-green';
      else if (c.status === 'PENDING') statusBadge = 'badge-yellow';
      else if (c.status === 'ESCALATED') statusBadge = 'badge-red';

      return `
        <tr>
          <td><strong>#${escapeHtml(c.ticketNumber || 'TICK-N/A')}</strong></td>
          <td><span class="badge badge-neutral">${escapeHtml(c.category || 'General')}</span></td>
          <td><strong>${escapeHtml(c.title || c.subject || 'Campus Facility Issue')}</strong></td>
          <td><span class="badge ${priorityBadge}">${escapeHtml(c.priority || 'MEDIUM')}</span></td>
          <td><span class="badge ${statusBadge}"><span class="badge-dot"></span> ${escapeHtml(c.status || 'IN_PROGRESS')}</span></td>
          <td><span class="badge badge-blue">${escapeHtml(c.assignedCell || 'Facilities Cell')}</span></td>
          <td style="color:var(--text-muted); font-size:0.8125rem;">${createdDate}</td>
          <td style="color:var(--text-muted); font-size:0.8125rem;">${targetSla}</td>
        </tr>
      `;
    }).join('');
  } catch (err) {
    console.error('Failed to load public complaints:', err);
    tbody.innerHTML = `
      <tr>
        <td colspan="8" style="text-align:center; padding:24px; color:var(--text-muted);">
          Could not load live public grievances.
        </td>
      </tr>`;
  }
}

/* ==========================================================================
   NOTIFICATIONS MANAGEMENT (Unified Across All Portals)
   ========================================================================== */
async function syncUnreadNotifications() {
  const dot = document.getElementById('topbarUnreadDot');
  if (!dot) return;

  try {
    const res = await notificationAPI.getUnreadCount();
    const count = res && (res.unreadCount ?? res.count ?? 0);
    if (count > 0) {
      dot.style.display = 'block';
    } else {
      dot.style.display = 'none';
    }
  } catch (err) {
    // Backend offline or unauthenticated — hide dot silently
    dot.style.display = 'none';
  }
}

async function openNotificationsModal() {
  openModal('notificationsModal');
  loadNotificationsList();
}

async function loadNotificationsList() {
  const container = document.getElementById('notificationsListContainer');
  const subtitle = document.getElementById('notificationsModalSubtitle');
  if (!container) return;

  container.innerHTML = `<div style="text-align:center; padding:24px; color:var(--text-muted);">Fetching notifications...</div>`;

  try {
    let list;
    try {
      list = await notificationAPI.getNotifications();
    } catch (apiErr) {
      if (apiErr.isNetworkError) {
        list = getDemoNotifications();
      } else { throw apiErr; }
    }
    if (!list || list.length === 0) {
      if (subtitle) subtitle.textContent = '0 unread notifications';
      container.innerHTML = `
        <div style="text-align:center; padding:32px; color:var(--text-muted);">
          <p style="font-weight:600; margin:0;">🎉 You are all caught up!</p>
          <small>No pending alerts or notifications at this time.</small>
        </div>`;
      syncUnreadNotifications();
      return;
    }

    const unreadCount = list.filter(n => !n.isRead).length;
    if (subtitle) subtitle.textContent = `${unreadCount} unread notification${unreadCount === 1 ? '' : 's'}`;

    container.innerHTML = list.map(n => {
      const timeStr = n.createdAt ? formatRelativeTime(n.createdAt) : 'Recently';
      const isUnread = !n.isRead;

      return `
        <div class="notification-item-card" style="background:${isUnread ? 'var(--blue-50, #EFF6FF)' : 'var(--bg-subtle)'}; border:1px solid ${isUnread ? 'var(--blue-200, #BFDBFE)' : 'var(--border-subtle)'}; border-radius:var(--radius-md); padding:12px 16px; display:flex; justify-content:space-between; align-items:flex-start; gap:12px;">
          <div style="flex:1;">
            <div style="display:flex; align-items:center; gap:8px; margin-bottom:4px;">
              <strong style="font-size:0.875rem; color:var(--navy-900);">${escapeHtml(n.title || 'Notification')}</strong>
              ${isUnread ? `<span class="badge badge-blue" style="font-size:0.625rem; padding:2px 6px;">New</span>` : ''}
            </div>
            <div style="font-size:0.8125rem; color:var(--text-secondary); line-height:1.4;">${escapeHtml(n.message || '')}</div>
            <div style="font-size:0.75rem; color:var(--text-muted); margin-top:6px;">${timeStr}</div>
          </div>
          ${isUnread ? `
            <button class="btn btn-ghost btn-sm" onclick="markNotificationAsRead('${n.id}')" style="font-size:0.75rem; color:var(--blue-600); white-space:nowrap;">
              Mark Read
            </button>
          ` : ''}
        </div>
      `;
    }).join('');

    syncUnreadNotifications();
  } catch (err) {
    console.error('Failed to load notifications:', err);
    container.innerHTML = `<div style="text-align:center; padding:20px; color:var(--status-red-text);">Failed to load notifications: ${escapeHtml(err.message)}</div>`;
  }
}

async function markNotificationAsRead(id) {
  try {
    try { await notificationAPI.markRead(id); } catch (e) { /* offline ok */ }
    loadNotificationsList();
  } catch (err) {
    console.error('Mark read failed:', err);
  }
}

async function markAllNotificationsAsRead() {
  try {
    showToast('Updating', 'Marking all notifications as read...', 'info');
    try { await notificationAPI.markAllRead(); } catch (e) { /* offline ok */ }
    showToast('Done', 'All notifications marked as read.', 'success');
    loadNotificationsList();
  } catch (err) {
    console.error('Mark all read failed:', err);
  }
}

/* ==========================================================================
   REPORTS MANAGEMENT SUITE (All 7 Types, PDF, Excel, Print)
   ========================================================================== */
let currentGeneratedReportData = null;

function openReportsModal() {
  openModal('reportsModal');
  executeGenerateReport();
}

async function executeGenerateReport() {
  const typeSelect = document.getElementById('reportTypeSelect');
  const deptSelect = document.getElementById('reportDeptSelect');
  const catSelect = document.getElementById('reportCategorySelect');
  const termSelect = document.getElementById('reportTermSelect');
  const preview = document.getElementById('reportPreviewContainer');

  if (!preview) return;

  const reportType = typeSelect ? typeSelect.value : 'OVERALL';
  const department = deptSelect && deptSelect.value ? deptSelect.value : null;
  const category = catSelect && catSelect.value ? catSelect.value : null;
  const term = termSelect && termSelect.value ? termSelect.value : null;

  preview.innerHTML = `<div style="text-align:center; padding:32px; color:var(--text-muted);">Generating report data from PostgreSQL database...</div>`;

  try {
    const payload = {
      reportType: reportType,
      department: department,
      category: category,
      term: term
    };

    let reportData;
    try {
      reportData = await adminAPI.generateReport(payload);
    } catch (apiErr) {
      if (apiErr.isNetworkError) {
        reportData = getDemoReportData(payload);
      } else { throw apiErr; }
    }
    currentGeneratedReportData = reportData;

    renderReportPreview(preview, reportData);
  } catch (err) {
    console.error('Generate report failed:', err);
    preview.innerHTML = `<div style="text-align:center; padding:24px; color:var(--status-red-text);">Failed to generate report: ${escapeHtml(err.message)}</div>`;
  }
}

function renderReportPreview(container, data) {
  if (!data) return;

  const dateStr = data.generatedAt ? new Date(data.generatedAt).toLocaleString() : 'Now';

  // 1. KPI Summary Cards Grid
  let metricsHtml = '';
  if (data.summaryMetrics && Object.keys(data.summaryMetrics).length > 0) {
    metricsHtml = `
      <div style="display:grid; grid-template-columns:repeat(auto-fit, minmax(160px, 1fr)); gap:12px; margin-bottom:20px;">
        ${Object.entries(data.summaryMetrics).map(([k, v]) => `
          <div style="background:var(--bg-subtle); padding:12px 14px; border-radius:var(--radius-sm); border:1px solid var(--border-subtle); text-align:center;">
            <div style="font-size:0.6875rem; font-weight:700; color:var(--text-muted); text-transform:uppercase;">${escapeHtml(k)}</div>
            <div style="font-size:1.15rem; font-weight:800; color:var(--navy-900); margin-top:2px;">${escapeHtml(String(v))}</div>
          </div>
        `).join('')}
      </div>
    `;
  }

  // 2. Data Rows Table
  let tableHtml = '';
  if (data.headers && data.headers.length > 0) {
    tableHtml = `
      <div style="margin-bottom:20px; overflow-x:auto;">
        <table class="custom-table" style="font-size:0.8125rem;">
          <thead>
            <tr>
              ${data.headers.map(h => `<th>${escapeHtml(h)}</th>`).join('')}
            </tr>
          </thead>
          <tbody>
            ${(data.rows && data.rows.length > 0) ? data.rows.map(r => `
              <tr>
                ${r.map(c => `<td>${escapeHtml(c)}</td>`).join('')}
              </tr>
            `).join('') : `<tr><td colspan="${data.headers.length}" style="text-align:center; color:var(--text-muted);">No records found for the selected criteria.</td></tr>`}
          </tbody>
        </table>
      </div>
    `;
  }

  // 3. AI Recommendations Box
  let recsHtml = '';
  if (data.recommendations && data.recommendations.length > 0) {
    recsHtml = `
      <div style="background:var(--status-purple-bg); border:1px solid rgba(139, 92, 246, 0.3); border-radius:var(--radius-sm); padding:14px 16px;">
        <div style="font-size:0.8125rem; font-weight:700; color:#6D28D9; margin-bottom:8px; display:flex; align-items:center; gap:6px;">
          <span>✦</span> AI Executive Action Recommendations
        </div>
        <ol style="margin:0; padding-left:20px; font-size:0.8125rem; color:var(--navy-900); line-height:1.6;">
          ${data.recommendations.map(rec => `<li>${escapeHtml(rec)}</li>`).join('')}
        </ol>
      </div>
    `;
  }

  container.innerHTML = `
    <div>
      <div style="border-bottom:1px solid var(--border-subtle); padding-bottom:12px; margin-bottom:16px;">
        <div style="display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:8px;">
          <div>
            <h3 style="margin:0; font-size:1.15rem; color:var(--navy-900);">${escapeHtml(data.title)}</h3>
            <div style="font-size:0.75rem; color:var(--text-secondary); margin-top:2px;">${escapeHtml(data.subtitle)}</div>
          </div>
          <span class="badge badge-blue">Official College Report</span>
        </div>
        <div style="font-size:0.6875rem; color:var(--text-muted); margin-top:6px;">
          Generated by: <strong>${escapeHtml(data.generatedBy)}</strong> • Timestamp: ${dateStr}
        </div>
      </div>

      ${metricsHtml}
      ${tableHtml}
      ${recsHtml}
    </div>
  `;
}

function getReportFilterParams() {
  const typeSelect = document.getElementById('reportTypeSelect');
  const deptSelect = document.getElementById('reportDeptSelect');
  const catSelect = document.getElementById('reportCategorySelect');
  const termSelect = document.getElementById('reportTermSelect');

  return {
    reportType: typeSelect ? typeSelect.value : 'OVERALL',
    department: deptSelect && deptSelect.value ? deptSelect.value : '',
    category: catSelect && catSelect.value ? catSelect.value : '',
    term: termSelect && termSelect.value ? termSelect.value : ''
  };
}

async function executeDownloadReportPdf() {
  const params = getReportFilterParams();
  try {
    showToast('Exporting PDF', 'Compiling PDF document...', 'info');
    try {
      await adminAPI.downloadReportPdf(params.reportType, params);
      showToast('Download Started', `CFMS ${params.reportType} PDF report ready.`, 'success');
    } catch (apiErr) {
      if (apiErr.isNetworkError) {
        showToast('Offline Mode', 'PDF export requires the backend server. Please start the Spring Boot application.', 'info');
      } else { throw apiErr; }
    }
  } catch (err) {
    console.error('PDF export failed:', err);
    showToast('Export Failed', err.message || 'Could not generate PDF.', 'error');
  }
}

async function executeDownloadReportExcel() {
  const params = getReportFilterParams();
  try {
    showToast('Exporting Excel', 'Generating Excel spreadsheet...', 'info');
    try {
      await adminAPI.downloadReportExcel(params.reportType, params);
      showToast('Download Started', `CFMS ${params.reportType} Excel file ready.`, 'success');
    } catch (apiErr) {
      if (apiErr.isNetworkError) {
        showToast('Offline Mode', 'Excel export requires the backend server. Please start the Spring Boot application.', 'info');
      } else { throw apiErr; }
    }
  } catch (err) {
    console.error('Excel export failed:', err);
    showToast('Export Failed', err.message || 'Could not generate Excel spreadsheet.', 'error');
  }
}

function executePrintReport() {
  const preview = document.getElementById('reportPreviewContainer');
  if (!preview) return;

  const printWindow = window.open('', '_blank', 'width=900,height=700');
  if (!printWindow) {
    window.print();
    return;
  }

  printWindow.document.write(`
    <!DOCTYPE html>
    <html>
      <head>
        <title>College Feedback Management System — Report Print</title>
        <style>
          body { font-family: 'Inter', system-ui, sans-serif; padding: 24px; color: #0F172A; }
          table { width: 100%; border-collapse: collapse; margin-top: 14px; margin-bottom: 14px; font-size: 12px; }
          th, td { border: 1px solid #CBD5E1; padding: 8px 10px; text-align: left; }
          th { background: #2563EB; color: #FFFFFF; font-weight: bold; }
          tr:nth-child(even) { background: #F8FAFC; }
          .badge { display: inline-block; padding: 3px 8px; border-radius: 4px; font-size: 11px; font-weight: bold; }
          .badge-blue { background: #DBEAFE; color: #1E40AF; }
          @media print {
            @page { margin: 1.5cm; }
          }
        </style>
      </head>
      <body>
        ${preview.innerHTML}
        <script>
          window.onload = function() { window.print(); window.close(); }
        </script>
      </body>
    </html>
  `);
  printWindow.document.close();
}

// Global Exports
window.showToast = showToast;
window.openModal = openModal;
window.closeModal = closeModal;
window.submitFeedbackResponse = submitFeedbackResponse;
window.submitComplaint = submitComplaint;
window.submitRequest = submitRequest;
window.submitCreateForm = submitCreateForm;
window.saveIssueUpdate = saveIssueUpdate;
window.saveAdminIssueUpdate = saveAdminIssueUpdate;
window.verifyAndCloseAdminIssue = verifyAndCloseAdminIssue;
window.openStudentRespondModal = openStudentRespondModal;
window.openStudentTrackModal = openStudentTrackModal;
window.openAdminIssueModal = openAdminIssueModal;
window.filterAdminIssues = filterAdminIssues;
window.loadPublicComplaintsFeed = loadPublicComplaintsFeed;
window.openNotificationsModal = openNotificationsModal;
window.markNotificationAsRead = markNotificationAsRead;
window.markAllNotificationsAsRead = markAllNotificationsAsRead;
window.openReportsModal = openReportsModal;
window.executeGenerateReport = executeGenerateReport;
window.executeDownloadReportPdf = executeDownloadReportPdf;
window.executeDownloadReportExcel = executeDownloadReportExcel;
window.executePrintReport = executePrintReport;



/* ==========================================================================
   DEMO DATA HELPERS — used when Spring Boot backend is offline
   All functions return realistic data matching the real API response shape.
   ========================================================================== */

/** Returns a demo feedback form with realistic questions */
function getDemoFeedbackForm(formId) {
  const forms = {
    'demo-1': { id: 'demo-1', title: 'Data Science – Faculty Feedback',              category: 'FACULTY',   deadline: '2026-09-15', allowAnonymous: true },
    'demo-2': { id: 'demo-2', title: 'Infrastructure Feedback',                       category: 'FACILITY',  deadline: '2026-09-18', allowAnonymous: true },
    'demo-3': { id: 'demo-3', title: 'Elective Course Review: AI & Machine Learning', category: 'ACADEMIC',  deadline: '2026-09-22', allowAnonymous: true }
  };
  const base = forms[formId] || { id: formId, title: 'Course Feedback', category: 'ACADEMIC', deadline: '2026-09-30', allowAnonymous: true };

  base.questions = [
    { id: 'q1', questionType: 'STAR_RATING',      questionText: 'Rate the overall quality of teaching and lecture clarity.',                              required: true  },
    { id: 'q2', questionType: 'STAR_RATING',      questionText: 'Rate the practical relevance of the course content and assignments.',                   required: true  },
    { id: 'q3', questionType: 'YES_NO',            questionText: 'Were all syllabus topics and lab exercises clearly covered within the term?',           required: true  },
    { id: 'q4', questionType: 'MULTIPLE_CHOICE',   questionText: 'How would you rate the faculty\'s communication and responsiveness to student queries?', required: true  },
    { id: 'q5', questionType: 'TEXT',              questionText: 'Please provide constructive suggestions for improving this course.',                     required: false }
  ];
  return base;
}

/** Returns a demo issue (complaint or request) for the Track modal */
function getDemoIssue(id) {
  const demoIssues = {
    'c1': { id: 'c1', ticketNumber: 'TICK-0101', category: 'INFRASTRUCTURE', title: 'AC Malfunction in Lab 3',   subject: 'AC Malfunction in Lab 3',   description: 'The air conditioning unit in Computer Lab 3 has been malfunctioning for the past week. The temperature is affecting student productivity and equipment health.', status: 'IN_PROGRESS', priority: 'HIGH',   assignedCell: 'Campus Facilities Cell', createdAt: new Date(Date.now() - 2 * 86400000).toISOString() },
    'c2': { id: 'c2', ticketNumber: 'TICK-0098', category: 'IT',             title: 'Wi-Fi connectivity issues', subject: 'Wi-Fi connectivity issues',   description: 'Wi-Fi connectivity in Block C is intermittent. Multiple students are unable to access online course materials during lectures.',                              status: 'PENDING',     priority: 'MEDIUM', assignedCell: 'IT Support Cell',        createdAt: new Date(Date.now() - 5 * 86400000).toISOString() },
    'r1': { id: 'r1', requestNumber: 'REQ-0045',  category: 'DOCUMENT',      title: 'Bonafide Certificate',       subject: 'Bonafide Certificate',        details: 'Requesting bonafide certificate for internship application at Tech Corp. Required by 20 Sept 2026.',                                                             status: 'APPROVED',    priority: 'MEDIUM', assignedCell: 'Academic Records Office', createdAt: new Date(Date.now() - 3 * 86400000).toISOString() }
  };

  const issue = demoIssues[id] || {
    id: id, ticketNumber: 'TICK-' + id.toUpperCase(), category: 'GENERAL',
    title: 'Issue #' + id, subject: 'Issue #' + id,
    description: 'Details for this issue.', status: 'PENDING', priority: 'MEDIUM',
    assignedCell: 'Admin Office', createdAt: new Date(Date.now() - 86400000).toISOString()
  };

  issue.updates = [
    { statusUpdate: issue.status === 'RESOLVED' ? 'RESOLVED' : 'PENDING', comment: 'Ticket registered and queued for review.', createdAt: issue.createdAt, updatedByName: 'System', updatedByRole: 'AUTO' },
    ...(issue.status === 'IN_PROGRESS' || issue.status === 'APPROVED' ? [{ statusUpdate: issue.status, comment: 'Assigned to relevant cell and under active review.', createdAt: new Date(Date.now() - 86400000).toISOString(), updatedByName: 'Admin Office', updatedByRole: 'ADMIN' }] : [])
  ];
  return issue;
}

/** Faculty dashboard KPI demo data */
function getDemoFacultyDashboard() {
  return { pendingFeedbackCount: 2, averageRating: 4.4, studentResponseRate: 82, syllabusMilestones: 96 };
}

/** Faculty assigned feedback demo list */
function getDemoFacultyFeedbacks() {
  return [
    { id: 'demo-1', title: 'End-Term Faculty Evaluation – Data Science',    category: 'FACULTY',  deadline: '2026-09-20', userStatus: 'PENDING' },
    { id: 'demo-2', title: 'Mid-Term Course Satisfaction Survey',           category: 'ACADEMIC', deadline: '2026-09-25', userStatus: 'PENDING' }
  ];
}

/** Admin dashboard KPI demo data */
function getDemoAdminDashboard() {
  return {
    totalResponses: 1247, averageRating: 4.2,
    activeComplaints: 18, activeComplaintsCount: 18,
    pendingRequests: 11, pendingRequestsCount: 11,
    highPriorityIssues: 4, escalatedIssues: 2
  };
}

/** Admin feedback forms table demo data */
function getDemoAdminFeedbackForms() {
  return [
    { id: 'f1', title: 'End-Term Faculty Evaluation – CS Dept',     category: 'FACULTY',  status: 'PUBLISHED', targetAudience: 'STUDENT', deadline: '2026-09-20', responseCount: 87,  averageRating: 4.3 },
    { id: 'f2', title: 'Infrastructure Satisfaction Survey',         category: 'FACILITY', status: 'PUBLISHED', targetAudience: 'ALL',     deadline: '2026-09-18', responseCount: 134, averageRating: 3.8 },
    { id: 'f3', title: 'AI & ML Elective Course Review',             category: 'ACADEMIC', status: 'PUBLISHED', targetAudience: 'STUDENT', deadline: '2026-09-22', responseCount: 56,  averageRating: 4.6 },
    { id: 'f4', title: 'Faculty Self-Assessment – Research Output',  category: 'FACULTY',  status: 'DRAFT',     targetAudience: 'FACULTY', deadline: '2026-10-05', responseCount: 0,   averageRating: 0   }
  ];
}

/** Admin complaints demo list */
function getDemoAdminComplaints() {
  return [
    { id: 'c1', ticketNumber: 'TICK-0101', category: 'INFRASTRUCTURE', subject: 'AC Malfunction in Lab 3',     status: 'IN_PROGRESS', priority: 'HIGH',   assignedCell: 'Campus Facilities Cell', createdAt: new Date(Date.now() - 2 * 86400000).toISOString(), studentName: 'Aarav Sharma',   studentIdentifier: 'CS-2024-042' },
    { id: 'c2', ticketNumber: 'TICK-0098', category: 'IT',             subject: 'Wi-Fi connectivity issues',   status: 'PENDING',     priority: 'MEDIUM', assignedCell: 'IT Support Cell',        createdAt: new Date(Date.now() - 5 * 86400000).toISOString(), studentName: 'Priya Nair',     studentIdentifier: 'CS-2024-019' },
    { id: 'c3', ticketNumber: 'TICK-0095', category: 'ACADEMIC',       subject: 'Incorrect marks in mid-term', status: 'RESOLVED',    priority: 'HIGH',   assignedCell: 'Examination Cell',       createdAt: new Date(Date.now() - 8 * 86400000).toISOString(), studentName: 'Rohit Verma',    studentIdentifier: 'CS-2023-088' }
  ];
}

/** Admin requests demo list */
function getDemoAdminRequests() {
  return [
    { id: 'r1', requestNumber: 'REQ-0045', category: 'DOCUMENT', title: 'Bonafide Certificate for Internship', status: 'APPROVED',   assignedCell: 'Academic Records Office', createdAt: new Date(Date.now() - 3 * 86400000).toISOString() },
    { id: 'r2', requestNumber: 'REQ-0043', category: 'LIBRARY',  title: 'Extended Library Access – Final Year', status: 'PENDING',   assignedCell: 'Library Cell',           createdAt: new Date(Date.now() - 6 * 86400000).toISOString() },
    { id: 'r3', requestNumber: 'REQ-0040', category: 'SPORTS',   title: 'Permission for Sports Facility',       status: 'COMPLETED', assignedCell: 'Student Affairs Cell',    createdAt: new Date(Date.now() - 10 * 86400000).toISOString() }
  ];
}

/** Demo form responses for the viewFormResponses modal */
function getDemoFormResponses(formId) {
  return [
    {
      id: 'r1', isAnonymous: false, overallRating: 4.5,
      userName: 'Aarav Sharma', userIdentifier: 'CS-2024-042', userEmail: 'aarav@example.com',
      submittedAt: new Date(Date.now() - 86400000).toISOString(),
      answers: [
        { questionText: 'Rate overall teaching quality.',       ratingValue: 5, textAnswer: null },
        { questionText: 'Rate practical course relevance.',     ratingValue: 4, textAnswer: null },
        { questionText: 'Were all topics covered?',             ratingValue: null, textAnswer: 'Yes' },
        { questionText: 'Faculty communication rating.',        ratingValue: null, textAnswer: 'Excellent' },
        { questionText: 'Suggestions for improvement.',         ratingValue: null, textAnswer: 'More hands-on labs would be beneficial.' }
      ]
    },
    {
      id: 'r2', isAnonymous: true, overallRating: 3.5,
      userName: null, userIdentifier: null, userEmail: null,
      submittedAt: new Date(Date.now() - 2 * 86400000).toISOString(),
      answers: [
        { questionText: 'Rate overall teaching quality.',       ratingValue: 3, textAnswer: null },
        { questionText: 'Rate practical course relevance.',     ratingValue: 4, textAnswer: null },
        { questionText: 'Were all topics covered?',             ratingValue: null, textAnswer: 'No' },
        { questionText: 'Faculty communication rating.',        ratingValue: null, textAnswer: 'Average' },
        { questionText: 'Suggestions for improvement.',         ratingValue: null, textAnswer: 'Lectures could be paced better for complex topics.' }
      ]
    }
  ];
}

/** Demo analytics data for the openFormAnalyticsModal */
function getDemoFormAnalytics(formId) {
  return {
    status: 'PUBLISHED', responseCount: 87, responseRate: 72.5, averageRating: 4.3,
    ratingDistribution: { 5: 42, 4: 28, 3: 11, 2: 4, 1: 2 },
    questionBreakdown: [
      { questionText: 'Rate overall teaching quality.',       averageRating: 4.5, responseCount: 87 },
      { questionText: 'Rate practical course relevance.',     averageRating: 4.1, responseCount: 87 },
      { questionText: 'Were all topics covered?',             averageRating: null, responseCount: 87 },
      { questionText: 'Faculty communication rating.',        averageRating: null, responseCount: 87 },
      { questionText: 'Suggestions for improvement.',         averageRating: null, responseCount: 63 }
    ]
  };
}

/** Demo notifications list */
function getDemoNotifications() {
  return [
    { id: 'n1', title: 'New Feedback Assigned',   message: 'Data Science – Faculty Feedback has been assigned to you. Deadline: 15 Sept 2026.', isRead: false, createdAt: new Date(Date.now() - 3600000).toISOString() },
    { id: 'n2', title: 'Complaint Update',         message: 'Your complaint TICK-0101 has been assigned to the Campus Facilities Cell.',         isRead: false, createdAt: new Date(Date.now() - 7200000).toISOString() },
    { id: 'n3', title: 'Request Approved',         message: 'Your bonafide certificate request REQ-0045 has been approved.',                     isRead: true,  createdAt: new Date(Date.now() - 86400000).toISOString() },
    { id: 'n4', title: 'Reminder: Pending Forms',  message: 'You have 3 feedback forms due within the next 7 days.',                             isRead: true,  createdAt: new Date(Date.now() - 2 * 86400000).toISOString() }
  ];
}

/** Demo report data for generateReport */
function getDemoReportData(payload) {
  const type = (payload.reportType || 'OVERALL').toUpperCase();
  return {
    reportType: type,
    department: payload.department || 'Computer Science',
    term: payload.term || 'Fall 2026',
    generatedAt: new Date().toISOString(),
    summaryMetrics: {
      'Total Responses': 1247,
      'Avg Rating': '4.2 / 5',
      'Active Complaints': 18,
      'Pending Requests': 11,
      'Response Rate': '74.3%'
    },
    headers: ['Department', 'Faculty', 'Course', 'Responses', 'Avg Rating', 'Status'],
    rows: [
      ['Computer Science', 'Dr. Vikram Malhotra', 'Data Science',          '87',  '4.3', 'PUBLISHED'],
      ['Computer Science', 'Dr. Ananya Iyer',     'AI & Machine Learning', '56',  '4.6', 'PUBLISHED'],
      ['Computer Science', 'Prof. Suresh Kumar',  'DBMS & Data Modeling',  '102', '3.9', 'PUBLISHED'],
      ['Mathematics',      'Dr. Riya Patel',       'Discrete Mathematics',  '78',  '4.1', 'PUBLISHED']
    ],
    recommendations: [
      'Schedule infrastructure maintenance for Block B facilities based on high complaint frequency.',
      'Recognize Dr. Ananya Iyer for the highest average rating of 4.6 — consider sharing best practices.',
      'Improve Wi-Fi infrastructure in Block C — 14 complaints flagged this term.',
      'Increase bonafide certificate processing speed — average resolution time is 3 days against 1-day SLA.'
    ]
  };
}
