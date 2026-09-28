/**
 * Online Quiz Application - Enhanced Exam Engine & Countdown Timer
 * Features: 3-State Navigator, Flag for Review, Palette Filter, Keyboard Shortcuts,
 * Audio Feedback, Fullscreen Focus Mode, and Font Zoom Adjuster.
 */

class QuizAttemptManager {
    constructor(config) {
        this.attemptId = config.attemptId;
        this.quizId = config.quizId;
        this.remainingSeconds = config.remainingSeconds;
        this.totalSeconds = config.totalSeconds || config.remainingSeconds;
        this.timerDisplay = document.getElementById(config.timerDisplayId || 'timerDisplay');
        this.progressBar = document.getElementById(config.progressBarId || 'timerProgressBar');
        this.form = document.getElementById(config.formId || 'quizAttemptForm');
        this.submitButton = document.getElementById(config.submitButtonId || 'submitQuizBtn');

        this.flaggedQuestions = new Set();
        this.answeredQuestions = new Set();
        this.activeQuestionId = null;
        this.fontSizeStep = 1; // 0: Normal, 1: Medium, 2: Large
        this.isSubmitted = false;
        this.timerInterval = null;

        this.init();
    }

    init() {
        this.checkInitialAnswers();
        this.updateDisplay();
        this.startTimer();
        this.attachEvents();
        this.updateNavigatorBadges();
        this.trackActiveQuestion();
    }

    checkInitialAnswers() {
        document.querySelectorAll('.question-block').forEach(qEl => {
            const qId = qEl.getAttribute('data-question-id');
            const checked = qEl.querySelector('input[type="radio"]:checked');
            if (checked) {
                this.answeredQuestions.add(qId);
            }
        });
    }

    startTimer() {
        this.timerInterval = setInterval(() => {
            this.remainingSeconds--;
            this.updateDisplay();

            // Sound beep at key milestones
            if (this.remainingSeconds === 60 || this.remainingSeconds === 30 || this.remainingSeconds === 10) {
                if (typeof AppAudio !== 'undefined') {
                    AppAudio.playTone(880, 0.15, 'sawtooth', 0.2);
                }
            }

            if (this.remainingSeconds <= 0) {
                clearInterval(this.timerInterval);
                this.handleTimeout();
            }
        }, 1000);
    }

    updateDisplay() {
        if (!this.timerDisplay) return;

        const minutes = Math.floor(Math.max(0, this.remainingSeconds) / 60);
        const seconds = Math.max(0, this.remainingSeconds) % 60;
        const formatted = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
        this.timerDisplay.textContent = formatted;

        // Visual warning when <= 60 seconds remaining
        if (this.remainingSeconds <= 60) {
            this.timerDisplay.classList.add('timer-warning');
        } else {
            this.timerDisplay.classList.remove('timer-warning');
        }

        // Update progress bar
        if (this.progressBar && this.totalSeconds > 0) {
            const percentage = Math.max(0, (this.remainingSeconds / this.totalSeconds) * 100);
            this.progressBar.style.width = `${percentage}%`;

            if (percentage < 20) {
                this.progressBar.className = 'progress-bar bg-danger';
            } else if (percentage < 50) {
                this.progressBar.className = 'progress-bar bg-warning';
            } else {
                this.progressBar.className = 'progress-bar bg-primary';
            }
        }
    }

    // Flag for Review toggle
    toggleFlag(qId) {
        const idStr = String(qId);
        const flagBtn = document.getElementById(`flag-btn-${idStr}`);

        if (this.flaggedQuestions.has(idStr)) {
            this.flaggedQuestions.delete(idStr);
            if (flagBtn) {
                flagBtn.classList.remove('is-flagged');
                flagBtn.innerHTML = '<i class="bi bi-flag"></i> Flag for Review';
            }
            showToast(`Question flagged state removed`, 'info');
        } else {
            this.flaggedQuestions.add(idStr);
            if (flagBtn) {
                flagBtn.classList.add('is-flagged');
                flagBtn.innerHTML = '<i class="bi bi-flag-fill text-warning"></i> Flagged for Review';
            }
            if (typeof AppAudio !== 'undefined') AppAudio.playFlagToggle();
            showToast(`Question marked for review`, 'warning');
        }

        this.updateNavigatorBadges();
    }

