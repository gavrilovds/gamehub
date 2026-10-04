#!/usr/bin/env sh
set -eu

api_url="${API_URL:-http://localhost:8080}"

curl -fsS -X POST "$api_url/api/players/batch" \
  -H 'Content-Type: application/json' \
  --data-binary @- <<'JSON'
{"players":[
  {"id":"1001","name":"Alice","level":10,"region":"EU"},
  {"id":"1002","name":"Bob","level":8,"region":"NA"},
  {"id":"1003","name":"Carol","level":12,"region":"ASIA"},
  {"id":"1004","name":"Dave","level":4,"region":"EU"},
  {"id":"1005","name":"Eve","level":14,"region":"NA"},
  {"id":"1006","name":"Frank","level":7,"region":"EU"},
  {"id":"1007","name":"Grace","level":9,"region":"ASIA"},
  {"id":"1008","name":"Heidi","level":3,"region":"EU"},
  {"id":"1009","name":"Ivan","level":11,"region":"NA"},
  {"id":"1010","name":"Judy","level":6,"region":"EU"},
  {"id":"1011","name":"Mallory","level":13,"region":"ASIA"},
  {"id":"1012","name":"Niaj","level":5,"region":"NA"},
  {"id":"1013","name":"Olivia","level":15,"region":"EU"},
  {"id":"1014","name":"Peggy","level":2,"region":"ASIA"},
  {"id":"1015","name":"Rupert","level":16,"region":"EU"},
  {"id":"1016","name":"Sybil","level":10,"region":"NA"},
  {"id":"1017","name":"Trent","level":8,"region":"EU"},
  {"id":"1018","name":"Victor","level":12,"region":"ASIA"},
  {"id":"1019","name":"Walter","level":7,"region":"NA"},
  {"id":"1020","name":"Zoe","level":9,"region":"EU"}
]}
JSON
printf '\n'

curl -fsS -X POST "$api_url/api/players/1001/login"
printf '\n'
curl -fsS -X POST "$api_url/api/leaderboard/score" \
  -H 'Content-Type: application/json' \
  -d '{"playerId":"1001","score":150}'
printf '\n'
curl -fsS -X POST "$api_url/api/leaderboard/score" \
  -H 'Content-Type: application/json' \
  -d '{"playerId":"1002","score":120}'
printf '\n'
curl -fsS -X POST "$api_url/api/players/1001/achievements" \
  -H 'Content-Type: application/json' \
  -d '{"achievementName":"FIRST_WIN"}'
printf '\n'
curl -fsS -X POST "$api_url/api/players/1002/achievements" \
  -H 'Content-Type: application/json' \
  -d '{"achievementName":"FIRST_WIN"}'
printf '\n'
curl -fsS -X PATCH "$api_url/api/players/1001/level" \
  -H 'Content-Type: application/json' \
  -d '{"delta":1}'
printf '\n'
curl -fsS "$api_url/api/players/1001"
printf '\nDemo data is ready. Run: python3 dz1_check.py\n'
