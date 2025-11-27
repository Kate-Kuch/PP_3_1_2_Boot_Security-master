// Общие утилиты для всего приложения
class NotificationManager {
    static show(message, type = 'success', duration = 5000) {
        // Создаем уведомление
        let notification = document.getElementById('global-notification');
        if (!notification) {
            notification = document.createElement('div');
            notification.id = 'global-notification';
            notification.style.cssText = `
                position: fixed;
                top: 20px;
                right: 20px;
                padding: 15px 20px;
                border-radius: 8px;
                color: white;
                z-index: 10000;
                display: none;
                max-width: 400px;
                word-wrap: break-word;
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                font-size: 14px;
                line-height: 1.4;
                cursor: pointer;
            `;
            document.body.appendChild(notification);
        }

        // Устанавливаем цвет в зависимости от типа
        const colors = {
            success: '#27ae60',
            error: '#e74c3c',
            warning: '#f39c12',
            info: '#3498db'
        };

        notification.textContent = message;
        notification.style.backgroundColor = colors[type] || colors.info;
        notification.style.display = 'block';
        notification.style.borderLeft = `4px solid ${this.getDarkColor(colors[type] || colors.info)}`;

        // Автоматическое скрытие
        const timeoutId = setTimeout(() => {
            if (notification.parentNode) {
                notification.style.display = 'none';
            }
        }, duration);

        // Закрытие по клику
        const clickHandler = () => {
            notification.style.display = 'none';
            clearTimeout(timeoutId);
            notification.removeEventListener('click', clickHandler);
        };

        notification.addEventListener('click', clickHandler);
    }

    static getDarkColor(color) {
        // Упрощенное затемнение цвета
        return color.replace(')', ', 0.8)').replace('rgb', 'rgba');
    }

    static showApiError(error, defaultMessage = 'Operation failed') {
        let message = defaultMessage;

        if (error.data) {
            // Обработка ошибок валидации с бэкенда
            if (typeof error.data === 'object') {
                message = error.data.error || error.data.message || defaultMessage;
            } else if (typeof error.data === 'string') {
                message = error.data;
            }
        } else if (error.message && error.message !== 'Failed to fetch') {
            message = error.message;
        }

        this.show(message, 'error');
    }
}

class LoadingManager {
    static show(containerId = null) {
        if (containerId) {
            // Локальная загрузка для конкретного контейнера
            const container = document.getElementById(containerId);
            if (container) {
                // Удаляем существующий лоадер
                const existingLoader = container.querySelector('.local-loading');
                if (existingLoader) {
                    existingLoader.remove();
                }

                const loader = document.createElement('div');
                loader.className = 'local-loading';
                loader.innerHTML = `
                    <div style="text-align: center; padding: 20px;">
                        <div class="spinner"></div>
                        <p style="margin-top: 10px; color: #666;">Loading...</p>
                    </div>
                `;
                container.style.position = 'relative';
                container.appendChild(loader);
            }
        } else {
            // Глобальная загрузка
            let loader = document.getElementById('global-loader');
            if (!loader) {
                loader = document.createElement('div');
                loader.id = 'global-loader';
                loader.innerHTML = `
                    <div style="position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); z-index:9999; display:flex; justify-content:center; align-items:center;">
                        <div style="background:white; padding:30px; border-radius:8px; text-align:center; box-shadow: 0 4px 12px rgba(0,0,0,0.15);">
                            <div class="spinner" style="width:40px; height:40px; margin:0 auto 15px;"></div>
                            <p style="margin:0; color:#333;">Loading...</p>
                        </div>
                    </div>
                `;
                document.body.appendChild(loader);
            }
            loader.style.display = 'flex';
        }
    }

    static hide(containerId = null) {
        if (containerId) {
            const container = document.getElementById(containerId);
            if (container) {
                const loader = container.querySelector('.local-loading');
                if (loader) {
                    loader.remove();
                }
            }
        } else {
            const loader = document.getElementById('global-loader');
            if (loader) {
                loader.style.display = 'none';
            }
        }
    }
}

// API методы
class ApiClient {
    static async request(url, options = {}) {
        try {
            const response = await fetch(url, {
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers,
                },
                ...options,
            });

            let data;
            const contentType = response.headers.get('content-type');

            if (contentType && contentType.includes('application/json')) {
                data = await response.json();
            } else if (response.status === 204) {
                // No content
                data = null;
            } else {
                data = await response.text();
            }

            if (!response.ok) {
                // Создаем ошибку с дополнительной информацией
                const error = new Error(data?.error || data?.message || `HTTP error! status: ${response.status}`);
                error.status = response.status;
                error.response = response;
                error.data = data;
                throw error;
            }

            return data;
        } catch (error) {
            console.error('API request failed:', error);

            // Если это не наша кастомная ошибка, создаем ее
            if (!error.data) {
                error.data = { error: error.message };
            }

            throw error;
        }
    }

    static async get(url) {
        return this.request(url);
    }

    static async post(url, data) {
        return this.request(url, {
            method: 'POST',
            body: JSON.stringify(data),
        });
    }

    static async put(url, data) {
        return this.request(url, {
            method: 'PUT',
            body: JSON.stringify(data),
        });
    }

    static async delete(url) {
        return this.request(url, {
            method: 'DELETE',
        });
    }
}

