/**
 * @file /js/components/Modal.js
 * @description 模态窗口组件，封装Bootstrap Modal，提供便捷的alert和confirm方法
 * @version 1.0.0 - 2025-10-13 - Gemini - 初始创建并添加文件头注释
 */

/**
 * A reusable Modal component wrapping Bootstrap's Modal functionality.
 */
export default class Modal {
    /**
     * @param {object} options
     * @param {string} options.title - The title of the modal.
     * @param {string|HTMLElement} options.body - The content of the modal body.
     * @param {string} [options.footer] - HTML string for the modal footer.
     * @param {'sm'|'lg'|'xl'} [options.size] - The size of the modal.
     * @param {boolean} [options.static=false] - If true, modal won't close on backdrop click.
     */
    constructor({ title, body, footer = '', size = '', staticBackdrop = false }) {
        this.title = title;
        this.body = body;
        this.footer = footer;
        this.size = size;
        this.staticBackdrop = staticBackdrop;

        this.modalElement = this._createModalElement();
        document.body.appendChild(this.modalElement);

        const modalOptions = {};
        if (this.staticBackdrop) {
            modalOptions.backdrop = 'static';
            modalOptions.keyboard = false;
        }

        this.bootstrapModal = new bootstrap.Modal(this.modalElement, modalOptions);

        this.modalElement.addEventListener('hidden.bs.modal', () => {
            this.modalElement.remove();
        });
    }

    /**
     * Creates the modal's HTML structure.
     * @private
     */
    _createModalElement() {
        const modalId = `modal-${Date.now()}`;
        const modalSizeClass = this.size ? `modal-${this.size}` : '';

        const element = document.createElement('div');
        element.className = 'modal fade';
        element.id = modalId;
        element.tabIndex = -1;
        element.setAttribute('aria-labelledby', `${modalId}-label`);
        element.setAttribute('aria-hidden', 'true');

        element.innerHTML = `
            <div class="modal-dialog ${modalSizeClass}">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="${modalId}-label">${this.title}</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        
                    </div>
                    ${this.footer ? `<div class="modal-footer">${this.footer}</div>` : ''}
                </div>
            </div>
        `;

        const bodyContainer = element.querySelector('.modal-body');
        if (typeof this.body === 'string') {
            bodyContainer.innerHTML = this.body;
        } else if (this.body instanceof HTMLElement) {
            bodyContainer.appendChild(this.body);
        }

        return element;
    }

    /**
     * Shows the modal.
     */
    show() {
        this.bootstrapModal.show();
    }

    /**
     * Hides the modal.
     */
    hide() {
        this.bootstrapModal.hide();
    }

    /**
     * Static method to quickly show an alert dialog.
     * @param {string} message - The message to display.
     * @param {string} [title='提示'] - The title of the alert.
     */
    static alert(message, title = '提示') {
        const footer = `<button type="button" class="btn btn-primary" data-bs-dismiss="modal">确定</button>`;
        const alertModal = new Modal({ title, body: message, footer });
        alertModal.show();
    }

    /**
     * Static method to show a confirmation dialog.
     * @param {string} message - The message to display.
     * @param {string} [title='请确认'] - The title of the confirmation.
     * @returns {Promise<boolean>} - A promise that resolves to true if confirmed, false otherwise.
     */
    static confirm(message, title = '请确认') {
        return new Promise((resolve) => {
            const modalId = `confirm-${Date.now()}`;
            const footer = `
                <button type="button" class="btn btn-secondary" data-action="cancel">取消</button>
                <button type="button" class="btn btn-primary" data-action="confirm">确定</button>
            `;
            const confirmModal = new Modal({ title, body: message, footer, staticBackdrop: true });

            const confirmBtn = confirmModal.modalElement.querySelector('[data-action="confirm"]');
            const cancelBtn = confirmModal.modalElement.querySelector('[data-action="cancel"]');
            const closeBtn = confirmModal.modalElement.querySelector('.btn-close');

            confirmBtn.addEventListener('click', () => {
                resolve(true);
                confirmModal.hide();
            });

            const closeModal = () => {
                resolve(false);
                confirmModal.hide();
            }

            cancelBtn.addEventListener('click', closeModal);
            closeBtn.addEventListener('click', closeModal);

            confirmModal.show();
        });
    }
}

