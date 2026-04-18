import { CONFIG } from './config.js';
import { formatTime, getScoreClass, getStatusColor, getStatusSymbol, getExplanationByType } from './utils.js';

let currentCandidates = [];

export function renderRanking(candidates) {
    if (!candidates || candidates.length === 0) {
        candidates = currentCandidates;
    }
    
    const sorted = [...candidates].sort((a, b) => b.mlScore - a.mlScore);
    const best = sorted[0];
    
    let html = '<table class="ranking-table">';
    html += '<thead><tr><th>#</th><th>Тип</th><th>Цель</th><th>Кол-во</th><th>Награда</th><th>ML Score</th></tr></thead>';
    html += '<tbody>';
    
    candidates.forEach(c => {
        const isBest = c.id === best.id;
        const scoreClass = getScoreClass(c.mlScore);
        html += '<tr' + (isBest ? ' class="best-quest"' : '') + '>';
        html += '<td>' + (isBest ? '🏆 ' : '') + c.id + '</td>';
        html += '<td>' + c.type + '</td>';
        html += '<td>' + c.target + '</td>';
        html += '<td>' + c.amount + '</td>';
        html += '<td>' + c.reward + ' XP</td>';
        html += '<td class="' + scoreClass + '">' + c.mlScore.toFixed(2) + '</td>';
        html += '</tr>';
    });
    
    html += '</tbody></table>';
    const rankingTable = document.getElementById('rankingTable');
    if (rankingTable) rankingTable.innerHTML = html;
    
    const bestScorePercent = Math.round(best.mlScore * 100);
    const explanationBox = document.getElementById('explanationBox');
    if (explanationBox) {
        explanationBox.innerHTML = '<strong>Выбран квест:</strong> ' + best.type + ' ' + best.target + ' x' + best.amount + ' (оценка ' + best.mlScore.toFixed(2) + ')<br>' +
            '<strong>Вероятность успеха:</strong> ' + bestScorePercent + '%<br>' +
            '<strong>Ключевые факторы:</strong> ' + getExplanationByType(best.type) + '<br>' +
            '<strong>ML модель:</strong> MIA-RANK';
    }
}

export function renderRecentQuests(quests) {
    if (!quests) return;
    
    let html = '';
    quests.forEach(q => {
        const statusColor = getStatusColor(q.status);
        const statusSymbol = getStatusSymbol(q.status);
        html += '<div class="quest-item">';
        html += '<div class="quest-info">';
        html += '<span class="quest-name">' + (q.quest || (q.type + ' ' + q.target + ' x' + q.amount)) + '</span>';
        html += '<span class="quest-meta">' + q.player + ' • ' + (q.time || formatTime()) + '</span>';
        html += '</div>';
        html += '<div style="display: flex; gap: 12px; align-items: center;">';
        html += '<span class="quest-score">' + (q.score || 0).toFixed(2) + '</span>';
        html += '<span style="color: ' + statusColor + '; font-size: 11px;">' + statusSymbol + ' ' + q.status + '</span>';
        html += '</div></div>';
    });
    const recentQuests = document.getElementById('recentQuests');
    if (recentQuests) recentQuests.innerHTML = html || '<div style="padding: 20px; text-align: center; color: #888;">Нет данных</div>';
}

export function renderPlayers(players) {
    if (!players) return;
    
    let html = '<table class="players-table">';
    html += '<thead><tr><th>Игрок</th><th>Успешно</th><th>Провалов</th><th>Avg ML Score</th></tr></thead>';
    html += '<tbody>';
    
    players.forEach(p => {
        html += '<tr>';
        html += '<td style="color: var(--cyan); font-weight: 600;">' + p.name + '</td>';
        html += '<td style="color: #00ff88;">' + (p.completed || 0) + '</td>';
        html += '<td style="color: #ff4444;">' + (p.failed || 0) + '</td>';
        html += '<td style="color: #00f2fe;">' + (p.avgScore || 0).toFixed(2) + '</td>';
        html += '</tr>';
    });
    
    html += '</tbody></table>';
    const playersTable = document.getElementById('playersTable');
    if (playersTable) playersTable.innerHTML = html;
}