    updateNavigatorBadges() {
        document.querySelectorAll('.question-block').forEach(qEl => {
            const qId = qEl.getAttribute('data-question-id');
            const navBtn = document.getElementById(`nav-btn-${qId}`);
            if (!navBtn) return;

            navBtn.classList.remove('nav-answered', 'nav-flagged');

            if (this.flaggedQuestions.has(qId)) {
                navBtn.classList.add('nav-flagged');
            } else if (this.answeredQuestions.has(qId)) {
                navBtn.classList.add('nav-answered');
            }
        });

        // Update counts in legend
        const answeredEl = document.getElementById('statAnsweredCount');
        const flaggedEl = document.getElementById('statFlaggedCount');
        const unansweredEl = document.getElementById('statUnansweredCount');
        const total = document.querySelectorAll('.question-block').length;

        const answeredCount = this.answeredQuestions.size;
        const flaggedCount = this.flaggedQuestions.size;
        const unansweredCount = Math.max(0, total - answeredCount);

        if (answeredEl) answeredEl.textContent = answeredCount;
        if (flaggedEl) flaggedEl.textContent = flaggedCount;
        if (unansweredEl) unansweredEl.textContent = unansweredCount;
    }

    filterPalette(type) {
        document.querySelectorAll('.filter-tab-btn').forEach(btn => {
            btn.classList.toggle('active', btn.getAttribute('data-filter') === type);
        });

        document.querySelectorAll('.question-block').forEach(qEl => {
            const qId = qEl.getAttribute('data-question-id');
            const navBtn = document.getElementById(`nav-btn-${qId}`);
            if (!navBtn) return;

            let visible = true;
            if (type === 'answered') {
                visible = this.answeredQuestions.has(qId);
            } else if (type === 'flagged') {
                visible = this.flaggedQuestions.has(qId);
            } else if (type === 'unanswered') {
                visible = !this.answeredQuestions.has(qId);
            }

            navBtn.style.opacity = visible ? '1' : '0.25';
            navBtn.style.pointerEvents = visible ? 'auto' : 'none';
        });
    }

    adjustFontSize(delta) {
        const sizes = ['1rem', '1.18rem', '1.35rem'];
        this.fontSizeStep = Math.max(0, Math.min(2, this.fontSizeStep + delta));
        const newSize = sizes[this.fontSizeStep];

        document.querySelectorAll('.question-statement').forEach(el => {
            el.style.fontSize = newSize;
        });

        showToast(`Text size: ${this.fontSizeStep === 0 ? 'Default' : (this.fontSizeStep === 1 ? 'Large' : 'Extra Large')}`, 'info');
    }

    toggleFullscreen() {
        if (!document.fullscreenElement) {
            document.documentElement.requestFullscreen().catch(err => {
                showToast('Fullscreen mode not available', 'error');
            });
            const fsBtn = document.getElementById('btnFullscreen');
            if (fsBtn) fsBtn.innerHTML = '<i class="bi bi-fullscreen-exit"></i> Exit Focus';
        } else {
            if (document.exitFullscreen) {
                document.exitFullscreen();
            }
            const fsBtn = document.getElementById('btnFullscreen');
            if (fsBtn) fsBtn.innerHTML = '<i class="bi bi-arrows-fullscreen"></i> Focus Mode';
        }
    }

