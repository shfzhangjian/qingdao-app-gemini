/**
 * @file /js/sso.js
 * @description 处理单点登录（SSO）重定向，从URL hash中提取Token并存储。
 */

window.addEventListener('load', () => {
    const hash = window.location.hash;

    // 检查 hash 是否存在且包含Token内容
    if (hash && hash.length > 1) {
        // 移除开头的 '#'，获取纯Token字符串
        const token = hash.substring(1);

        console.log("SSO: 成功获取到Token, 正在存储...");

        // 将Token存储到 localStorage，这是前端应用后续所有API请求的认证依据
        localStorage.setItem('jwt_token', token);

        console.log("SSO: Token已存储, 正在跳转到主页...");

        // 使用 replace 跳转到主应用页面，这样可以防止用户通过“后退”按钮回到这个中转页
        window.location.replace('index.html');

    } else {
        console.error("SSO: 未在URL hash中找到Token，认证失败。");
        // 如果没有Token，跳转到标准的登录页并提示错误
        window.location.replace('/login.html?error=sso_token_missing');
    }
});
