# blogsite-clj

Scaffolded using `lein new app blogsite-clj`. Following the ring-clojure [Getting Started page](https://github.com/ring-clojure/ring/wiki/Getting-Started).

## Usage

FIXME: explanation

    $ java -jar blogsite-clj-0.1.0-standalone.jar [args]

## Development

Set environment variables:

```
export DEVELOPMENT=true
export DB_NAME=blogsite_dev
```

Set up development database:

```
createdb $DB_NAME
psql $DB_NAME
```

Run database migrations:

```
sh scripts/run-migrations.sh
```

Run:

```
lein run
```

Restart the server on file changes:

```
watchexec --watch src --restart lein run
```

## Architecture

### Code

- Model layer: sql and object schemas
- Service layer: platform-independent logic, monad stuff, etc
- Controller layer: validation, parsing/creating req&res, HTTP stuff

### Database

- DB records primary keys:
    - Using rowid without explicit [autoincrement](https://www.sqlite.org/autoinc.html)
