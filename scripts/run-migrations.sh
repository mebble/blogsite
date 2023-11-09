ls ./resources/migrations | xargs -I {} sqlite3 blog.db -init ./resources/migrations/{}
