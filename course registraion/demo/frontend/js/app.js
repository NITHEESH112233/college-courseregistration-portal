// VIT-AP Course Registration Portal - Shared Interactions & Utilities

document.addEventListener('DOMContentLoaded', () => {
  initTheme();
  initCountdownTimer();
  initRegisterButtons();
  initPasswordVisibilityToggle();
  initLiveCourseSearch();
});

// Theme Persistence Across All Pages
function initTheme() {
  const savedTheme = localStorage.getItem('vitap_theme');
  if (savedTheme === 'dark') {
    document.body.classList.add('dark-theme');
  } else {
    document.body.classList.remove('dark-theme');
  }
}

function setTheme(theme) {
  localStorage.setItem('vitap_theme', theme);
  if (theme === 'dark') {
    document.body.classList.add('dark-theme');
  } else if (theme === 'light') {
    document.body.classList.remove('dark-theme');
  } else {
    // System preference
    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
      document.body.classList.add('dark-theme');
    } else {
      document.body.classList.remove('dark-theme');
    }
  }
  showNotification(`Theme updated to ${theme} mode`, 'info');
}

// Live Course Search Filter for Course Listing Page
function initLiveCourseSearch() {
  const searchInput = document.getElementById('course-search-input');
  if (!searchInput) return;

  searchInput.addEventListener('input', (e) => {
    const query = e.target.value.toLowerCase().trim();
    const courseCards = document.querySelectorAll('.course-card');

    courseCards.forEach(card => {
      const title = card.querySelector('.course-title')?.textContent.toLowerCase() || '';
      const code = card.querySelector('.course-code-tag')?.textContent.toLowerCase() || '';
      const desc = card.querySelector('.course-description')?.textContent.toLowerCase() || '';

      if (title.includes(query) || code.includes(query) || desc.includes(query)) {
        card.style.display = 'flex';
      } else {
        card.style.display = 'none';
      }
    });
  });
}

// Countdown Timer for Registration Slot Banner
function initCountdownTimer() {
  const hoursElem = document.getElementById('timer-hours');
  const minsElem = document.getElementById('timer-mins');
  const secsElem = document.getElementById('timer-secs');

  if (!hoursElem || !minsElem) return;

  let totalSeconds = 2 * 3600 + 45 * 60 + 30; // 02 HRS 45 MIN 30 SEC

  const timerInterval = setInterval(() => {
    if (totalSeconds <= 0) {
      clearInterval(timerInterval);
      hoursElem.textContent = '00';
      minsElem.textContent = '00';
      if (secsElem) secsElem.textContent = '00';
      return;
    }

    totalSeconds--;
    const h = Math.floor(totalSeconds / 3600);
    const m = Math.floor((totalSeconds % 3600) / 60);
    const s = totalSeconds % 60;

    hoursElem.textContent = String(h).padStart(2, '0');
    minsElem.textContent = String(m).padStart(2, '0');
    if (secsElem) secsElem.textContent = String(s).padStart(2, '0');
  }, 1000);
}

// Course Registration Button Interactions
function initRegisterButtons() {
  const regBtns = document.querySelectorAll('.action-register-btn');
  regBtns.forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      if (btn.classList.contains('registered')) {
        btn.classList.remove('registered');
        btn.textContent = 'Register';
        btn.style.backgroundColor = 'var(--vit-maroon)';
        showNotification('Course registration cancelled.', 'info');
      } else {
        btn.classList.add('registered');
        btn.textContent = 'Registered ✓';
        btn.style.backgroundColor = '#166534'; // Green success
        showNotification('Successfully registered for course!', 'success');
      }
    });
  });
}

// Password Visibility Toggle for Login Page
function initPasswordVisibilityToggle() {
  const toggleBtn = document.getElementById('toggle-password-btn');
  const passInput = document.getElementById('password-input');

  if (toggleBtn && passInput) {
    toggleBtn.addEventListener('click', () => {
      const type = passInput.getAttribute('type') === 'password' ? 'text' : 'password';
      passInput.setAttribute('type', type);
      toggleBtn.classList.toggle('fa-eye');
      toggleBtn.classList.toggle('fa-eye-slash');
    });
  }
}

// Notification Toast Utility
function showNotification(message, type = 'success') {
  let toast = document.getElementById('portal-toast');
  if (!toast) {
    toast = document.createElement('div');
    toast.id = 'portal-toast';
    toast.style.cssText = `
      position: fixed;
      bottom: 24px;
      right: 24px;
      background-color: ${type === 'success' ? '#15803D' : type === 'danger' ? '#B91C1C' : '#0A2540'};
      color: #ffffff;
      padding: 14px 22px;
      border-radius: 10px;
      font-size: 0.88rem;
      font-weight: 600;
      box-shadow: 0 10px 30px rgba(0,0,0,0.3);
      z-index: 9999;
      transition: all 0.3s ease;
      opacity: 0;
      transform: translateY(10px);
      display: flex;
      align-items: center;
      gap: 10px;
    `;
    document.body.appendChild(toast);
  }

  toast.innerHTML = `<i class="fa-solid fa-${type === 'success' ? 'circle-check' : type === 'danger' ? 'circle-exclamation' : 'circle-info'}"></i> ${message}`;
  toast.style.opacity = '1';
  toast.style.transform = 'translateY(0)';

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
  }, 3200);
}

// Print Timetable Helper
function printTimetable() {
  window.print();
}
