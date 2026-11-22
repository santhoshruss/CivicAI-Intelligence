import os
import pymysql

# Read .env
env_vars = {}
env_path = os.path.join(os.path.dirname(__file__), "..", ".env")
with open(env_path, "r") as f:
    for line in f:
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            k, v = line.split("=", 1)
            env_vars[k.strip()] = v.strip()

conn = pymysql.connect(
    host=env_vars.get("DB_HOST", "localhost"),
    user=env_vars.get("DB_USERNAME", "root"),
    password=env_vars.get("DB_PASSWORD", "root"),
    database=env_vars.get("DB_NAME", "civicai_db")
)

cols_to_add = [
    ("incidents", "original_language", "VARCHAR(20) DEFAULT 'English'"),
    ("incidents", "normalized_description", "TEXT NULL"),
    ("incident_analysis", "detected_objects", "JSON NULL"),
    ("incident_analysis", "model_version", "VARCHAR(100) NULL"),
    ("incident_analysis", "is_heuristic", "BOOLEAN DEFAULT FALSE"),
    ("duplicate_incidents", "similarity_level", "VARCHAR(30) DEFAULT 'MEDIUM_SIMILARITY'"),
    ("duplicate_incidents", "explanation", "TEXT NULL"),
    ("maintenance_documents", "ocr_extracted", "BOOLEAN DEFAULT FALSE"),
    ("maintenance_documents", "document_metadata", "JSON NULL"),
    ("agent_runs", "model_versions", "JSON NULL"),
]

with conn.cursor() as cur:
    for table, col, col_type in cols_to_add:
        cur.execute(f"""
            SELECT COUNT(*) FROM information_schema.columns 
            WHERE table_schema = DATABASE() AND table_name = '{table}' AND column_name = '{col}'
        """)
        exists = cur.fetchone()[0] > 0
        if not exists:
            cur.execute(f"ALTER TABLE {table} ADD COLUMN {col} {col_type}")
            print(f"Added column {col} to table {table}")
        else:
            print(f"Column {col} already exists in table {table}")

    # Check and add index
    cur.execute("""
        SELECT COUNT(*) FROM information_schema.statistics 
        WHERE table_schema = DATABASE() AND table_name = 'incidents' AND index_name = 'idx_incidents_cat_loc'
    """)
    idx_exists = cur.fetchone()[0] > 0
    if not idx_exists:
        cur.execute("CREATE INDEX idx_incidents_cat_loc ON incidents (category, latitude, longitude)")
        print("Created index idx_incidents_cat_loc on incidents")
    else:
        print("Index idx_incidents_cat_loc already exists")

conn.commit()
conn.close()
print("Database schema migration v2 applied successfully!")
