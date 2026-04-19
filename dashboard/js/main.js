import { CONFIG, ENDPOINTS } from './modules/config.js';
import { updateTimestamp, formatTime } from './modules/utils.js';
import { 
    setCurrentCandidates, 
    renderRanking, 
    renderRecentQuests, 
    renderPlayers, 
    renderLogs,
    renderDatabaseTable,
    renderDatabaseStats,
    renderStatsCharts,
    renderSettingsForm
} from './modules/render.js';
import { 
    addConsoleLine, 
    addLogMessage, 
    refreshAll, 
    refreshRankingOnly, 
    clearLogs, 
    onQuestCommand,
    loadDatabaseData,
    loadStatsData,
    saveSettings,
    loadSettingsFromStorage
} from './modules/handlers.js';
import { loadCandidates, loadRecentQuests, loadPlayersStats, renderStatusBadges } from './modules/api.js';

function getCurrentPage() {
    const path = window.location.pathname.split('/').pop();
    if (path === '' || path === 'index.html') return 'index';
    if (path === 'database.html') return 'database';
    if (path === 'stats.html') return 'stats';
    if (path === 'settings.html') return 'settings';
    return 'index';
}

function highlightActiveNav() {
    const currentPage = getCurrentPage();
    const navLinks = document.querySelectorAll('.nav-link');
    
    navLinks.forEach(link => {
        const href = link.getAttribute('href');
        let page = null;
        
        if (href === 'index.html' || href === '/') page = 'index';
        else if (href === 'database.html') page = 'database';
        else if (href === 'stats.html') page = 'stats';
        else if (href === 'settings.html') page = 'settings';
        
        if (page === currentPage) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });
}

