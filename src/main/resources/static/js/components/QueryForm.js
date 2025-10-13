/**
 * 源码路径: js/components/QueryForm.js
 * 功能说明: 根据配置动态创建查询表单和工具栏。
 * 版本变动:
 * v1.2.0 - 2025-10-13: 新增键盘支持，可通过回车查询和箭头键移动焦点。
 */

import DatePicker from './DatePicker.js';

export default class QueryForm {
    constructor({ fields = [], actions = [] }) {
        this.fields = fields;
        this.actions = actions;
        this.container = null;
        this.callbacks = {};
        this.datePickers = []; // Store date picker instances
    }

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
        this._initializeDatePickers(); // Initialize after rendering
    }

    _createFieldsHtml() {
        return this.fields.map(field => {
            const commonParts = `<label class="form-label mb-0 flex-shrink-0" ${field.labelWidth ? `style="width:${field.labelWidth}; text-align: right; margin-right: 0.5rem;"` : ''}>${field.label}:</label>`;

            switch (field.type) {
                case 'text':
                    return `<div class="d-flex align-items-center ${field.containerClass || ''}">${commonParts}<input type="text" name="${field.name}" class="form-control form-control-sm" ${field.style ? `style="${field.style}"` : ''} value="${field.defaultValue || ''}"></div>`;
                case 'pills':
                    const optionsHtml = field.options.map(opt => `<input type="radio" class="btn-check" name="${field.name}" id="${field.name}-${opt.value}" value="${opt.value}" autocomplete="off" ${opt.checked ? 'checked' : ''}><label class="btn btn-outline-secondary" for="${field.name}-${opt.value}">${opt.label}</label>`).join('');
                    return `<div class="d-flex align-items-center gap-2 ${field.containerClass || ''}">${commonParts}<div class="btn-group btn-group-sm" role="group">${optionsHtml}</div></div>`;
                case 'daterange':
                    return `<div class="d-flex align-items-center ${field.containerClass || ''}">${commonParts}<input type="text" name="${field.name}" class="form-control form-control-sm" ${field.style ? `style="${field.style}"` : ''} placeholder="请选择日期范围..."></div>`;
                default:
                    return '';
            }
        }).join('');
    }

    _initializeDatePickers() {
        this.destroyDatePickers(); // Clean up old instances first
        this.fields.forEach(field => {
            if (field.type === 'daterange') {
                const input = this.container.querySelector(`input[name="${field.name}"]`);
                if (input) {
                    this.datePickers.push(new DatePicker(input, field.options || {}));
                }
            }
        });
    }

    _createActionsHtml() {
        return this.actions.map(action => `<button class="btn btn-sm ${action.class || 'btn-primary'}" data-action="${action.name}">${action.text}</button>`).join('');
    }

    _attachEventListeners() {
        // --- Click Event Listeners ---
        this.container.addEventListener('click', (e) => {
            const button = e.target.closest('button[data-action]');
            if (button) {
                e.preventDefault();
                this._trigger(button.dataset.action, this.getValues());
            }
        });

        // --- Keyboard Event Listeners ---
        this.container.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && e.target.matches('input')) {
                e.preventDefault();
                const searchButton = this.container.querySelector('[data-action="search"]') || this.container.querySelector('button[data-action]');
                searchButton?.click();
            }

            if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(e.key)) {
                e.preventDefault();
                const focusable = Array.from(this.container.querySelectorAll('input, button'));
                const currentIndex = focusable.indexOf(document.activeElement);

                if (currentIndex === -1) {
                    focusable[0]?.focus();
                    return;
                }

                let nextIndex;
                if (e.key === 'ArrowRight' || e.key === 'ArrowDown') {
                    nextIndex = (currentIndex + 1) % focusable.length;
                } else {
                    nextIndex = (currentIndex - 1 + focusable.length) % focusable.length;
                }
                focusable[nextIndex]?.focus();
            }
        });
    }

    getValues() {
        const values = {};
        this.fields.forEach(field => {
            const input = this.container.querySelector(`[name="${field.name}"]`);
            if (!input) return;

            if (field.type === 'pills') {
                const checkedInput = this.container.querySelector(`input[name="${field.name}"]:checked`);
                if (checkedInput) values[field.name] = checkedInput.value;
            } else {
                values[field.name] = input.value;
            }
        });
        return values;
    }

    on(eventName, callback) {
        if (typeof callback === 'function') {
            this.callbacks[eventName] = callback;
        }
    }

    _trigger(eventName, data) {
        if (this.callbacks[eventName]) {
            this.callbacks[eventName](data);
        }
    }

    destroyDatePickers() {
        this.datePickers.forEach(dp => dp.destroy());
        this.datePickers = [];
    }
}

