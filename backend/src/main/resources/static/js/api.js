// VIT-AP Course Registration Portal - Centralized REST API Service

const API_BASE_URL = window.location.origin.includes(':8080')
  ? `${window.location.origin}/api`
  : 'http://localhost:8080/api';

// Helper for making HTTP requests
async function apiRequest(endpoint, method = 'GET', body = null) {
  const options = {
    method,
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
  };

  if (body) {
    options.body = JSON.stringify(body);
  }

  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
    const data = await response.json();
    return data;
  } catch (error) {
    console.warn(`API call failed for ${endpoint}:`, error);
    return { success: false, message: error.message || 'Server connection failed' };
  }
}

// User Session Management
function getCurrentUser() {
  const userJson = localStorage.getItem('vitap_user') || sessionStorage.getItem('vitap_user');
  if (userJson) {
    try {
      return JSON.parse(userJson);
    } catch (e) {
      return null;
    }
  }
  return {
    username: '23MIC7129',
    fullName: 'Nitheesh K. Reddy',
    role: 'STUDENT',
    program: 'B.Tech CSE (Sem 6)',
    semester: 'Semester 6',
    cgpa: 8.95,
    earnedCredits: 120,
    registeredCredits: 15,
    maxCredits: 27
  };
}

function setCurrentUser(user, persist = true) {
  const json = JSON.stringify(user);
  if (persist) {
    localStorage.setItem('vitap_user', json);
  } else {
    sessionStorage.setItem('vitap_user', json);
  }
}

function logoutUser() {
  localStorage.removeItem('vitap_user');
  sessionStorage.removeItem('vitap_user');
  window.location.href = window.location.pathname.includes('/admin/') ? '../student/login.html' : 'login.html';
}

// Auth API
const authApi = {
  login: async (credentials) => {
    return await apiRequest('/auth/login', 'POST', credentials);
  }
};

// Student API
const studentApi = {
  getProfile: async (username) => {
    return await apiRequest(`/students/${username}`);
  },
  updateSettings: async (username, data) => {
    return await apiRequest(`/students/${username}/settings`, 'PUT', data);
  }
};

// Courses API
const courseApi = {
  getAll: async (basket = '', username = '') => {
    const query = new URLSearchParams();
    if (basket) query.append('basket', basket);
    if (username) query.append('username', username);
    return await apiRequest(`/courses?${query.toString()}`);
  },
  getByCode: async (code, username = '') => {
    const query = username ? `?username=${username}` : '';
    return await apiRequest(`/courses/${code}${query}`);
  }
};

// Registrations API
const registrationApi = {
  getForStudent: async (username) => {
    return await apiRequest(`/registrations/student/${username}`);
  },
  register: async (data) => {
    return await apiRequest('/registrations', 'POST', data);
  },
  modifySlot: async (data) => {
    return await apiRequest('/registrations/modify', 'POST', data);
  },
  deleteRegistration: async (id) => {
    return await apiRequest(`/registrations/${id}`, 'DELETE');
  },
  dropCourse: async (username, courseCode) => {
    return await apiRequest(`/registrations/student/${username}/course/${courseCode}`, 'DELETE');
  },
  batchDrop: async (username, courseCodes) => {
    return await apiRequest('/registrations/batch-delete', 'POST', { username, courseCodes });
  }
};

// Masterlist API
const masterlistApi = {
  get: async (username) => {
    return await apiRequest(`/masterlist/student/${username}`);
  },
  save: async (username, items) => {
    return await apiRequest(`/masterlist/student/${username}/save`, 'POST', items);
  },
  execute: async (username) => {
    return await apiRequest(`/masterlist/student/${username}/execute`, 'POST');
  },
  clear: async (username) => {
    return await apiRequest(`/masterlist/student/${username}`, 'DELETE');
  }
};

// Notifications API
const notificationApi = {
  getAll: async (category = '') => {
    const query = category ? `?category=${category}` : '';
    return await apiRequest(`/notifications${query}`);
  },
  toggleRead: async (id) => {
    return await apiRequest(`/notifications/${id}/read`, 'PUT');
  },
  markAllRead: async () => {
    return await apiRequest('/notifications/read-all', 'PUT');
  },
  delete: async (id) => {
    return await apiRequest(`/notifications/${id}`, 'DELETE');
  },
  clearAll: async () => {
    return await apiRequest('/notifications/clear-all', 'DELETE');
  },
  simulate: async () => {
    return await apiRequest('/notifications/simulate', 'POST');
  }
};

// Admin API
const adminApi = {
  getStats: async () => {
    return await apiRequest('/admin/stats');
  },
  getAllStudents: async () => {
    return await apiRequest('/admin/students');
  },
  createStudent: async (data) => {
    return await apiRequest('/admin/students', 'POST', data);
  },
  updateStudent: async (id, data) => {
    return await apiRequest(`/admin/students/${id}`, 'PUT', data);
  },
  deleteStudent: async (id) => {
    return await apiRequest(`/admin/students/${id}`, 'DELETE');
  },
  createCourse: async (data) => {
    return await apiRequest('/admin/courses', 'POST', data);
  },
  deleteCourse: async (id) => {
    return await apiRequest(`/admin/courses/${id}`, 'DELETE');
  },
  deleteCourseByCode: async (code) => {
    return await apiRequest(`/admin/courses/code/${code}`, 'DELETE');
  },
  getAllSlots: async () => {
    return await apiRequest('/admin/slots');
  },
  assignSlot: async (data) => {
    return await apiRequest('/admin/slots', 'POST', data);
  },
  deleteSlot: async (id) => {
    return await apiRequest(`/admin/slots/${id}`, 'DELETE');
  },
  getConfig: async () => {
    return await apiRequest('/admin/config');
  },
  updateConfig: async (data) => {
    return await apiRequest('/admin/config', 'PUT', data);
  },
  extendWindow: async (minutes = 60) => {
    return await apiRequest('/admin/controls/extend', 'POST', { minutes });
  },
  togglePause: async () => {
    return await apiRequest('/admin/controls/toggle-pause', 'POST');
  }
};

// Public Configuration API (For Student Portal & Countdown)
const configApi = {
  get: async () => {
    return await apiRequest('/config');
  }
};

