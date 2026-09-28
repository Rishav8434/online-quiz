/**
 * Online Quiz Application - Client-Side App Utilities
 * Includes Theme Management (Dark/Light), Authentication, Web Audio Effects, and High-Fidelity Toasts
 */

// ==========================================================================
// Theme Management (Light & Dark Mode)
// ==========================================================================
const AppTheme = {
    STORAGE_KEY: 'quiz_theme',

    init() {
        const savedTheme = localStorage.getItem(this.STORAGE_KEY);
        const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
        const currentTheme = savedTheme ? savedTheme : (prefersDark ? 'dark' : 'light');

        this.applyTheme(currentTheme);

        // Listen for OS theme changes if user hasn't explicitly set one
        window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
            if (!localStorage.getItem(this.STORAGE_KEY)) {
                this.applyTheme(e.matches ? 'dark' : 'light');
            }
        });
    },

    applyTheme(theme) {
        document.documentElement.setAttribute('data-bs-theme', theme);
        this.updateIcons(theme);
    },

    toggle() {
        const current = document.documentElement.getAttribute('data-bs-theme') || 'light';
        const next = current === 'dark' ? 'light' : 'dark';
        localStorage.setItem(this.STORAGE_KEY, next);
        this.applyTheme(next);
    },

    updateIcons(theme) {
        const darkIcon = document.querySelector('.theme-icon-dark');
        const lightIcon = document.querySelector('.theme-icon-light');
        if (darkIcon && lightIcon) {
            if (theme === 'dark') {
                darkIcon.classList.add('d-none');
                lightIcon.classList.remove('d-none');
            } else {
                darkIcon.classList.remove('d-none');
                lightIcon.classList.add('d-none');
            }
        }
    }
};

// ==========================================================================
// Authentication Utilities
// ==========================================================================
const AppAuth = {
    setToken(token, user) {
        localStorage.setItem('auth_token', token);
        if (user) {
            localStorage.setItem('auth_user', JSON.stringify(user));
        }
        // Set cookie for server-side Thymeleaf requests
        document.cookie = `auth_token=${token}; path=/; max-age=86400; SameSite=Lax`;
    },

    getToken() {
        return localStorage.getItem('auth_token');
    },

    getUser() {
        const u = localStorage.getItem('auth_user');
        return u ? JSON.parse(u) : null;
    },

    logout() {
        localStorage.removeItem('auth_token');
        localStorage.removeItem('auth_user');
        document.cookie = 'auth_token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;';
        window.location.href = '/login?logged_out=true';
    },

    async fetchAuth(url, options = {}) {
        const token = this.getToken();
        const headers = options.headers || {};

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        headers['Content-Type'] = headers['Content-Type'] || 'application/json';

        try {
            const response = await fetch(url, { ...options, headers });
            if (response && response.status === 401) {
                this.logout();
                return null;
            }
            return response;
        } catch (err) {
            console.error('API request error:', err);
            throw err;
        }
    }
};

// ==========================================================================
// Interactive Web Audio Sound Effects (Zero External Asset Dependencies)
// ==========================================================================
const AppAudio = {
    STORAGE_KEY: 'quiz_sound_enabled',
    ctx: null,

    isEnabled() {
        const val = localStorage.getItem(this.STORAGE_KEY);
        return val === null ? true : val === 'true';
    },

    toggleSound() {
        const next = !this.isEnabled();
        localStorage.setItem(this.STORAGE_KEY, String(next));
        this.updateSoundIcons();
        return next;
    },

    updateSoundIcons() {
        const enabled = this.isEnabled();
        document.querySelectorAll('.sound-toggle-icon').forEach(el => {
            if (enabled) {
                el.classList.remove('bi-volume-mute-fill');
                el.classList.add('bi-volume-up-fill');
            } else {
                el.classList.remove('bi-volume-up-fill');
                el.classList.add('bi-volume-mute-fill');
            }
        });
    },

    getContext() {
        if (!this.ctx && (window.AudioContext || window.webkitAudioContext)) {
            const AudioCtx = window.AudioContext || window.webkitAudioContext;
            this.ctx = new AudioCtx();
        }
        if (this.ctx && this.ctx.state === 'suspended') {
            this.ctx.resume();
        }
        return this.ctx;
    },

    playTone(frequency, duration = 0.08, type = 'sine', volume = 0.12) {
        if (!this.isEnabled()) return;
        try {
            const ctx = this.getContext();
            if (!ctx) return;
            const osc = ctx.createOscillator();
            const gain = ctx.createGain();

            osc.type = type;
            osc.frequency.setValueAtTime(frequency, ctx.currentTime);

            gain.gain.setValueAtTime(volume, ctx.currentTime);
            gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + duration);

            osc.connect(gain);
            gain.connect(ctx.destination);

            osc.start();
            osc.stop(ctx.currentTime + duration);
        } catch (e) {
            // Audio context not allowed or unsupported
        }
    },

    playOptionSelect() {
        this.playTone(520, 0.06, 'sine', 0.1);
    },

    playFlagToggle() {
        this.playTone(660, 0.09, 'triangle', 0.15);
    },

    playSuccessChord() {
        if (!this.isEnabled()) return;
        try {
            setTimeout(() => this.playTone(523.25, 0.25, 'sine', 0.12), 0);
            setTimeout(() => this.playTone(659.25, 0.25, 'sine', 0.12), 80);
            setTimeout(() => this.playTone(783.99, 0.35, 'sine', 0.14), 160);
        } catch (e) {}
    }
};

// ==========================================================================
// Notification Toasts
// ==========================================================================
function showToast(message, type = 'info') {
    let toastContainer = document.getElementById('toastContainer');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'toastContainer';
        toastContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        toastContainer.style.zIndex = '9999';
        document.body.appendChild(toastContainer);
    }

    const toastEl = document.createElement('div');
    const bgClass = type === 'error' ? 'bg-danger' : (type === 'success' ? 'bg-success' : (type === 'warning' ? 'bg-warning text-dark' : 'bg-primary'));
    const iconClass = type === 'error' ? 'bi-exclamation-octagon-fill' : (type === 'success' ? 'bi-check-circle-fill' : (type === 'warning' ? 'bi-exclamation-triangle-fill' : 'bi-info-circle-fill'));

    toastEl.className = `toast align-items-center text-white ${bgClass} border-0 shadow-lg`;
    toastEl.setAttribute('role', 'alert');
    toastEl.setAttribute('aria-live', 'assertive');
    toastEl.setAttribute('aria-atomic', 'true');

    toastEl.innerHTML = `
        <div class="d-flex align-items-center">
            <div class="ps-3 fs-5"><i class="bi ${iconClass}"></i></div>
            <div class="toast-body flex-grow-1 ps-2 pe-3">${message}</div>
            <button type="button" class="btn-close ${type === 'warning' ? '' : 'btn-close-white'} me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
    `;

    toastContainer.appendChild(toastEl);
    const toast = new bootstrap.Toast(toastEl, { delay: 4000 });
    toast.show();

    toastEl.addEventListener('hidden.bs.toast', () => {
        toastEl.remove();
    });
}

// Auto-initialize on DOM ready
document.addEventListener('DOMContentLoaded', () => {
    AppTheme.init();
    AppAudio.updateSoundIcons();
});
