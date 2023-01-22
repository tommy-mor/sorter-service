curl 'http://localhost:8080' \
  -X POST \
  -H 'content-type: application/json' \
  --data '{
    "query": "{ continents { code name } }"
  }'
