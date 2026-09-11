/**
 * College Student Feedback Management System
 * Centralized Frontend API Client Layer
 * Handles authentication, token management, Bearer authorization header injection,
 * and unified REST endpoints for Student, Faculty, and Admin portals.
 */

const API_BASE_URL = window.API_BASE_URL || 'http://localhost:8081/api';

// ============================================================================
// TOKEN & SESSION MANAGEMENT
// ============================================================================
const AuthStore = {
  TOKEN_KEY: 'cfms_auth_token',
  USER_KEY: 'cfms_auth_user',

  getToken() {
    return localStorage.getItem(this.TOKEN_KEY) || sessionStorage.getItem(this.TOKEN_KEY);
  },

  setToken(token, remember = true) {
    if (remember) {
      localStorage.setItem(this.TOKEN_KEY, token);
    } else {
      sessionStorage.setItem(this.TOKEN_KEY, token);
    }
  },

  getUser() {
    const raw = localStorage.getItem(this.USER_KEY) || sessionStorage.getItem(this.USER_KEY);
    try {
      return raw ? JSON.parse(raw) : null;
    } catch (e) {
      return null;
    }
  },

  setUser(user, remember = true) {
    const serialized = JSON.stringify(user);
    if (remember) {
      localStorage.setItem(this.USER_KEY, serialized);
    } else {
      sessionStorage.setItem(this.USER_KEY, serialized);
    }
  },

  clear() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    sessionStorage.removeItem(this.TOKEN_KEY);
    sessionStorage.removeItem(this.USER_KEY);
  },

  isAuthenticated() {
    return !!this.getToken();
  },

  getRole() {
    const user = this.getUser();
    return user ? user.role : null;
  }
};

// ============================================================================
// HTTP CLIENT WITH INTERCEPTORS
// ============================================================================
async function apiRequest(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;
  const headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    ...options.headers
  };

  const token = AuthStore.getToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const config = {
    ...options,
    headers
  };

  try {
    const response = await fetch(url, config);

    // 401 Unauthorized Interceptor
    if (response.status === 401) {
      AuthStore.clear();
      const isLoginPage = window.location.pathname.includes('login.html');
      if (!isLoginPage) {
        if (typeof showToast === 'function') {
          showToast('Session Expired', 'Please login to continue.', 'error');
        }
        setTimeout(() => {
          window.location.href = 'login.html';
        }, 1200);
      }
      throw new Error('Invalid email or password. Please try again.');
    }

    // 403 Forbidden Interceptor
    if (response.status === 403) {
      throw new Error('Access denied. You do not have permission for this action.');
    }

    let data;
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      data = await response.json();
    } else {
      data = { message: await response.text() };
    }

    if (!response.ok) {
      const errorMsg = data && data.message ? data.message : `HTTP Error ${response.status}: ${response.statusText}`;
      throw new Error(errorMsg);
    }

    // Unwrap ApiResponse if wrapped
    if (data && data.hasOwnProperty('success') && data.hasOwnProperty('data')) {
      return data.data;
    }

    return data;
  } catch (error) {
    // Distinguish network/connection errors from server errors
    if (error instanceof TypeError && error.message.toLowerCase().includes('fetch')) {
      const networkErr = new Error('Cannot connect to server. Please ensure the backend is running on port 8081.');
      networkErr.isNetworkError = true;
      console.error(`Network Error on [${options.method || 'GET'} ${endpoint}]:`, networkErr.message);
      throw networkErr;
    }
    console.error(`API Error on [${options.method || 'GET'} ${endpoint}]:`, error.message);
    throw error;
  }
}

