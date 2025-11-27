class UserApp {
    constructor() {
        this.currentUser = null;
        this.init();
    }

    async init() {
        await this.loadUserData();
        this.setupEventListeners();
    }

    setupEventListeners() {
        // Можно добавить обработчики событий если нужно
        console.log('User app initialized');
    }

    async loadUserData() {
        this.showLoading();

        try {
            // Загружаем данные пользователя через REST API
            this.currentUser = await ApiClient.get('/api/user');
            this.renderUserInfo();
            this.hideLoading();
        } catch (error) {
            this.hideLoading();
            // Ошибка уже обработана в ApiClient
            console.error('Failed to load user data:', error);
        }
    }

    renderUserInfo() {
        if (!this.currentUser) return;

        // Обновляем верхнюю панель
        const emailElement = document.getElementById('currentUserEmail');
        const rolesElement = document.getElementById('currentUserRoles');

        if (emailElement) {
            emailElement.textContent = this.currentUser.email;
        }

        if (rolesElement) {
            rolesElement.textContent = this.currentUser.roles
                .map(role => role.name.replace('ROLE_', ''))
                .join(' ');
        }

        // Обновляем таблицу с информацией о пользователе
        const tbody = document.getElementById('userTableBody');
        if (tbody) {
            tbody.innerHTML = this.createUserTableRow();
        }

        // Показываем таблицу после загрузки данных
        const userInfoTable = document.getElementById('userInfoTable');
        if (userInfoTable) {
            userInfoTable.style.display = 'block';
        }
    }

    createUserTableRow() {
        return `
            <tr>
                <td>${this.currentUser.id}</td>
                <td>${this.currentUser.firstName}</td>
                <td>${this.currentUser.lastName}</td>
                <td>${this.currentUser.age}</td>
                <td>${this.currentUser.email}</td>
                <td>
                    ${this.currentUser.roles.map(role =>
            `<span class="role-badge ${role.name.includes('ADMIN') ? 'role-admin' : 'role-user'}">
                            ${role.name.replace('ROLE_', '')}
                        </span>`
        ).join('')}
                </td>
            </tr>
        `;
    }

    showLoading() {
        const loadingIndicator = document.getElementById('loadingIndicator');
        const userInfoTable = document.getElementById('userInfoTable');

        if (loadingIndicator) {
            loadingIndicator.style.display = 'block';
        }
        if (userInfoTable) {
            userInfoTable.style.display = 'none';
        }
    }

    hideLoading() {
        const loadingIndicator = document.getElementById('loadingIndicator');
        if (loadingIndicator) {
            loadingIndicator.style.display = 'none';
        }
    }

    // Метод для обновления данных (если понадобится)
    async refreshUserData() {
        await this.loadUserData();
        NotificationManager.show('User data updated', 'success');
    }
}

// Инициализация приложения когда DOM загружен
document.addEventListener('DOMContentLoaded', () => {
    window.userApp = new UserApp();
});

// Дополнительные функции для работы с модальными окнами (если понадобятся)
function handleUserAction(action) {
    switch(action) {
        case 'refresh':
            window.userApp.refreshUserData();
            break;
        default:
            console.log('Unknown action:', action);
    }
}