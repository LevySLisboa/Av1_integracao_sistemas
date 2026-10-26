#!/usr/bin/env bash
# Demonstra o fluxo completo: CSRF -> login (senha) -> 2FA (código do email) -> endpoints protegidos -> logout.
# Uso:  BASE=https://api-kjea.onrender.com ./docs/exemplos-curl.sh email@exemplo.com 'SuaSenha#123'
# Local: BASE=http://localhost:8080 (suba a API com COOKIE_SECURE=false)
set -euo pipefail

BASE="${BASE:-http://localhost:8080}"
EMAIL="${1:?informe o email}"
SENHA="${2:?informe a senha}"
JAR="$(mktemp)"
trap 'rm -f "$JAR"' EXIT

curl_api() { curl -s -c "$JAR" -b "$JAR" "$@"; echo; }
xsrf()     { awk '$6=="XSRF-TOKEN"{print $7}' "$JAR" | tail -n1; }

echo "1) Obtendo token CSRF"
curl_api "$BASE/auth/csrf"

echo "2) Login (etapa 1: senha) -> o servidor envia o código por email"
curl_api -X POST "$BASE/auth/login" \
  -H "Content-Type: application/json" -H "X-XSRF-TOKEN: $(xsrf)" \
  -d "{\"email\":\"$EMAIL\",\"senha\":\"$SENHA\"}"

read -r -p "Digite o código de 6 dígitos recebido por email: " CODIGO

echo "3) Verificação do 2º fator (etapa 2)"
curl_api -X POST "$BASE/auth/verificar-2fa" \
  -H "Content-Type: application/json" -H "X-XSRF-TOKEN: $(xsrf)" \
  -d "{\"codigo\":\"$CODIGO\"}"

echo "4) Quem sou eu?";            curl_api "$BASE/auth/me"
echo "5) Provas visíveis p/ mim";  curl_api "$BASE/provas"
echo "6) Alunos";                  curl_api "$BASE/alunos"

echo "7) Logout"
curl_api -X POST "$BASE/auth/logout" -H "X-XSRF-TOKEN: $(xsrf)"

echo "8) Após logout, o acesso deve dar 401:"
curl_api -w "HTTP %{http_code}\n" "$BASE/provas"
