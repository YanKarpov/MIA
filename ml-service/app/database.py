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
        SELECT player_uuid, type, target, amount, ml_score, status, created_at 
        FROM quests 
        ORDER BY created_at DESC 
        LIMIT %s
    """, (limit,))
    rows = cur.fetchall()
    cur.close()
    conn.close()
    
    quests = []
    for row in rows:
        quests.append({
            "player": row[0],
            "quest": f"{row[1]} {row[2]} x{row[3]}",
            "score": float(row[4]) if row[4] else 0,
            "status": row[5] if row[5] else "pending",
            "time": row[6].strftime("%H:%M:%S") if row[6] else ""
        })
    return quests

def get_players_stats_from_db():
    conn = get_db_connection()
    cur = conn.cursor()
    cur.execute("""
        SELECT name, kills, deaths, completed_quests, failed_quests, avg_ml_score 
        FROM players_stats 
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
    total = cur.fetchone()[0]
    
    cur.execute("SELECT COUNT(*) FROM quests WHERE status = 'completed'")
    completed = cur.fetchone()[0]
    
    cur.execute("SELECT AVG(ml_score) FROM quests WHERE ml_score IS NOT NULL")
    avg_score = cur.fetchone()[0]
    
    cur.execute("""
        SELECT name, completed_quests 
        FROM players_stats 
        ORDER BY completed_quests DESC 
        LIMIT 1
    """)
    top = cur.fetchone()
    
    cur.execute("""
        SELECT name, kills 
        FROM players_stats 
        ORDER BY kills DESC 
        LIMIT 1
    """)
    active = cur.fetchone()
    
    cur.close()
    conn.close()
    
    return {
        "total": total,
        "completed": completed,
        "avg_score": avg_score,
        "top_player": top[0] if top else None,
        "most_active": active[0] if active else None
    }

def get_db_table_data(table_name: str, limit: int):
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
                val = val.isoformat()
            item[col] = val
        result.append(item)
    return result