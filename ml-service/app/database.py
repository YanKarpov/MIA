import psycopg2
import os
from datetime import datetime

def get_db_connection():
    return psycopg2.connect(
        host=os.getenv('DB_HOST', 'db'),
        port=os.getenv('DB_PORT', '5432'),
        user=os.getenv('DB_USER', 'admin'),
        password=os.getenv('DB_PASSWORD', 'admin'),
        database=os.getenv('DB_NAME', 'quests')
    )

def get_recent_quests_from_db(limit: int):
    conn = get_db_connection()
    cur = conn.cursor()
    cur.execute("""
        SELECT q.player_id, q.type, q.target, q.amount, q.ml_score, q.status, q.issued_at, p.name
        FROM quests q
        LEFT JOIN players p ON q.player_id = p.id
        ORDER BY q.issued_at DESC 
        LIMIT %s
    """, (limit,))
    rows = cur.fetchall()
    cur.close()
    conn.close()
    
    quests = []
    for row in rows:
        ml_score = round(float(row[4]), 3) if row[4] else 0
        
        issued_at = row[6]
        formatted_time = ""
        if issued_at:
            if isinstance(issued_at, datetime):
                formatted_time = issued_at.strftime("%d.%m %H:%M")
            else:
                formatted_time = str(issued_at)[5:16].replace('T', ' ')
        
        quests.append({
            "player": row[7] if row[7] else f"Player_{row[0]}",
            "quest": f"{row[1]} {row[2]} x{row[3]}",
            "score": ml_score,
            "status": row[5] if row[5] else "pending",
            "time": formatted_time
        })
    return quests

def get_players_stats_from_db():
    conn = get_db_connection()
    cur = conn.cursor()
    
    cur.execute("""
        SELECT 
            name,
            total_kills,
            total_deaths,
            completed_quests,
            total_quests - completed_quests as failed_quests,
            ROUND(CAST(success_rate AS numeric), 3) as success_rate
        FROM players 
        ORDER BY completed_quests DESC
    """)
    rows = cur.fetchall()
    cur.close()
    conn.close()
    
    players = []
    for row in rows:
        players.append({
            "name": row[0],
            "kills": row[1] or 0,
            "deaths": row[2] or 0,
            "completed": row[3] or 0,
            "failed": row[4] or 0,
            "avgScore": float(row[5]) if row[5] else 0
        })
    return players

def get_stats_summary_from_db():
    conn = get_db_connection()
    cur = conn.cursor()
    
    cur.execute("SELECT COUNT(*) FROM quests")
    total_quests = cur.fetchone()[0]
    
    cur.execute("SELECT COUNT(*) FROM quests WHERE status = 'COMPLETED'")
    completed_quests = cur.fetchone()[0]
    
    cur.execute("SELECT ROUND(CAST(AVG(ml_score) AS numeric), 3) FROM quests WHERE ml_score IS NOT NULL")
    avg_ml_score = cur.fetchone()[0]
    
    cur.execute("""
        SELECT name, completed_quests 
        FROM players 
        ORDER BY completed_quests DESC 
        LIMIT 1
    """)
    top_player = cur.fetchone()
    
    cur.execute("""
        SELECT name, total_kills 
        FROM players 
        ORDER BY total_kills DESC 
        LIMIT 1
    """)
    most_active = cur.fetchone()
    
    cur.close()
    conn.close()
    
    return {
        "total": total_quests,
        "completed": completed_quests,
        "avg_score": float(avg_ml_score) if avg_ml_score else 0.76,
        "top_player": top_player[0] if top_player else None,
        "most_active": most_active[0] if most_active else None
    }

def get_db_table_data(table_name: str, limit: int):
    allowed = ["quests", "players", "ml_predictions"]
    if table_name not in allowed:
        return []
    
    conn = get_db_connection()
    cur = conn.cursor()
    cur.execute(f"SELECT * FROM {table_name} ORDER BY id DESC LIMIT %s", (limit,))
    rows = cur.fetchall()
    
    cur.execute(f"""
        SELECT column_name FROM information_schema.columns 
        WHERE table_name = %s ORDER BY ordinal_position
    """, (table_name,))
    columns = [row[0] for row in cur.fetchall()]
    cur.close()
    conn.close()
    
    result = []
    for row in rows:
        item = {}
        for i, col in enumerate(columns):
            val = row[i]
            if isinstance(val, datetime):
                val = val.strftime("%d.%m.%Y %H:%M:%S")
            elif isinstance(val, float):
                val = round(val, 3)
            elif val is None:
                val = "—"
            item[col] = val
        result.append(item)
    return result