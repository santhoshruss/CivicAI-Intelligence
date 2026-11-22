"""
CivicAI Database Setup Script (Python / MySQL CLI)
Reads connection settings from ../.env or prompts the user.
"""
import os
import sys
import subprocess
from pathlib import Path

def load_env(env_path):
    env_vars = {}
    if os.path.exists(env_path):
        with open(env_path, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith('#') and '=' in line:
                    k, v = line.split('=', 1)
                    env_vars[k.strip()] = v.strip()
    return env_vars

def main():
    base_dir = Path(__file__).resolve().parent
    env_file = base_dir.parent / '.env'
    env_vars = load_env(env_file)

    db_user = env_vars.get('DB_USERNAME', 'root')
    db_pass = env_vars.get('DB_PASSWORD', '')

    if not db_pass or db_pass == 'your_mysql_password_here':
        print("[!] DB_PASSWORD not found in .env or set to default placeholder.")
        db_pass = input(f"Enter MySQL password for user '{db_user}': ").strip()

    mysql_paths = [
        r"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
        r"C:\Program Files\MySQL\MySQL Workbench 8.0\mysql.exe",
        "mysql"
    ]

    mysql_exe = None
    for p in mysql_paths:
        if os.path.exists(p) or p == "mysql":
            mysql_exe = p
            break

    if not mysql_exe:
        print("[-] Could not find mysql.exe. Please execute schema.sql and seed.sql via MySQL Workbench.")
        return

    schema_sql = base_dir / 'schema.sql'
    seed_sql = base_dir / 'seed.sql'

    print(f"[*] Executing schema.sql with {mysql_exe}...")
    cmd_schema = [mysql_exe, f"-u{db_user}", f"-p{db_pass}", "-e", f"source {schema_sql.as_posix()}"]
    ret_schema = subprocess.run(cmd_schema, capture_output=True, text=True)
    if ret_schema.returncode != 0:
        print(f"[-] Error executing schema.sql: {ret_schema.stderr}")
        return
    print("[+] Schema created successfully!")

    print(f"[*] Executing seed.sql...")
    cmd_seed = [mysql_exe, f"-u{db_user}", f"-p{db_pass}", "-e", f"source {seed_sql.as_posix()}"]
    ret_seed = subprocess.run(cmd_seed, capture_output=True, text=True)
    if ret_seed.returncode != 0:
        print(f"[-] Error executing seed.sql: {ret_seed.stderr}")
        return
    print("[+] Seed data populated successfully!")
    print("[✓] CivicAI Database is ready.")

if __name__ == '__main__':
    main()
