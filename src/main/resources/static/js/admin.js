class AdminApp {
    constructor() {
        this.users = [];
        this.allRoles = [];
        this.currentUser = null;
        this.currentEditingUserId = null;
        this.currentDeletingUserId = null;
        this.isCreating = false;
        this.isUpdating = false;

        this.init();
    }

    async init() {
        await this.loadInitialData();
        this.setupEventListeners();
        console.log('AdminApp initialized successfully');
    }

    setupEventListeners() {
        // Mode switching
        document.getElementById('adminModeBtn')?.addEventListener('click', () => this.switchMode('admin'));
        document.getElementById('userModeBtn')?.addEventListener('click', () => this.switchMode('user'));

        // Tab switching
        document.getElementById('usersTableTab')?.addEventListener('click', () => this.switchTab('users'));
        document.getElementById('newUserTab')?.addEventListener('click', () => this.switchTab('newUser'));
        document.getElementById('cancelCreate')?.addEventListener('click', () => this.switchTab('users'));

        // Forms
        const createForm = document.getElementById('createUserForm');
        if (createForm) {
            createForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.createUser();
            });
        }

        const editForm = document.getElementById('editUserForm');
        if (editForm) {
            editForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.updateUser();
            });
        }

        // Modal buttons
        document.getElementById('confirmDelete')?.addEventListener('click', () => this.confirmDelete());
        document.getElementById('cancelDelete')?.addEventListener('click', () => this.closeDeleteModal());
        document.getElementById('cancelEdit')?.addEventListener('click', () => this.closeEditModal());

        // Modal close events
        document.getElementById('editUserModal')?.addEventListener('click', (e) => {
            if (e.target === e.currentTarget) this.closeEditModal();
        });
        document.getElementById('deleteUserModal')?.addEventListener('click', (e) => {
            if (e.target === e.currentTarget) this.closeDeleteModal();
        });

        // Escape key to close modals
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.closeEditModal();
                this.closeDeleteModal();
            }
        });
    }

    async loadInitialData() {
        LoadingManager.show();
        try {
            const initData = await ApiClient.get('/api/admin/data');
            this.users = initData.users || [];
            this.allRoles = initData.allRoles || [];
            this.currentUser = initData.currentUser;

            this.renderRolesSelects();
            this.renderUsersTable();
            this.renderCurrentUserInfo();

            // УБРАТЬ всплывающее уведомление при загрузке данных
            // NotificationManager.show('Data loaded successfully', 'success');

        } catch (error) {
            console.error('Failed to load initial data:', error);
            NotificationManager.showApiError(error, 'Failed to load data');
        } finally {
            LoadingManager.hide();
        }
    }

    async reloadUsers() {
        try {
            console.log('Reloading users...');
            const users = await ApiClient.get('/api/admin/users');
            this.users = users || [];
            this.renderUsersTable();
            console.log('Users reloaded successfully', this.users);
        } catch (error) {
            console.error('Failed to reload users:', error);
            NotificationManager.showApiError(error, 'Failed to reload users');
        }
    }

    renderRolesSelects() {
        this.renderRolesSelect('createRoles');
        this.renderRolesSelect('editRoles');
        this.renderRolesSelect('roles');
    }

    renderRolesSelect(selectId, userRoles = []) {
        const select = document.getElementById(selectId);
        if (!select) {
            console.warn(`Select element with id '${selectId}' not found`);
            return;
        }

        select.innerHTML = '';

        if (!this.allRoles || this.allRoles.length === 0) {
            console.warn('No roles available to render');
            const option = document.createElement('option');
            option.value = '';
            option.textContent = 'No roles available';
            select.appendChild(option);
            return;
        }

        this.allRoles.forEach(role => {
            const option = document.createElement('option');
            option.value = role.name;
            option.textContent = Utils.formatRoleName(role.name);
            option.selected = userRoles.some(userRole => userRole.name === role.name);
            select.appendChild(option);
        });
    }

    renderUsersTable() {
        const tbody = document.getElementById('usersTableBody');
        if (!tbody) {
            console.error('Users table body not found');
            return;
        }

        console.log('Rendering users table with', this.users.length, 'users');
        tbody.innerHTML = '';

        if (!this.users || this.users.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="8" style="text-align: center; padding: 20px; color: #666;">
                        No users found
                    </td>
                </tr>
            `;
            return;
        }

        this.users.forEach(user => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${user.id}</td>
                <td>${Utils.escapeHtml(user.firstName)}</td>
                <td>${Utils.escapeHtml(user.lastName)}</td>
                <td>${user.age}</td>
                <td>${Utils.escapeHtml(user.email)}</td>
                <td>
                    ${user.roles.map(role =>
                `<span class="role-badge">${Utils.formatRoleName(role.name)}</span>`
            ).join(' ')}
                </td>
                <td>
                    <button class="edit-btn" onclick="adminApp.openEditModal(${user.id})">
                        <i class="fas fa-edit"></i> Edit
                    </button>
                </td>
                <td>
                    <button class="delete-btn" onclick="adminApp.openDeleteModal(${user.id})">
                        <i class="fas fa-trash"></i> Delete
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
    }

    renderCurrentUserInfo() {
        if (!this.currentUser) return;

        // Update top bar
        const emailElement = document.getElementById('currentUserEmail');
        const rolesElement = document.getElementById('currentUserRoles');

        if (emailElement) emailElement.textContent = this.currentUser.email;
        if (rolesElement) {
            rolesElement.textContent = this.currentUser.roles
                .map(role => Utils.formatRoleName(role.name))
                .join(', ');
        }

        // Update user panel table
        const tbody = document.getElementById('currentUserTableBody');
        if (tbody) {
            tbody.innerHTML = `
                <tr>
                    <td>${this.currentUser.id}</td>
                    <td>${Utils.escapeHtml(this.currentUser.firstName)}</td>
                    <td>${Utils.escapeHtml(this.currentUser.lastName)}</td>
                    <td>${this.currentUser.age}</td>
                    <td>${Utils.escapeHtml(this.currentUser.email)}</td>
                    <td>
                        ${this.currentUser.roles.map(role =>
                `<span class="role-badge ${role.name.includes('ADMIN') ? 'role-admin' : 'role-user'}">
                                ${Utils.formatRoleName(role.name)}
                            </span>`
            ).join('')}
                    </td>
                </tr>
            `;
        }
    }

    switchMode(mode) {
        const adminBtn = document.getElementById('adminModeBtn');
        const userBtn = document.getElementById('userModeBtn');
        const adminPanel = document.getElementById('adminPanelContent');
        const userPanel = document.getElementById('userPanelContent');

        if (mode === 'admin') {
            adminBtn?.classList.add('active');
            userBtn?.classList.remove('active');
            if (adminPanel) adminPanel.style.display = 'block';
            if (userPanel) userPanel.style.display = 'none';
        } else {
            userBtn?.classList.add('active');
            adminBtn?.classList.remove('active');
            if (userPanel) userPanel.style.display = 'block';
            if (adminPanel) adminPanel.style.display = 'none';
        }
    }

    switchTab(tab) {
        const usersTab = document.getElementById('usersTableTab');
        const newUserTab = document.getElementById('newUserTab');
        const usersSection = document.getElementById('usersSection');
        const createPanel = document.getElementById('createUserPanel');

        if (tab === 'users') {
            usersTab?.classList.add('active');
            newUserTab?.classList.remove('active');
            if (usersSection) usersSection.style.display = 'block';
            if (createPanel) createPanel.style.display = 'none';
        } else {
            newUserTab?.classList.add('active');
            usersTab?.classList.remove('active');
            if (usersSection) usersSection.style.display = 'none';
            if (createPanel) createPanel.style.display = 'block';
            this.renderRolesSelect('createRoles');
        }
    }

    clearCreateForm() {
        const form = document.getElementById('createUserForm');
        if (form) form.reset();

        // Clear any error highlights
        this.clearFieldErrors('createFirstName');
        this.clearFieldErrors('createLastName');
        this.clearFieldErrors('createAge');
        this.clearFieldErrors('createEmail');
        this.clearFieldErrors('createPassword');
    }

    // Edit Modal Methods
    async openEditModal(userId) {
        try {
            const user = await ApiClient.get(`/api/admin/users/${userId}`);
            this.currentEditingUserId = user.id;

            document.getElementById('editUserId').value = user.id;
            document.getElementById('displayId').textContent = user.id;
            document.getElementById('editFirstName').value = user.firstName || '';
            document.getElementById('editLastName').value = user.lastName || '';
            document.getElementById('editAge').value = user.age || '';
            document.getElementById('editEmail').value = user.email || '';

            // Select roles with current user roles selected
            this.renderRolesSelect('editRoles', user.roles);

            // Clear any previous errors
            this.clearFieldErrors('editEmail');

            document.getElementById('editUserModal').style.display = 'block';
        } catch (error) {
            console.error('Error loading user:', error);
            NotificationManager.showApiError(error, 'Error loading user data');
        }
    }

    closeEditModal() {
        const modal = document.getElementById('editUserModal');
        if (modal) modal.style.display = 'none';
        this.currentEditingUserId = null;
    }

    // Delete Modal Methods
    async openDeleteModal(userId) {
        try {
            const user = await ApiClient.get(`/api/admin/users/${userId}`);
            this.currentDeletingUserId = user.id;

            document.getElementById('deleteId').textContent = user.id;
            document.getElementById('deleteFirstName').textContent = user.firstName || 'N/A';
            document.getElementById('deleteLastName').textContent = user.lastName || 'N/A';
            document.getElementById('deleteAge').textContent = user.age || 'N/A';
            document.getElementById('deleteEmail').textContent = user.email || 'N/A';
            document.getElementById('deleteRole').textContent =
                user.roles.map(role => Utils.formatRoleName(role.name)).join(', ') || 'No roles';

            document.getElementById('deleteUserModal').style.display = 'block';
        } catch (error) {
            console.error('Error loading user:', error);
            NotificationManager.showApiError(error, 'Error loading user data');
        }
    }

    closeDeleteModal() {
        const modal = document.getElementById('deleteUserModal');
        if (modal) modal.style.display = 'none';
        this.currentDeletingUserId = null;
    }

    // User Operations
    async createUser() {
        // Защита от двойного нажатия
        if (this.isCreating) {
            console.log('Create user already in progress, ignoring duplicate call');
            return;
        }

        console.log('Creating user...');

        let userData;

        // В вашем HTML используется только форма в панели
        const createForm = document.getElementById('createUserForm');
        const createPanel = document.getElementById('createUserPanel');

        if (createForm && createPanel) {
            const formData = new FormData(createForm);
            userData = {
                firstName: formData.get('firstName'),
                lastName: formData.get('lastName'),
                age: parseInt(formData.get('age')),
                email: formData.get('email'),
                password: formData.get('password'),
                roles: formData.getAll('roles')
            };
        } else {
            console.error('Create form not found');
            NotificationManager.show('Create form not found', 'error');
            return;
        }

        // Validation
        if (!this.validateUserData(userData)) {
            return;
        }

        this.isCreating = true;
        LoadingManager.show();

        // Отключаем кнопку отправки
        this.disableCreateButton();

        try {
            console.log('Sending user data:', userData);
            const response = await ApiClient.post('/api/admin/users', userData);
            console.log('User created successfully:', response);

            NotificationManager.show('User created successfully', 'success');

            // Clear form
            this.clearCreateForm();

            // Switch to users tab
            this.switchTab('users');

            // Reload data
            await this.reloadUsers();

        } catch (error) {
            console.error('Error creating user:', error);

            // Handle validation errors
            if (error.data && error.data.field === 'email') {
                this.highlightFieldError('createEmail', error.data.error);
                NotificationManager.show(error.data.error, 'error');
            } else {
                NotificationManager.showApiError(error, 'Error creating user');
            }
        } finally {
            LoadingManager.hide();
            this.isCreating = false;

            // Включаем кнопку обратно
            this.enableCreateButton();
        }
    }

    async updateUser() {
        // Защита от двойного нажатия
        if (this.isUpdating) {
            console.log('Update user already in progress, ignoring duplicate call');
            return;
        }

        const formData = new FormData(document.getElementById('editUserForm'));
        const userData = {
            firstName: formData.get('firstName'),
            lastName: formData.get('lastName'),
            age: parseInt(formData.get('age')),
            email: formData.get('email'),
            roles: formData.getAll('roles')
        };

        const password = formData.get('password');
        if (password && password.trim() !== '') {
            userData.password = password;
        }

        // Validation
        if (!this.validateUserData(userData, false)) {
            return;
        }

        this.isUpdating = true;
        LoadingManager.show();

        // Отключаем кнопку сохранения
        this.disableSaveButton();

        try {
            await ApiClient.put(`/api/admin/users/${this.currentEditingUserId}`, userData);
            NotificationManager.show('User updated successfully', 'success');
            this.closeEditModal();

            await this.reloadUsers();

        } catch (error) {
            console.error('Error updating user:', error);

            if (error.data && error.data.field === 'email') {
                this.highlightFieldError('editEmail', error.data.error);
                NotificationManager.show(error.data.error, 'error');
            } else {
                NotificationManager.showApiError(error, 'Error updating user');
            }
        } finally {
            LoadingManager.hide();
            this.isUpdating = false;

            // Включаем кнопку обратно
            this.enableSaveButton();
        }
    }

    async confirmDelete() {
        if (!this.currentDeletingUserId) return;

        LoadingManager.show();
        try {
            await ApiClient.delete(`/api/admin/users/${this.currentDeletingUserId}`);
            NotificationManager.show('User deleted successfully', 'success');
            this.closeDeleteModal();

            await this.reloadUsers();

        } catch (error) {
            console.error('Error deleting user:', error);
            NotificationManager.showApiError(error, 'Error deleting user');
        } finally {
            LoadingManager.hide();
        }
    }

    // Button Management Methods
    disableCreateButton() {
        const submitBtn = document.querySelector('#createUserForm button[type="submit"]');
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = 'Creating...';
            submitBtn.style.opacity = '0.6';
        }
    }

    enableCreateButton() {
        const submitBtn = document.querySelector('#createUserForm button[type="submit"]');
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Create User';
            submitBtn.style.opacity = '1';
        }
    }

    disableSaveButton() {
        const saveBtn = document.querySelector('#editUserForm button[type="submit"]');
        if (saveBtn) {
            saveBtn.disabled = true;
            saveBtn.textContent = 'Saving...';
            saveBtn.style.opacity = '0.6';
        }
    }

    enableSaveButton() {
        const saveBtn = document.querySelector('#editUserForm button[type="submit"]');
        if (saveBtn) {
            saveBtn.disabled = false;
            saveBtn.textContent = 'Save Changes';
            saveBtn.style.opacity = '1';
        }
    }

    // Utility Methods
    validateUserData(userData, isCreate = true) {
        // Clear previous errors
        this.clearAllFieldErrors();

        let isValid = true;

        if (!userData.firstName || userData.firstName.trim() === '') {
            this.highlightFieldError('createFirstName', 'First name is required');
            isValid = false;
        }

        if (!userData.lastName || userData.lastName.trim() === '') {
            this.highlightFieldError('createLastName', 'Last name is required');
            isValid = false;
        }

        if (!userData.email || userData.email.trim() === '') {
            this.highlightFieldError('createEmail', 'Email is required');
            isValid = false;
        } else if (!Utils.validateEmail(userData.email)) {
            this.highlightFieldError('createEmail', 'Please enter a valid email address');
            isValid = false;
        }

        if (isCreate && (!userData.password || userData.password.trim() === '')) {
            this.highlightFieldError('createPassword', 'Password is required');
            isValid = false;
        }

        if (!userData.roles || userData.roles.length === 0) {
            NotificationManager.show('Please select at least one role', 'error');
            isValid = false;
        }

        return isValid;
    }

    highlightFieldError(fieldId, errorMessage) {
        const field = document.getElementById(fieldId);
        if (!field) return;

        field.classList.add('error-field');

        let errorElement = field.parentNode.querySelector('.field-error');
        if (!errorElement) {
            errorElement = document.createElement('div');
            errorElement.className = 'field-error';
            field.parentNode.appendChild(errorElement);
        }
        errorElement.textContent = errorMessage;

        const removeError = () => {
            field.classList.remove('error-field');
            if (errorElement && errorElement.parentNode) {
                errorElement.parentNode.removeChild(errorElement);
            }
            field.removeEventListener('input', removeError);
            field.removeEventListener('change', removeError);
        };

        field.addEventListener('input', removeError);
        field.addEventListener('change', removeError);
    }

    clearFieldErrors(fieldId) {
        const field = document.getElementById(fieldId);
        if (field) {
            field.classList.remove('error-field');
            const errorElement = field.parentNode.querySelector('.field-error');
            if (errorElement && errorElement.parentNode) {
                errorElement.parentNode.removeChild(errorElement);
            }
        }
    }

    clearAllFieldErrors() {
        const errorFields = document.querySelectorAll('.error-field');
        errorFields.forEach(field => field.classList.remove('error-field'));

        const errorMessages = document.querySelectorAll('.field-error');
        errorMessages.forEach(error => error.remove());
    }
}

// Инициализация приложения когда DOM загружен
document.addEventListener('DOMContentLoaded', () => {
    window.adminApp = new AdminApp();
});