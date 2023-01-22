curl 'http://localhost:8080/graphql' -v\
  -X POST \
  -H 'content-type: application/json' \
  --data '{
    "query": "{ continents { code name } }"
  }'

curl 'http://localhost:8080/graphql' -v\
  -X OPTIONS
