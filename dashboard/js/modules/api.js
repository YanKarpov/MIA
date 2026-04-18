import { CONFIG, ENDPOINTS } from './config.js';
import { getCurrentPlayer } from './utils.js';
import { FALLBACK_DATA } from './data.js';
import { addLogMessage } from './handlers.js';

export async function fetchLatestPredictions() {
    try {
        const response = await fetch(`${CONFIG.API_BASE}/predictions/latest?limit=10`);
        if (response.ok) {
            const data = await response.json();
            if (data && data.length > 0) {
                addLogMessage(`Loaded ${data.length} latest predictions from DB`, 'success');
                return data;
            }
        }
    } catch (e) {
        console.warn('Failed to fetch predictions:', e);
    }
    return null;
}

export async function fetchRecentQuests() {
    try {
        const response = await fetch(`${CONFIG.API_BASE}${ENDPOINTS.QUESTS_RECENT}?limit=10`);
        if (response.ok) {
            const data = await response.json();
            if (data && data.length > 0) {
                addLogMessage(`Loaded ${data.length} recent quests from DB`, 'success');
                return data;
            }
        }
    } catch (e) {
        console.warn('Failed to fetch recent quests:', e);
    }
    return null;
}

export async function fetchPlayersStats() {
    try {
        const response = await fetch(`${CONFIG.API_BASE}${ENDPOINTS.PLAYERS_STATS}`);
        if (response.ok) {
            const data = await response.json();
            if (data && data.length > 0) {
                addLogMessage(`Loaded ${data.length} players from DB`, 'success');
                return data;
            }
        }
    } catch (e) {
        console.warn('Failed to fetch players stats:', e);
    }
    return null;
}

export async function checkMLHealth() {
    try {
        const response = await fetch(`${CONFIG.API_BASE}${ENDPOINTS.HEALTH}`);
        return response.ok;
    } catch {
        return false;
    }
}

export async function loadCandidates() {
    // Сначала пытаемся загрузить реальные предсказания из БД
    const predictions = await fetchLatestPredictions();
    if (predictions && predictions.length > 0) {
        const candidates = predictions.map((p, idx) => ({
            id: idx,
            type: p.type,
            target: p.target,
            amount: p.amount,
            reward: p.reward,
            mlScore: p.score
        }));
        addLogMessage(`Loaded ${candidates.length} real predictions from DB`, 'success');
        return candidates;
    }
    
    // Fallback на демо-данные
    addLogMessage('Using fallback candidates data', 'warning');
    return [...FALLBACK_DATA.candidates];
}

export async function loadRecentQuests() {
    const data = await fetchRecentQuests();
    if (data && data.length > 0) {
        return data;
    }
    addLogMessage('Using fallback quests data', 'warning');
    return [...FALLBACK_DATA.quests];
}

export async function loadPlayersStats() {
    const data = await fetchPlayersStats();
    if (data && data.length > 0) {
        return data;
    }
    addLogMessage('Using fallback players data', 'warning');
    return [...FALLBACK_DATA.players];
}

export async function updateSystemStatus() {
    const status = {
        ml_service: false,
        postgres: true,
        minecraft: true
    };
    
    try {
        const mlResponse = await fetch(`${CONFIG.API_BASE}${ENDPOINTS.HEALTH}`);
        status.ml_service = mlResponse.ok;
        if (status.ml_service) {
            addLogMessage('ML Service is healthy', 'success');
        } else {
            addLogMessage('ML Service is unavailable', 'warning');
        }
    } catch (e) {
        status.ml_service = false;
        addLogMessage('ML Service connection failed', 'warning');
    }
    
    return status;
}

export async function renderStatusBadges() {
    const container = document.getElementById('status-container');
    if (!container) return;
    
    const status = await updateSystemStatus();
    
    container.innerHTML = `
        <div class="status-cards">
            <div class="status-card-mini ${status.postgres ? 'online' : 'offline'}" id="status-postgres">
                <div class="status-icon">
                    <img src="assets/icons/postgresql-logo.svg" alt="PostgreSQL" class="status-icon-img">
                </div>
                <div class="status-info">
                    <span class="status-name">PostgreSQL</span>
                    <span class="status-value">${status.postgres ? 'работает' : 'ошибка'}</span>
                </div>
                <div class="status-led ${status.postgres ? 'green' : 'red'}"></div>
            </div>
            <div class="status-card-mini ${status.ml_service ? 'online' : 'offline'}" id="status-ml">
                <div class="status-icon">
                    <span class="status-icon-emoji">🤖</span>
                </div>
                <div class="status-info">
                    <span class="status-name">ML Service</span>
                    <span class="status-value">${status.ml_service ? 'работает' : 'ошибка'}</span>
                </div>
                <div class="status-led ${status.ml_service ? 'green' : 'red'}"></div>
            </div>
            <div class="status-card-mini ${status.minecraft ? 'online' : 'offline'}" id="status-minecraft">
                <div class="status-icon">
                    <img src="assets/icons/minecraft-logo.svg" alt="Minecraft" class="status-icon-img">
                </div>
                <div class="status-info">
                    <span class="status-name">Minecraft Server</span>
                    <span class="status-value">${status.minecraft ? 'работает' : 'ошибка'}</span>
                </div>
                <div class="status-led ${status.minecraft ? 'green' : 'red'}"></div>
            </div>
        </div>
    `;
}