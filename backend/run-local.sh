#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

export SPRING_PROFILES_ACTIVE=local

if [[ ! -f local.env ]]; then
  echo "⚠️  backend/local.env not found."
  echo "   Copy local.env.example → local.env and add GOOGLE_* / GITHUB_* from Render."
  echo "   OAuth buttons will fail until then. Demo login: cinematic_maya / demo1234"
  echo ""
else
  if ! ./scripts/check-local-oauth.sh 2>/dev/null; then
    echo "⚠️  OAuth keys in local.env are invalid — Google sign-in will be blocked until fixed."
    echo ""
  fi
fi

exec ./mvnw spring-boot:run "$@"
