ls ./resources/migrations | xargs -I {} psql -d blogsite_dev -f ./resources/migrations/{}
