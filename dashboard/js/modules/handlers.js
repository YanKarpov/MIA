import { CONFIG } from './config.js';
import { formatTime, updateTimestamp } from './utils.js';
import { loadCandidates, loadRecentQuests, loadPlayersStats } from './api.js';
import { FALLBACK_DATA } from './data.js';
import { 
    setCurrentCandidates, 
    renderRanking, 
    renderRecentQuests, 
    renderPlayers, 
    renderLogs 
} from './render.js';

let globalLogs = [
    { time: formatTime(), message: 'Dashboard initialized' },
    { time: formatTime(), message: `ML Service endpoint: ${CONFIG.API_BASE}/rank` },
];

export function addLogMessage(message, type = 'info') {
    const time = formatTime();
    globalLogs.unshift({ time, message });
    if (globalLogs.length > CONFIG.LOGS_LIMIT) globalLogs.pop();
    renderLogs(globalLogs);
}

export function addConsoleLine(message) {
    const consoleDiv = document.getElementById('consoleOutput');
    if (!consoleDiv) return;
    const newLine = document.createElement('div');
    newLine.className = 'console-line';
    newLine.innerHTML = `> ${message}`;
    consoleDiv.appendChild(newLine);
    consoleDiv.scrollTop = consoleDiv.scrollHeight;
}

export function clearLogs() {
    globalLogs = [];
    addLogMessage('Logs cleared by user');
    renderLogs(globalLogs);
    addConsoleLine('> Logs cleared');
}

export async function refreshAll() {
    addConsoleLine('⟳ Refreshing all data...');
    
    const candidates = await loadCandidates();
    setCurrentCandidates(candidates);
    renderRanking();
    
    const quests = await loadRecentQuests();
    renderRecentQuests(quests);
    
    const players = await loadPlayersStats();
    renderPlayers(players);
    
    addConsoleLine('✓ Data refreshed');
    updateTimestamp();
}

export async function refreshRankingOnly() {
    addConsoleLine('⟳ Fetching ML ranking...');
    const candidates = await loadCandidates();
    setCurrentCandidates(candidates);
    renderRanking();
    const bestScore = Math.max(...candidates.map(c => c.mlScore));
    addConsoleLine(`✓ Ranking updated, best score: ${bestScore.toFixed(2)}`);
}

export async function onQuestCommand() {
    addLogMessage('📡 Received /quest command from Minecraft', 'info');
    addLogMessage('⚙️ Generating 5 candidate quests...', 'info');
    addLogMessage('🧠 ML ranking in progress...', 'info');
    
    setTimeout(async () => {
        const candidates = await loadCandidates();
        setCurrentCandidates(candidates);
        renderRanking();
        const bestScore = Math.max(...candidates.map(c => c.mlScore));
        addLogMessage(`✅ ML ranking completed. Best score: ${bestScore.toFixed(2)}`, 'success');
        addConsoleLine(`🎮 /quest → Best quest selected (score: ${bestScore.toFixed(2)})`);
    }, 300);
}

export async function loadDatabaseData(tableName) {
    if (!CONFIG.USE_REAL_API) {
        if (tableName === 'quests') return FALLBACK_DATA.quests;
        if (tableName === 'players') return FALLBACK_DATA.players;
        if (tableName === 'stats') return { total_quests: 127, completed: 89, failed: 23, avg_ml_score: 0.76 };
        return [];
    }
    
    try {
        const response = await fetch(`${CONFIG.API_BASE}/db/${tableName}?limit=50`);
        if (response.ok) {
            return await response.json();
        }
    } catch (e) {
        console.warn(`Failed to load ${tableName}:`, e);
    }
    
    if (tableName === 'quests') return FALLBACK_DATA.quests;
    if (tableName === 'players') return FALLBACK_DATA.players;
    return [];
}

export async function loadStatsData() {
    if (!CONFIG.USE_REAL_API) {
        return {
            accuracy: 94,
            avg_latency: 87,
            total_requests: 127,
            avg_ml_score: 0.76,
            top_player: 'MinerPro',
            most_active: 'Alex_Player',
            type_labels: ['Kill', 'Break', 'Collect'],
            type_counts: [45, 38, 22],
            score_labels: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10'],
            score_values: [0.76, 0.82, 0.71, 0.91, 0.73, 0.88, 0.69, 0.94, 0.81, 0.87],
            latency_labels: ['0-50', '50-100', '100-150', '150-200', '200+'],
            latency_counts: [12, 67, 34, 10, 4]
        };
    }
    
    try {
        const response = await fetch(`${CONFIG.API_BASE}/stats/summary`);
        if (response.ok) {
            return await response.json();
        }
    } catch (e) {
        console.warn('Failed to load stats:', e);
    }
    
    return {
        accuracy: 94,
        avg_latency: 87,
        total_requests: 127,
        avg_ml_score: 0.76,
        top_player: 'MinerPro',
        most_active: 'Alex_Player',
        type_labels: ['Kill', 'Break', 'Collect'],
        type_counts: [45, 38, 22],
        score_labels: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10'],
        score_values: [0.76, 0.82, 0.71, 0.91, 0.73, 0.88, 0.69, 0.94, 0.81, 0.87],
        latency_labels: ['0-50', '50-100', '100-150', '150-200', '200+'],
        latency_counts: [12, 67, 34, 10, 4]
    };
}

export function saveSettings(settings) {
    localStorage.setItem('ml_threshold', settings.ml_threshold);
    localStorage.setItem('candidates_count', settings.candidates_count);
    localStorage.setItem('api_mode', settings.api_mode);
    localStorage.setItem('api_endpoint', settings.api_endpoint);
    
    CONFIG.USE_REAL_API = settings.api_mode === 'real';
    CONFIG.API_BASE = settings.api_endpoint.replace('/rank', '');
    
    addLogMessage(`Settings saved: Threshold=${settings.ml_threshold}, Mode=${settings.api_mode}`, 'success');
}