// ============================================================================
// AUTHENTICATION API
// ============================================================================
const authAPI = {
  async login(email, password, remember = true) {
    const payload = { email: email.trim().toLowerCase(), password };
    const response = await apiRequest('/auth/login', {
      method: 'POST',
      body: JSON.stringify(payload)
    });

    if (response && response.token) {
      AuthStore.setToken(response.token, remember);
      AuthStore.setUser(response, remember);
    }
    return response;
  },

  async register(data) {
    return apiRequest('/auth/register', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  async getCurrentUser() {
    return apiRequest('/auth/me');
  },

  logout() {
    AuthStore.clear();
    window.location.href = 'login.html';
  }
};

// ============================================================================
// PUBLIC API (Zero-PII Public Grievance Portal Feed)
// ============================================================================
const publicAPI = {
  getPublicComplaints() {
    return apiRequest('/public/complaints');
  }
};

// ============================================================================
// STUDENT API
// ============================================================================
const studentAPI = {
  getDashboard() {
    return apiRequest('/student/dashboard');
  },

  getFeedback() {
    return apiRequest('/student/feedback');
  },

  getFeedbackDetails(id) {
    return apiRequest(`/student/feedback/${id}`);
  },

  submitFeedback(id, payload) {
    return apiRequest(`/student/feedback/${id}/response`, {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  getComplaints() {
    return apiRequest('/student/complaints');
  },

  createComplaint(payload) {
    return apiRequest('/student/complaints', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  getRequests() {
    return apiRequest('/student/requests');
  },

  createRequest(payload) {
    return apiRequest('/student/requests', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  getIssue(id) {
    return apiRequest(`/student/issues/${id}`);
  },

  getNotifications() {
    return apiRequest('/student/notifications');
  },

  markNotificationRead(id) {
    return apiRequest(`/student/notifications/${id}/read`, {
      method: 'PATCH'
    });
  }
};

// ============================================================================
// FACULTY API
// ============================================================================
const facultyAPI = {
  getDashboard() {
    return apiRequest('/faculty/dashboard');
  },

  getFeedback() {
    return apiRequest('/faculty/feedback');
  },

  getFeedbackDetails(id) {
    return apiRequest(`/faculty/feedback/${id}`);
  },

  submitFeedback(id, payload) {
    return apiRequest(`/faculty/feedback/${id}/response`, {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  getRatings() {
    return apiRequest('/faculty/ratings');
  },

  getInsights() {
    return apiRequest('/faculty/insights');
  },

  getPerformance() {
    return apiRequest('/faculty/performance');
  },

  getNotifications() {
    return apiRequest('/faculty/notifications');
  }
};

// ============================================================================
// ADMIN API
// ============================================================================
const adminAPI = {
  getDashboard() {
    return apiRequest('/admin/dashboard');
  },

  getAllFeedbackForms() {
    return apiRequest('/admin/feedback');
  },

  getFeedbackFormDetails(id) {
    return apiRequest(`/admin/feedback/${id}`);
  },

  createFeedbackForm(payload) {
    return apiRequest('/admin/feedback', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  updateFeedbackForm(id, payload) {
    return apiRequest(`/admin/feedback/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    });
  },

  deleteFeedbackForm(id) {
    return apiRequest(`/admin/feedback/${id}`, {
      method: 'DELETE'
    });
  },

  addQuestion(formId, payload) {
    return apiRequest(`/admin/feedback/${formId}/questions`, {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  updateQuestion(questionId, payload) {
    return apiRequest(`/admin/questions/${questionId}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    });
  },

  deleteQuestion(questionId) {
    return apiRequest(`/admin/questions/${questionId}`, {
      method: 'DELETE'
    });
  },

  publishFeedbackForm(id) {
    return apiRequest(`/admin/feedback/${id}/publish`, {
      method: 'POST'
    });
  },

  closeFeedbackForm(id) {
    return apiRequest(`/admin/feedback/${id}/close`, {
      method: 'POST'
    });
  },

  assignFeedback(id, payload) {
    return apiRequest(`/admin/feedback/${id}/assign`, {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  getAllResponses(params = {}) {
    const query = new URLSearchParams(params).toString();
    return apiRequest(`/admin/responses${query ? '?' + query : ''}`);
  },

  getFormResponses(id) {
    return apiRequest(`/admin/feedback/${id}/responses`);
  },

  getAnalyticsOverview() {
    return apiRequest('/admin/analytics/overview');
  },

  getFeedbackAnalytics(id) {
    return apiRequest(`/admin/analytics/feedback/${id}`);
  },

  getAllComplaints(params = {}) {
    const cleanParams = {};
    Object.keys(params).forEach(k => {
      if (params[k]) cleanParams[k] = params[k];
    });
    const query = new URLSearchParams(cleanParams).toString();
    return apiRequest(`/admin/complaints${query ? '?' + query : ''}`);
  },

  getComplaint(id) {
    return apiRequest(`/admin/complaints/${id}`);
  },

  updateComplaint(id, payload) {
    return apiRequest(`/admin/complaints/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    });
  },

  getAllRequests(params = {}) {
    const cleanParams = {};
    Object.keys(params).forEach(k => {
      if (params[k]) cleanParams[k] = params[k];
    });
    const query = new URLSearchParams(cleanParams).toString();
    return apiRequest(`/admin/requests${query ? '?' + query : ''}`);
  },

  getRequest(id) {
    return apiRequest(`/admin/requests/${id}`);
  },

  updateRequest(id, payload) {
    return apiRequest(`/admin/requests/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    });
  },

  // AI & Advanced Analytics
  getFeedbackTrend(period = '6m') {
    return apiRequest(`/admin/analytics/trend?period=${encodeURIComponent(period)}`);
  },

  getSentiment() {
    return apiRequest('/admin/analytics/sentiment');
  },

  getRecurringIssues() {
    return apiRequest('/admin/analytics/recurring-issues');
  },

  getAIInsights() {
    return apiRequest('/admin/analytics/insights');
  },

  regenerateAIInsights() {
    return apiRequest('/admin/analytics/insights/generate', { method: 'POST' });
  },

  getComplaintStatusStats() {
    return apiRequest('/admin/analytics/complaint-status');
  },

  getComplaintPriorityStats() {
    return apiRequest('/admin/analytics/complaint-priority');
  },

  getFacultyComparison(dept = 'all') {
    return apiRequest(`/admin/analytics/faculty-comparison?dept=${encodeURIComponent(dept)}`);
  },

  // Reports
  generateReport(payload) {
    return apiRequest('/admin/reports/generate', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  downloadReportPdf(reportType = 'OVERALL', params = {}) {
    const token = AuthStore.getToken();
    const query = new URLSearchParams({ reportType, ...params }).toString();
    const url = `${API_BASE_URL}/admin/reports/export/pdf?${query}`;
    
    // Fetch blob with authorization header to support secure download
    return fetch(url, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(res => {
      if (!res.ok) throw new Error('Failed to download PDF');
      return res.blob();
    })
    .then(blob => {
      const blobUrl = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = blobUrl;
      a.download = `CFMS-Report-${reportType.toLowerCase()}.pdf`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(blobUrl);
    });
  },

  downloadReportExcel(reportType = 'OVERALL', params = {}) {
    const token = AuthStore.getToken();
    const query = new URLSearchParams({ reportType, ...params }).toString();
    const url = `${API_BASE_URL}/admin/reports/export/excel?${query}`;
    
    return fetch(url, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(res => {
      if (!res.ok) throw new Error('Failed to download Excel file');
      return res.blob();
    })
    .then(blob => {
      const blobUrl = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = blobUrl;
      a.download = `CFMS-Report-${reportType.toLowerCase()}.xlsx`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(blobUrl);
    });
  }
};

// ============================================================================
// NOTIFICATION API (Unified)
// ============================================================================
const notificationAPI = {
  getNotifications() {
    return apiRequest('/notifications');
  },

  getUnreadCount() {
    return apiRequest('/notifications/unread-count');
  },

  markRead(id) {
    return apiRequest(`/notifications/${id}/read`, {
      method: 'PUT'
    });
  },

  markAllRead() {
    return apiRequest('/notifications/mark-all-read', {
      method: 'PUT'
    });
  }
};

// Export globally for browser scripts
window.AuthStore = AuthStore;
window.authAPI = authAPI;
window.publicAPI = publicAPI;
window.studentAPI = studentAPI;
window.facultyAPI = facultyAPI;
window.adminAPI = adminAPI;
window.notificationAPI = notificationAPI;