async function initIndexPage() {
    console.log('Initializing Index Page...');
    
    const candidates = await loadCandidates();
    setCurrentCandidates(candidates);
    renderRanking();
    
    const quests = await loadRecentQuests();
    renderRecentQuests(quests);
    
    const players = await loadPlayersStats();
    renderPlayers(players);
    
    const statsData = await loadStatsData();
    
    const latencyEl = document.getElementById('statLatency');
    const accuracyEl = document.getElementById('statAccuracy');
    const totalQuestsEl = document.getElementById('statTotalQuests');
    const avgScoreEl = document.getElementById('statAvgScore');
    
    if (latencyEl) latencyEl.innerText = statsData.avg_latency || '87';
    if (accuracyEl) accuracyEl.innerText = statsData.accuracy || '94';
    if (totalQuestsEl) totalQuestsEl.innerText = statsData.total_requests || '127';
    if (avgScoreEl) avgScoreEl.innerText = (statsData.avg_ml_score || 0.76).toFixed(2);
    
    renderLogs([]);
    
    addConsoleLine('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    addConsoleLine('Minecraft Quest ML Dashboard v1.0');
    addConsoleLine(`API: ${CONFIG.API_BASE}${ENDPOINTS.RANK}`);
    addConsoleLine('Режим: Гибридный (API + Запасные данные)');
    addConsoleLine('Введите /quest в Minecraft для ранжирования');
    addConsoleLine('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    
    setTimeout(() => {
        addConsoleLine('Демонстрация: симуляция команды /quest...');
        onQuestCommand();
    }, CONFIG.AUTO_DEMO_DELAY);
}

async function initDatabasePage() {
    console.log('Initializing Database Page...');
    
    const statsData = await loadStatsData();
    
    const totalQuestsEl = document.getElementById('totalQuestsDb');
    const completedEl = document.getElementById('completedQuests');
    const failedEl = document.getElementById('failedQuests');
    const avgScoreEl = document.getElementById('avgMlScore');
    
    if (totalQuestsEl) totalQuestsEl.innerText = statsData.total_requests || '127';
    if (completedEl) completedEl.innerText = statsData.completed || '89';
    if (failedEl) failedEl.innerText = (statsData.total_requests - statsData.completed) || '23';
    if (avgScoreEl) avgScoreEl.innerText = (statsData.avg_ml_score || 0.76).toFixed(2);
    
    const questsData = await loadDatabaseData('quests');
    renderDatabaseTable(questsData, 'quests');
    
    document.querySelectorAll('.table-tab').forEach(tab => {
        tab.addEventListener('click', async () => {
            document.querySelectorAll('.table-tab').forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            const tableName = tab.dataset.table;
            const data = await loadDatabaseData(tableName);
            renderDatabaseTable(data, tableName);
        });
    });
    
    const refreshBtn = document.getElementById('refreshDbBtn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', async () => {
            const activeTab = document.querySelector('.table-tab.active');
            const tableName = activeTab?.dataset.table || 'quests';
            const data = await loadDatabaseData(tableName);
            renderDatabaseTable(data, tableName);
            
            const freshStats = await loadStatsData();
            if (totalQuestsEl) totalQuestsEl.innerText = freshStats.total_requests || '127';
            if (completedEl) completedEl.innerText = freshStats.completed || '89';
            if (failedEl) failedEl.innerText = (freshStats.total_requests - freshStats.completed) || '23';
            if (avgScoreEl) avgScoreEl.innerText = (freshStats.avg_ml_score || 0.76).toFixed(2);
            
            addConsoleLine('База данных обновлена');
        });
    }
    
    addConsoleLine('Страница базы данных загружена');
    addConsoleLine('Просмотр квестов, игроков и ML логов');
}

async function initStatsPage() {
    console.log('Initializing Stats Page...');
    
    const statsData = await loadStatsData();
    renderStatsCharts(statsData);
    
    const accuracyEl = document.getElementById('statAccuracy');
    const latencyEl = document.getElementById('statLatency');
    const requestsEl = document.getElementById('statRequests');
    const avgScoreEl = document.getElementById('statAvgScore');
    const topPlayerEl = document.getElementById('topPlayer');
    const mostActiveEl = document.getElementById('mostActive');
    const successRateEl = document.getElementById('successRate');
    
    if (accuracyEl) accuracyEl.innerText = statsData.accuracy || '94';
    if (latencyEl) latencyEl.innerText = statsData.avg_latency || '87';
    if (requestsEl) requestsEl.innerText = statsData.total_requests || '127';
    if (avgScoreEl) avgScoreEl.innerText = (statsData.avg_ml_score || 0.76).toFixed(2);
    if (topPlayerEl) topPlayerEl.innerText = statsData.top_player || 'MinerPro';
    if (mostActiveEl) mostActiveEl.innerText = statsData.most_active || 'Alex_Player';
    if (successRateEl) successRateEl.innerText = statsData.success_rate || '78';
    
    addConsoleLine('Страница статистики загружена');
    addConsoleLine(`Точность ML: ${statsData.accuracy || 94}%, Средняя задержка: ${statsData.avg_latency || 87}ms`);
}

async function initSettingsPage() {
    console.log('Initializing Settings Page...');
    
    renderSettingsForm();
    
    const savedThreshold = localStorage.getItem('ml_threshold') || '0.5';
    const savedCandidates = localStorage.getItem('candidates_count') || '5';
    const savedEndpoint = localStorage.getItem('api_endpoint') || '/api/rank';
    
    const thresholdInput = document.getElementById('mlThreshold');
    const thresholdSpan = document.getElementById('thresholdValue');
    const candidatesInput = document.getElementById('candidatesCount');
    const endpointInput = document.getElementById('apiEndpoint');
    
    if (thresholdInput) thresholdInput.value = savedThreshold;
    if (thresholdSpan) thresholdSpan.innerText = savedThreshold;
    if (candidatesInput) candidatesInput.value = savedCandidates;
    if (endpointInput) endpointInput.value = savedEndpoint;
    
    if (thresholdInput) {
        thresholdInput.addEventListener('input', (e) => {
            if (thresholdSpan) thresholdSpan.innerText = e.target.value;
        });
    }
    
    const saveBtn = document.getElementById('saveSettingsBtn');
    if (saveBtn) {
        saveBtn.addEventListener('click', () => {
            const settings = {
                ml_threshold: parseFloat(thresholdInput?.value || 0.5),
                candidates_count: parseInt(candidatesInput?.value || 5),
                api_endpoint: endpointInput?.value || '/api/rank'
            };
            saveSettings(settings);
            addConsoleLine(`Настройки сохранены: Порог=${settings.ml_threshold}, Кандидатов=${settings.candidates_count}`);
            addConsoleLine(`API Endpoint: ${settings.api_endpoint}`);
        });
    }
    
    loadMetricsFromStorage();
    
    const trainBtn = document.getElementById('trainModelBtn');
    if (trainBtn) {
        trainBtn.addEventListener('click', async () => {
            addConsoleLine('Запуск переобучения модели...');
            trainBtn.disabled = true;
            trainBtn.textContent = 'Обучение...';
            
            try {
                const response = await fetch('/api/train', { method: 'POST' });
                if (response.ok) {
                    const data = await response.json();
                    addConsoleLine(`Модель успешно обучена: ${data.message || 'Готово'}`);
                    
                    updateMetricsDisplay(data);
                    saveMetricsToStorage(data);
                    
                    const statusEl = document.getElementById('modelStatus');
                    if (statusEl) {
                        statusEl.innerHTML = 'Модель успешно обновлена';
                        setTimeout(() => {
                            statusEl.innerHTML = 'Модель загружена, готова к работе';
                        }, 3000);
                    }
                } else {
                    addConsoleLine(`Ошибка обучения: HTTP ${response.status}`);
                }
            } catch (e) {
                addConsoleLine(`Ошибка обучения: ${e.message}`);
            } finally {
                trainBtn.disabled = false;
                trainBtn.textContent = 'Обновить модель';
            }
        });
    }
    
    await updateModelStatus();
    
    addConsoleLine('Страница управления ML загружена');
    addConsoleLine('Настройка параметров и переобучение модели');
}

function loadMetricsFromStorage() {
    const savedMetrics = localStorage.getItem('train_metrics');
    if (savedMetrics) {
        const metrics = JSON.parse(savedMetrics);
        updateMetricsDisplay(metrics);
    }
}

function saveMetricsToStorage(metrics) {
    localStorage.setItem('train_metrics', JSON.stringify(metrics));
}

function updateMetricsDisplay(metrics) {
    const accuracyEl = document.getElementById('metricAccuracy');
    const f1El = document.getElementById('metricF1');
    const samplesEl = document.getElementById('metricSamples');
    const timeEl = document.getElementById('metricTime');
    
    if (accuracyEl && metrics.accuracy) accuracyEl.innerText = `${(metrics.accuracy * 100).toFixed(1)}%`;
    if (f1El && metrics.f1_score) f1El.innerText = metrics.f1_score.toFixed(3);
    if (samplesEl && metrics.samples) samplesEl.innerText = metrics.samples;
    if (timeEl && metrics.timestamp) timeEl.innerText = new Date(metrics.timestamp).toLocaleString();
}

async function updateModelStatus() {
    try {
        const response = await fetch('/api/health');
        if (response.ok) {
            const statusEl = document.getElementById('modelStatus');
            if (statusEl) {
                statusEl.innerHTML = 'Модель загружена, готова к работе';
            }
        } else {
            throw new Error('Health check failed');
        }
    } catch (e) {
        const statusEl = document.getElementById('modelStatus');
        if (statusEl) {
            statusEl.innerHTML = 'ML сервис недоступен';
        }
    }
}

async function init() {
    loadSettingsFromStorage();
    
    highlightActiveNav();
    await renderStatusBadges();
    
    const page = getCurrentPage();
    
    switch(page) {
        case 'index':
            await initIndexPage();
            break;
        case 'database':
            await initDatabasePage();
            break;
        case 'stats':
            await initStatsPage();
            break;
        case 'settings':
            await initSettingsPage();
            break;
        default:
            await initIndexPage();
    }
    
    setInterval(async () => {
        await renderStatusBadges();
        if (page === 'settings') {
            await updateModelStatus();
        }
    }, 10000);
}

init();

window.refreshAll = refreshAll;
window.refreshRanking = refreshRankingOnly;
window.clearLogs = clearLogs;
window.onQuestCommand = onQuestCommand;

document.addEventListener('DOMContentLoaded', () => {
    const refreshBtn = document.getElementById('refreshRankingBtn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', () => {
            console.log('Кнопка обновления нажата');
            refreshRankingOnly();
        });
    }
    
    const clearBtn = document.getElementById('clearLogsBtn');
    if (clearBtn) {
        clearBtn.addEventListener('click', () => {
            console.log('Кнопка очистки нажата');
            clearLogs();
        });
    }
});