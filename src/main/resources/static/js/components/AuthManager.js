import Modal from './Modal.js';

let instance = null;

/**
 * AuthManager 是一个单例，用于处理 API 请求认证失败时的重新登录流程。
 * 当需要用户凭证时，它会弹出一个模态框，并返回一个 Promise，
 * 该 Promise 在用户成功登录后解析为新的 token 和 user 对象。
 */
class AuthManager {
    constructor() {
        if (instance) {
            return instance;
        }
        this.isModalOpen = false;
        this.resolvePromise = null;
        this.rejectPromise = null;
        this.authModal = null;
        instance = this;
    }

    static getInstance() {
        if (!instance) {
            instance = new AuthManager();
        }
        return instance;
    }

    /**
     * 请求用户凭证。如果认证模态框未打开，则显示它。
     * @returns {Promise<{token: string, user: object}>} 成功登录后解析为包含 token 和 user 信息的对象。
     */
    requestCredentials() {
        if (this.isModalOpen) {
            // 如果一个认证流程已在进行中，则立即拒绝新的请求，防止多个模态框。
            return Promise.reject(new Error("认证流程已在进行中。"));
        }

        this.isModalOpen = true;

        return new Promise((resolve, reject) => {
            this.resolvePromise = resolve;
            this.rejectPromise = reject;
            this._createAndShowModal();
        });
    }

    /**
     * 创建并显示登录模态框。
     * @private
     */
    _createAndShowModal() {
        const bodyHtml = `
            <div class="alert alert-warning" role="alert">
                您的会话已过期或无权访问，请重新登录。
            </div>
            <form id="reauth-form">
                <div class="mb-3">
                    <label for="reauth-username" class="form-label">用户名</label>
                    <input type="text" class="form-control" id="reauth-username" required autocomplete="username">
                </div>
                <div class="mb-3">
                    <label for="reauth-password" class="form-label">密码</label>
                    <input type="password" class="form-control" id="reauth-password" required autocomplete="current-password">
                </div>
                <div id="reauth-error" class="text-danger mt-2" style="display: none;"></div>
            </form>
        `;

        const footerHtml = `
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" id="reauth-submit-btn">登录</button>
        `;

        this.authModal = new Modal({
            title: '需要认证',
            body: bodyHtml,
            footer: footerHtml,
            staticBackdrop: true
        });

        const form = this.authModal.modalElement.querySelector('#reauth-form');
        const submitBtn = this.authModal.modalElement.querySelector('#reauth-submit-btn');
        const errorDiv = this.authModal.modalElement.querySelector('#reauth-error');

        const handleLogin = async () => {
            const username = form.querySelector('#reauth-username').value;
            const password = form.querySelector('#reauth-password').value;

            if (!username || !password) {
                errorDiv.textContent = '请输入用户名和密码。';
                errorDiv.style.display = 'block';
                return;
            }

            submitBtn.disabled = true;
            submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 登录中...';
            errorDiv.style.display = 'none';

            try {
                const response = await fetch('/api/system/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ loginid: username, password: password })
                });

                if (!response.ok) {
                    const errorText = await response.text();
                    throw new Error(errorText || '登录失败，请检查您的凭证。');
                }

                const data = await response.json();
                const { token, user } = data;

                // 存储新的 token
                localStorage.setItem('jwt_token', token);

                // 派发全局事件，通知 app.js 更新UI
                window.dispatchEvent(new CustomEvent('userSwitched', { detail: { user } }));

                // [核心] 解析 Promise，将新的 token 和 user 信息传递出去
                this.resolvePromise({ token, user });
                this.authModal.hide();

            } catch (error) {
                errorDiv.textContent = error.message;
                errorDiv.style.display = 'block';
            } finally {
                submitBtn.disabled = false;
                submitBtn.innerHTML = '登录';
            }
        };

        submitBtn.addEventListener('click', handleLogin);
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            handleLogin();
        });

        this.authModal.modalElement.addEventListener('hidden.bs.modal', () => {
            this.isModalOpen = false;
            // 如果 Promise 仍然是 pending 状态（意味着用户关闭了模态框而不是登录），则拒绝它。
            if (this.rejectPromise) {
                this.rejectPromise(new Error("用户取消了认证。"));
            }
            this.resolvePromise = null;
            this.rejectPromise = null;
            this.authModal = null; // 清理 DOM 引用
        }, { once: true });

        this.authModal.show();
    }
}

export default AuthManager.getInstance();

