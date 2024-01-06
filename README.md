# blogsite-clj

Scaffolded using `lein new app blogsite-clj`. Following the ring-clojure [Getting Started page](https://github.com/ring-clojure/ring/wiki/Getting-Started).

## Usage

FIXME: explanation

    $ java -jar blogsite-clj-0.1.0-standalone.jar [args]

## Development

Run database migrations:

```
sh scripts/run-migrations.sh
```

Set environment variables:

```
export DEVELOPMENT=true
```

Run:

```
lein run
```

Restart the server on file changes:

```
watchexec --watch src --restart lein run
```

Open development database:

```
sqlite3 blog.db
```

## Architecture

### Code

- Model layer: sql and object schemas
- Service layer: platform-independent logic, monad stuff, etc
- Controller layer: validation, parsing/creating req&res, HTTP stuff

### Database

- DB records primary keys:
    - Using rowid without explicit [autoincrement](https://www.sqlite.org/autoinc.html)