// Вспомогательные функции
class Utils {
    static formatDate(dateString) {
        if (!dateString) return 'N/A';
        try {
            return new Date(dateString).toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'short',
                day: 'numeric'
            });
        } catch (e) {
            return 'Invalid Date';
        }
    }

    static formatDateTime(dateString) {
        if (!dateString) return 'N/A';
        try {
            return new Date(dateString).toLocaleString('en-US', {
                year: 'numeric',
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        } catch (e) {
            return 'Invalid Date';
        }
    }

    static escapeHtml(unsafe) {
        if (!unsafe) return '';
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    static debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    static generateId() {
        return Date.now().toString(36) + Math.random().toString(36).substr(2);
    }

    static validateEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }

    static formatRoleName(roleName) {
        if (!roleName) return '';
        return roleName.replace('ROLE_', '').replace(/_/g, ' ');
    }
}

// Форматирование чисел
class Formatter {
    static formatCurrency(amount, currency = 'USD') {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: currency
        }).format(amount);
    }

    static formatNumber(number) {
        return new Intl.NumberFormat('en-US').format(number);
    }

    static formatPercentage(value) {
        return new Intl.NumberFormat('en-US', {
            style: 'percent',
            minimumFractionDigits: 1,
            maximumFractionDigits: 2
        }).format(value);
    }
}

// Local Storage helpers
class StorageManager {
    static set(key, value) {
        try {
            localStorage.setItem(key, JSON.stringify(value));
            return true;
        } catch (e) {
            console.error('Error saving to localStorage:', e);
            return false;
        }
    }

    static get(key, defaultValue = null) {
        try {
            const item = localStorage.getItem(key);
            return item ? JSON.parse(item) : defaultValue;
        } catch (e) {
            console.error('Error reading from localStorage:', e);
            return defaultValue;
        }
    }

    static remove(key) {
        try {
            localStorage.removeItem(key);
            return true;
        } catch (e) {
            console.error('Error removing from localStorage:', e);
            return false;
        }
    }

    static clear() {
        try {
            localStorage.clear();
            return true;
        } catch (e) {
            console.error('Error clearing localStorage:', e);
            return false;
        }
    }
}

// Добавляем CSS для спиннера и утилит
const commonStyles = `
@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

.spinner {
    border: 4px solid #f3f3f3;
    border-top: 4px solid #3498db;
    border-radius: 50%;
    width: 40px;
    height: 40px;
    animation: spin 1s linear infinite;
    margin: 0 auto;
}

.local-loading {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.9);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 100;
    border-radius: 4px;
}

.error-field {
    border: 2px solid #e74c3c !important;
    background-color: #fff5f5 !important;
}

.field-error {
    color: #e74c3c;
    font-size: 0.875rem;
    margin-top: 0.25rem;
    font-weight: 500;
}

.hidden {
    display: none !important;
}

.text-center {
    text-align: center;
}

.text-left {
    text-align: left;
}

.text-right {
    text-align: right;
}

.mt-1 { margin-top: 0.25rem; }
.mt-2 { margin-top: 0.5rem; }
.mt-3 { margin-top: 1rem; }
.mt-4 { margin-top: 1.5rem; }
.mt-5 { margin-top: 3rem; }

.mb-1 { margin-bottom: 0.25rem; }
.mb-2 { margin-bottom: 0.5rem; }
.mb-3 { margin-bottom: 1rem; }
.mb-4 { margin-bottom: 1.5rem; }
.mb-5 { margin-bottom: 3rem; }

.p-1 { padding: 0.25rem; }
.p-2 { padding: 0.5rem; }
.p-3 { padding: 1rem; }
.p-4 { padding: 1.5rem; }
.p-5 { padding: 3rem; }
`;

// Добавляем стили в документ
if (typeof document !== 'undefined') {
    const styleSheet = document.createElement('style');
    styleSheet.textContent = commonStyles;
    document.head.appendChild(styleSheet);
}

// Экспорты для использования в других модулях
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        NotificationManager,
        LoadingManager,
        ApiClient,
        Utils,
        Formatter,
        StorageManager
    };
}