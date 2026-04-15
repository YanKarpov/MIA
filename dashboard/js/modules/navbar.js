class NavBar extends HTMLElement {
    connectedCallback() {
        const currentPage = window.location.pathname.split('/').pop() || 'index.html';
        this.innerHTML = `
            <nav class="nav-bar">
                <a href="index.html" class="nav-link ${currentPage === 'index.html' ? 'active' : ''}">🎮 ML Ранжирование</a>
                <a href="database.html" class="nav-link ${currentPage === 'database.html' ? 'active' : ''}">🗄️ База данных</a>
                <a href="stats.html" class="nav-link ${currentPage === 'stats.html' ? 'active' : ''}">📊 Статистика</a>
                <a href="settings.html" class="nav-link ${currentPage === 'settings.html' ? 'active' : ''}">⚙️ Настройки</a>
            </nav>
        `;
    }
}
customElements.define('nav-bar', NavBar);