#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

ENV_FILE="local.env"
if [[ ! -f "$ENV_FILE" ]]; then
  echo "❌ Missing $ENV_FILE — run: cp local.env.example local.env"
  exit 1
fi

fail=0
check() {
  local key="$1"
  local val
  val="$(grep -E "^${key}=" "$ENV_FILE" | head -1 | cut -d= -f2- | tr -d '\r' | sed 's/^["'\'']//;s/["'\'']$//')"
  if [[ -z "$val" ]]; then
    echo "❌ $key is empty"
    fail=1
    return
  fi
  local lower
  lower="$(echo "$val" | tr '[:upper:]' '[:lower:]')"
  if [[ "$lower" == your-* ]] || [[ "$lower" == *placeholder* ]] || [[ "$lower" == *example* ]]; then
    echo "❌ $key is still a placeholder ($val)"
    fail=1
    return
  fi
  if [[ "$key" == "GOOGLE_CLIENT_ID" ]] && [[ "$val" != *.apps.googleusercontent.com ]]; then
    echo "❌ $key must end with .apps.googleusercontent.com (got: ${val:0:20}...)"
    fail=1
    return
  fi
  echo "✅ $key looks valid"
}

check GOOGLE_CLIENT_ID
check GOOGLE_CLIENT_SECRET
check GITHUB_CLIENT_ID
check GITHUB_CLIENT_SECRET

if [[ "$fail" -ne 0 ]]; then
  echo ""
  echo "Fix: Render Dashboard → hivi backend service → Environment → copy the four OAuth variables into backend/local.env"
  echo "Then restart: ./run-local.sh"
  exit 1
fi

echo ""
if command -v curl >/dev/null 2>&1; then
  echo "Runtime OAuth registration (from running backend):"
  curl -sf "http://localhost:8080/oauth/status" 2>/dev/null | python3 -c "
import json,sys
try:
  d=json.load(sys.stdin)
  g=d.get('googleRegistration') or {}
  print('  Google clientIdPrefix:', g.get('clientIdPrefix','(backend not up)'))
  print('  Google redirectUri:   ', g.get('redirectUri', d.get('googleRedirectUri')))
  for issue in d.get('issues') or []:
    if 'redirect_uri' in issue.lower() or 'Client ID' in issue:
      print('  ⚠️ ', issue)
except Exception as e:
  print('  (start backend with ./run-local.sh first)')
" 2>/dev/null || echo "  (backend not reachable on :8080)"
fi
echo ""
echo "OAuth env looks good. Restart backend if it is already running."
