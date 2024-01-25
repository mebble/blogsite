ls ./resources/migrations | xargs -t -I {} psql -d $DB_NAME -f ./resources/migrations/{}