export function renderLogs(logs) {
    let html = '';
    logs.slice(0, CONFIG.LOGS_LIMIT).forEach(log => {
        let color = '#888';
        if (log.message.includes('error') || log.message.includes('Error')) color = '#ff4444';
        else if (log.message.includes('success')) color = '#00ff88';
        else if (log.message.includes('ML')) color = '#00f2fe';
        
        html += '<div class="log-entry">';
        html += '<span class="log-time">[' + log.time + ']</span>';
        html += '<span class="log-message" style="color: ' + color + ';">' + log.message + '</span>';
        html += '</div>';
    });
    const logsContainer = document.getElementById('logsContainer');
    if (logsContainer) logsContainer.innerHTML = html || '<div style="padding: 20px;">Нет логов</div>';
}

export function setCurrentCandidates(candidates) {
    currentCandidates = candidates;
}

export function getCurrentCandidates() {
    return currentCandidates;
}

export function renderDatabaseTable(data, tableName) {
    const container = document.getElementById('dbTableContainer');
    if (!container) return;
    
    if (!data || data.length === 0) {
        container.innerHTML = '<div style="padding: 40px; text-align: center; color: #888;">Нет данных</div>';
        return;
    }
    
    const columns = Object.keys(data[0]);
    let html = '<table class="db-table"><thead><tr>';
    columns.forEach(col => {
        html += '<th>' + col + '</th>';
    });
    html += '</tr></thead><tbody>';
    
    data.forEach(row => {
        html += '<tr>';
        columns.forEach(col => {
            let value = row[col];
            if (value === null) value = '—';
            if (col === 'ml_score' && typeof value === 'number') value = value.toFixed(3);
            if (col === 'created_at' && value) value = value.slice(0, 19).replace('T', ' ');
            html += '<td>' + value + '</td>';
        });
        html += '</tr>';
    });
    html += '</tbody></table>';
    container.innerHTML = html;
}

export function renderDatabaseStats(stats) {
    if (!stats) return;
    
    const totalEl = document.getElementById('totalQuestsDb');
    const completedEl = document.getElementById('completedQuests');
    const failedEl = document.getElementById('failedQuests');
    const avgScoreEl = document.getElementById('avgMlScore');
    
    if (totalEl) totalEl.innerText = stats.total_quests || 0;
    if (completedEl) completedEl.innerText = stats.completed || 0;
    if (failedEl) failedEl.innerText = stats.failed || 0;
    if (avgScoreEl) avgScoreEl.innerText = (stats.avg_ml_score || 0).toFixed(2);
}

let questTypeChart = null;
let mlScoreChart = null;

export function renderStatsCharts(data) {
    if (!data) return;
    
    const ctx1 = document.getElementById('questTypeChart');
    if (ctx1) {
        if (questTypeChart) questTypeChart.destroy();
        questTypeChart = new Chart(ctx1, {
            type: 'bar',
            data: {
                labels: data.type_labels || ['Kill', 'Break', 'Collect'],
                datasets: [{
                    label: 'Количество квестов',
                    data: data.type_counts || [45, 38, 22],
                    backgroundColor: ['#00f2fe', '#4facfe', '#00ff88'],
                    borderRadius: 8
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: { labels: { color: '#e0e0e0' } }
                }
            }
        });
    }
    
    const ctx2 = document.getElementById('mlScoreChart');
    if (ctx2) {
        if (mlScoreChart) mlScoreChart.destroy();
        mlScoreChart = new Chart(ctx2, {
            type: 'line',
            data: {
                labels: data.score_labels || ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10'],
                datasets: [{
                    label: 'ML Score',
                    data: data.score_values || [0.76, 0.82, 0.71, 0.91, 0.73, 0.88, 0.69, 0.94, 0.81, 0.87],
                    borderColor: '#00f2fe',
                    backgroundColor: 'rgba(0, 242, 254, 0.1)',
                    fill: true,
                    tension: 0.4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: { labels: { color: '#e0e0e0' } }
                },
                scales: {
                    y: { min: 0, max: 1, grid: { color: '#2a2a3e' }, ticks: { color: '#e0e0e0' } },
                    x: { grid: { color: '#2a2a3e' }, ticks: { color: '#e0e0e0' } }
                }
            }
        });
    }
}

export function renderSettingsForm() {
    console.log('Settings form rendered');
}