/**
 * 源码路径: js/components/DatePicker.js
 * 功能说明: 封装了 flatpickr 的日期选择器组件。
 * 版本变动:
 * v1.0.0 - 2025-10-13: 初始版本。
 */

export default class DatePicker {
    /**
     * @param {HTMLInputElement} element - The input element to attach the date picker to.
     * @param {object} options - Options to pass to flatpickr.
     */
    constructor(element, options = {}) {
        if (!window.flatpickr) {
            console.error("Flatpickr library is not loaded.");
            return;
        }

        const defaultOptions = {
            mode: "range",
            dateFormat: "Y-m-d",
            locale: "zh", // Assumes the locale file is loaded globally
           // defaultDate: ["2025-06-01", "2025-06-30"],
        };

        this.instance = flatpickr(element, { ...defaultOptions, ...options });
    }

    destroy() {
        if (this.instance) {
            this.instance.destroy();
        }
    }
}
