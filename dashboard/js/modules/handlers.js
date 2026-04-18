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
        addLogMessage(`ML ranking completed. Best score: ${bestScore.toFixed(2)}`, 'success');
        addConsoleLine(`/quest → Best quest selected (score: ${bestScore.toFixed(2)})`);
    }, 300);
}

export async function loadDatabaseData(tableName) {
    try {
        const response = await fetch(`${CONFIG.API_BASE}/db/${tableName}?limit=50`);
        if (response.ok) {
            return await response.json();
        }
    } catch (e) {
        console.warn(`Failed to load ${tableName}:`, e);
    }
    
    if (tableName === 'quests') return [...FALLBACK_DATA.quests];
    if (tableName === 'players') return [...FALLBACK_DATA.players];
    if (tableName === 'stats') return { ...FALLBACK_DATA.dbStats };
    return [];
}

export async function loadStatsData() {
    try {
        const response = await fetch(`${CONFIG.API_BASE}/stats/summary`);
        if (response.ok) {
            return await response.json();
        }
    } catch (e) {
        console.warn('Failed to load stats:', e);
    }
    
    return { ...FALLBACK_DATA.statsSummary };
}

export function saveSettings(settings) {
    localStorage.setItem('ml_threshold', settings.ml_threshold);
    localStorage.setItem('candidates_count', settings.candidates_count);
    localStorage.setItem('api_endpoint', settings.api_endpoint);
    
    CONFIG.API_BASE = settings.api_endpoint.replace('/rank', '');
    
    addLogMessage(`Settings saved: Threshold=${settings.ml_threshold}`, 'success');
}