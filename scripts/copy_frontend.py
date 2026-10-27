from pathlib import Path
from shutil import copytree


PROJECT_ROOT = Path(__file__).resolve().parents[1]
FRONTEND_BUILD = PROJECT_ROOT / "frontend" / "dist"
VERCEL_PUBLIC = PROJECT_ROOT / "public"

if not (FRONTEND_BUILD / "index.html").is_file():
    raise SystemExit("Vite build is missing frontend/dist/index.html")

copytree(FRONTEND_BUILD, VERCEL_PUBLIC, dirs_exist_ok=True)
print(f"Copied Vite output to {VERCEL_PUBLIC}")
