/**
 * @file /js/components/QueryForm.js
 * @description 动态查询表单/工具栏组件
 * @version 1.0.0 - 2025-10-13 - Gemini - 初始创建并添加文件头注释
 */

/**
 * A component for dynamically creating query forms and toolbars.
 */
export default class QueryForm {
    /**
     * @param {object} config
     * @param {Array<object>} config.fields - Configuration for form fields.
     * @param {Array<object>} config.actions - Configuration for action buttons.
     */
    constructor({ fields = [], actions = [] }) {
        this.fields = fields;
        this.actions = actions;
        this.container = null;
        this.callbacks = {}; // Simple event handler store
    }

    /**
     * Renders the form into a specified container.
     * @param {HTMLElement} container - The element to render the form into.
     */
    render(container) {
        if (!container) {
            console.error("QueryForm requires a container element to render.");
            return;
        }
        this.container = container;

        const fieldsHtml = this._createFieldsHtml();
        const actionsHtml = this._createActionsHtml();

        // The structure can be customized based on needs, e.g., putting actions inside the same container.
        this.container.innerHTML = `
            <div class="d-flex flex-wrap align-items-center gap-3 mb-3 p-3 rounded" style="background-color: var(--bg-dark-primary);">
                ${fieldsHtml}
            </div>
            <div class="d-flex justify-content-start gap-2 mb-3">
                ${actionsHtml}
            </div>
        `;

        this._attachEventListeners();
    }

    /**
     * Creates HTML for form fields.
     * @private
     */
    _createFieldsHtml() {
        return this.fields.map(field => {
            const commonParts = `
                <label class="form-label mb-0 flex-shrink-0" ${field.labelWidth ? `style="width:${field.labelWidth}; text-align: right; margin-right: 0.5rem;"` : ''}>
                    ${field.label}:
                </label>`;

            switch (field.type) {
                case 'text':
                    return `
                        <div class="d-flex align-items-center ${field.containerClass || ''}">
                            ${commonParts}
                            <input type="text" name="${field.name}" class="form-control form-control-sm" ${field.style ? `style="${field.style}"` : ''} value="${field.defaultValue || ''}">
                        </div>`;
                case 'pills':
                    const optionsHtml = field.options.map(opt => `
                        <input type="radio" class="btn-check" name="${field.name}" id="${field.name}-${opt.value}" autocomplete="off" ${opt.checked ? 'checked' : ''}>
                        <label class="btn btn-outline-secondary" for="${field.name}-${opt.value}">${opt.label}</label>
                    `).join('');
                    return `
                        <div class="d-flex align-items-center gap-2 ${field.containerClass || ''}">
                            ${commonParts}
                            <div class="btn-group btn-group-sm" role="group">
                                ${optionsHtml}
                            </div>
                        </div>`;
                default:
                    return '';
            }
        }).join('');
    }

    /**
     * Creates HTML for action buttons.
     * @private
     */
    _createActionsHtml() {
        return this.actions.map(action => `
            <button class="btn btn-sm ${action.class || 'btn-primary'}" data-action="${action.name}">
                ${action.text}
            </button>
        `).join('');
    }

    /**
     * Attaches event listeners for action buttons.
     * @private
     */
    _attachEventListeners() {
        this.actions.forEach(action => {
            const button = this.container.querySelector(`[data-action="${action.name}"]`);
            if (button) {
                button.addEventListener('click', (e) => {
                    e.preventDefault();
                    this._trigger(action.name, this.getValues());
                });
            }
        });
    }

    /**
     * Gets the current values of all form fields.
     * @returns {object} - An object containing field names and their values.
     */
    getValues() {
        const values = {};
        this.fields.forEach(field => {
            if (field.type === 'text') {
                const input = this.container.querySelector(`input[name="${field.name}"]`);
                if (input) values[field.name] = input.value;
            } else if (field.type === 'pills') {
                const checkedInput = this.container.querySelector(`input[name="${field.name}"]:checked`);
                if (checkedInput) values[field.name] = checkedInput.id.split('-').pop();
            }
        });
        return values;
    }

    /**
     * Registers a callback for a specific action.
     * @param {string} eventName - The name of the action/event (e.g., 'search').
     * @param {function} callback - The function to call when the event is triggered.
     */
    on(eventName, callback) {
        if (typeof callback === 'function') {
            this.callbacks[eventName] = callback;
        }
    }

    /**
     * Triggers a registered callback.
     * @private
     */
    _trigger(eventName, data) {
        if (this.callbacks[eventName]) {
            this.callbacks[eventName](data);
        }
    }
}