    trackActiveQuestion() {
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    this.activeQuestionId = entry.target.getAttribute('data-question-id');
                }
            });
        }, { threshold: 0.5 });

        document.querySelectorAll('.question-block').forEach(el => observer.observe(el));
    }

    attachEvents() {
        // Prevent accidental tab closing or navigation
        window.addEventListener('beforeunload', (e) => {
            if (!this.isSubmitted) {
                e.preventDefault();
                e.returnValue = 'You have an active quiz session in progress!';
            }
        });

        // Form submission listener
        if (this.form) {
            this.form.addEventListener('submit', (e) => {
                e.preventDefault();
                this.submitQuiz(false);
            });
        }

        // Option select listener
        document.querySelectorAll('.quiz-option-input').forEach(radio => {
            radio.addEventListener('change', (e) => {
                const qId = e.target.getAttribute('data-question-id');
                this.answeredQuestions.add(qId);
                this.updateNavigatorBadges();
                if (typeof AppAudio !== 'undefined') AppAudio.playOptionSelect();
            });
        });

        // Keyboard Shortcuts
        window.addEventListener('keydown', (e) => {
            // Ignore if active element is an input or textarea
            if (['INPUT', 'TEXTAREA'].includes(document.activeElement.tagName)) return;

            // Flag toggle key: 'F' or 'f'
            if (e.key === 'f' || e.key === 'F') {
                if (this.activeQuestionId) {
                    this.toggleFlag(this.activeQuestionId);
                }
                return;
            }

            // Options selection keys: 1, 2, 3, 4 or a, b, c, d
            const keyMap = { '1': 0, '2': 1, '3': 2, '4': 3, 'a': 0, 'b': 1, 'c': 2, 'd': 3, 'A': 0, 'B': 1, 'C': 2, 'D': 3 };
            if (this.activeQuestionId && keyMap[e.key] !== undefined) {
                const qEl = document.getElementById(`question-card-${this.activeQuestionId}`);
                if (qEl) {
                    const radios = qEl.querySelectorAll('.quiz-option-input');
                    const targetRadio = radios[keyMap[e.key]];
                    if (targetRadio) {
                        targetRadio.checked = true;
                        targetRadio.dispatchEvent(new Event('change'));
                    }
                }
            }
        });

        // Modal prepare audit
        const confirmModal = document.getElementById('confirmSubmitModal');
        if (confirmModal) {
            confirmModal.addEventListener('show.bs.modal', () => {
                const total = document.querySelectorAll('.question-block').length;
                const answered = this.answeredQuestions.size;
                const unanswered = total - answered;
                const flagged = this.flaggedQuestions.size;

                const auditEl = document.getElementById('submitAuditText');
                if (auditEl) {
                    if (unanswered > 0) {
                        auditEl.innerHTML = `
                            <div class="alert alert-warning py-2 mb-3">
                                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                                <strong>${unanswered} question${unanswered > 1 ? 's are' : ' is'} unanswered!</strong>
                            </div>
                            <p class="mb-0">You have completed <strong>${answered}</strong> of <strong>${total}</strong> questions. (${flagged} marked for review).</p>
                        `;
                    } else {
                        auditEl.innerHTML = `
                            <div class="alert alert-success py-2 mb-3">
                                <i class="bi bi-check-circle-fill me-2"></i>
                                <strong>All ${total} questions answered!</strong>
                            </div>
                            <p class="mb-0">You are ready to submit your assessment. Once confirmed, answers are evaluated immediately.</p>
                        `;
                    }
                }
            });
        }
    }

    handleTimeout() {
        if (this.isSubmitted) return;
        showToast('Time has expired! Submitting your answers automatically...', 'warning');
        this.submitQuiz(true);
    }

    async submitQuiz(isAutoSubmit = false) {
        if (this.isSubmitted) return;
        this.isSubmitted = true;
        clearInterval(this.timerInterval);

        if (this.submitButton) {
            this.submitButton.disabled = true;
            this.submitButton.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>${isAutoSubmit ? 'Auto-Submitting...' : 'Submitting Answers...'}`;
        }

        if (typeof AppAudio !== 'undefined') AppAudio.playSuccessChord();

        // Collect answers
        const answers = [];
        const questionElements = document.querySelectorAll('.question-block');

        questionElements.forEach(qEl => {
            const questionId = parseInt(qEl.getAttribute('data-question-id'), 10);
            const selectedRadio = qEl.querySelector('input[type="radio"]:checked');
            const selectedOptionId = selectedRadio ? parseInt(selectedRadio.value, 10) : null;

            answers.push({
                questionId: questionId,
                selectedOptionId: selectedOptionId
            });
        });

        const payload = { answers: answers };

        try {
            const response = await AppAuth.fetchAuth(`/api/quizzes/attempts/${this.attemptId}/submit`, {
                method: 'POST',
                body: JSON.stringify(payload)
            });

            if (response && response.ok) {
                const result = await response.json();
                window.location.href = `/quizzes/${this.quizId}/result?attemptId=${result.attemptId}`;
            } else {
                const errorData = response ? await response.json() : null;
                const msg = errorData && errorData.message ? errorData.message : 'Submission failed';
                showToast(msg, 'error');
                setTimeout(() => {
                    window.location.href = `/quizzes/${this.quizId}/result?attemptId=${this.attemptId}`;
                }, 1500);
            }
        } catch (err) {
            console.error('Error submitting quiz:', err);
            window.location.href = `/quizzes/${this.quizId}/result?attemptId=${this.attemptId}`;
        }
    }
}
