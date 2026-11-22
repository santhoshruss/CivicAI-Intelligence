import pymysql
import bcrypt

def update_passwords():
    # Generate fresh BCrypt hash for Password@123
    pw = b"Password@123"
    salt = bcrypt.gensalt(rounds=10, prefix=b"2a")
    hashed = bcrypt.hashpw(pw, salt).decode("utf-8")
    print(f"Generated fresh hash: {hashed}")
    assert bcrypt.checkpw(pw, hashed.encode("utf-8")), "Self check failed"

    conn = pymysql.connect(
        host="localhost",
        user="root",
        password="Sandy@118",
        database="civicai_db"
    )
    cur = conn.cursor()
    cur.execute("UPDATE users SET password_hash = %s", (hashed,))
    conn.commit()
    print(f"Updated {cur.rowcount} users successfully!")

    cur.execute("SELECT id, name, email, role, password_hash FROM users")
    rows = cur.fetchall()
    for r in rows:
        print(f"User #{r[0]}: {r[2]} ({r[3]}) -> Hash: {r[4]}")
    conn.close()

if __name__ == "__main__":
    update_passwords()
