export function getCurrentPlayer() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('player') || localStorage.getItem('currentPlayer') || 'Alex_Player';
}

export function formatTime() {
    return new Date().toLocaleTimeString();
}

export function updateTimestamp() {
    const timestamp = document.getElementById('timestamp');
    if (timestamp) {
        timestamp.innerText = formatTime();
    }
}

export function getStatusColor(status) {
    switch(status) {
        case 'completed': return '#00ff88';
        case 'failed': return '#ff4444';
        case 'active': return '#ffd700';
        default: return '#888';
    }
}

export function getStatusSymbol(status) {
    switch(status) {
        case 'completed': return '✓';
        case 'failed': return '✗';
        case 'active': return '●';
        default: return '○';
    }
}

export function getScoreClass(score) {
    if (score >= 0.7) return 'score-high';
    if (score <= 0.3) return 'score-low';
    return 'score-mid';
}

export function getExplanationByType(type) {
    switch(type) {
        case 'Kill': return 'Игрок имеет высокий показатель PvE, успешно убивает мобов';
        case 'Break': return 'Игрок активно добывает ресурсы, высокий skill mining';
        case 'Collect': return 'Игрок предпочитает собирательство, low risk high reward';
        default: return 'Адаптировано под стиль игры игрока';
    }
}