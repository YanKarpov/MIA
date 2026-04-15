import { CONFIG, ENDPOINTS } from './config.js';
import { getCurrentPlayer } from './utils.js';
import { FALLBACK_DATA } from './data.js';
import { addLogMessage } from './handlers.js';

export async function fetchRankingFromML() {
    try {
        const response = await fetch(`${CONFIG.API_BASE}${ENDPOINTS.RANK}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 
                player_uuid: getCurrentPlayer(),
                timestamp: new Date().toISOString()
            })
        });
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        
        const data = await response.json();
        addLogMessage('ML Service responded successfully', 'success');
        return data;
    } catch (error) {
        console.warn('ML Service unavailable:', error);
        addLogMessage(`ML Service error: ${error.message}, using fallback`, 'warning');
        return null;
    }
}

export async function fetchRecentQuests() {
    try {
        const response = await fetch(`${CONFIG.API_BASE}${ENDPOINTS.QUESTS_RECENT}?limit=10`);
        if (response.ok) {
            return await response.json();
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
            return await response.json();
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
    if (CONFIG.USE_REAL_API) {
        const data = await fetchRankingFromML();
        if (data && data.candidates) {
            return data.candidates.map(c => ({
                id: c.id,
                type: c.type,
                target: c.target,
                amount: c.amount,
                reward: c.reward,
                mlScore: c.ml_score || c.mlScore
            }));
        }
    }
    return [...FALLBACK_DATA.candidates];
}

export async function loadRecentQuests() {
    if (CONFIG.USE_REAL_API) {
        const data = await fetchRecentQuests();
        if (data) return data;
    }
    return [...FALLBACK_DATA.quests];
}

export async function loadPlayersStats() {
    if (CONFIG.USE_REAL_API) {
        const data = await fetchPlayersStats();
        if (data) return data;
    }
    return [...FALLBACK_DATA.players];
}

export async function updateSystemStatus() {
    const status = {
        ml_service: false,
        postgres: true,      // ← ЗАГЛУШКА: всегда работает
        minecraft: true      // ← ЗАГЛУШКА: всегда работает
    };
    
    // Реальная проверка только для ML сервера
    try {
        const mlResponse = await fetch(`${CONFIG.API_BASE}${ENDPOINTS.HEALTH}`);
        status.ml_service = mlResponse.ok;
    } catch (e) {
        status.ml_service = false;
    }
    
    return status;
}

// export async function updateSystemStatus() {
//     const status = {
//         ml_service: false,
//         postgres: false,
//         minecraft: false
//     };
    
//     // Реальная проверка ML сервера
//     try {
//         const mlResponse = await fetch(`${CONFIG.API_BASE}/health`);
//         status.ml_service = mlResponse.ok;
//     } catch (e) {
//         status.ml_service = false;
//     }
    
//     // Проверка PostgreSQL (через ML сервис)
//     try {
//         const dbResponse = await fetch(`${CONFIG.API_BASE}/db/health`);
//         status.postgres = dbResponse.ok;
//     } catch (e) {
//         status.postgres = false;
//     }
    
//     // Проверка Minecraft (через ML сервис или прямой запрос)
//     try {
//         const mcResponse = await fetch(`${CONFIG.API_BASE}/minecraft/status`);
//         status.minecraft = mcResponse.ok;
//     } catch (e) {
//         status.minecraft = false;
//     }
    
//     return status;
// }

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