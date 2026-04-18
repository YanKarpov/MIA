export const FALLBACK_DATA = {
    candidates: [
        { id: 0, type: 'Kill', target: 'Spider', amount: 3, reward: 60, mlScore: 0.91 },
        { id: 1, type: 'Break', target: 'Stone', amount: 10, reward: 40, mlScore: 0.45 },
        { id: 2, type: 'Collect', target: 'Carrot', amount: 15, reward: 45, mlScore: 0.12 },
        { id: 3, type: 'Kill', target: 'Zombie', amount: 5, reward: 50, mlScore: 0.73 },
        { id: 4, type: 'Break', target: 'Dirt', amount: 25, reward: 30, mlScore: 0.08 },
    ],
    quests: [
        { player: 'Alex_Player', quest: 'Kill Spider x3', score: 0.91, status: 'completed', time: '17:30:22' },
        { player: 'Alex_Player', quest: 'Break Stone x10', score: 0.45, status: 'completed', time: '17:28:15' },
        { player: 'MinerPro', quest: 'Kill Zombie x5', score: 0.73, status: 'active', time: '17:25:03' },
        { player: 'Builder123', quest: 'Break Dirt x25', score: 0.08, status: 'failed', time: '17:20:44' },
        { player: 'Alex_Player', quest: 'Collect Carrot x15', score: 0.12, status: 'failed', time: '17:15:30' },
    ],
    players: [
        { name: 'Alex_Player', kills: 47, deaths: 12, completed: 23, failed: 3, avgScore: 0.76 },
        { name: 'MinerPro', kills: 89, deaths: 24, completed: 41, failed: 7, avgScore: 0.82 },
        { name: 'Builder123', kills: 12, deaths: 8, completed: 18, failed: 2, avgScore: 0.68 },
    ],
    
    defaultCandidates: [
        { type: "Kill", target: "Spider", amount: 3, reward: 60 },
        { type: "Break", target: "Stone", amount: 10, reward: 40 },
        { type: "Collect", target: "Carrot", amount: 15, reward: 45 },
        { type: "Kill", target: "Zombie", amount: 5, reward: 50 },
        { type: "Break", target: "Dirt", amount: 25, reward: 30 }
    ],
    
    defaultPlayerStats: {
        deaths: 10,
        kills: 50,
        success_rate: 0.7
    },
    
    dbStats: {
        total_quests: 127,
        completed: 89,
        failed: 23,
        avg_ml_score: 0.76
    },
    
    statsSummary: {
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
    }
};