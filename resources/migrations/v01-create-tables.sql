CREATE TABLE IF NOT EXISTS users (
    id integer primary key autoincrement,
    username text,
    password text
);

CREATE TABLE IF NOT EXISTS posts (
    id integer primary key autoincrement,
    slug text,
    title text,
    description text,
    contents text,
    user_id integer,
    foreign key(user_id) references users(id)
);

CREATE TABLE IF NOT EXISTS comments (
    id integer primary key autoincrement,
    contents text,
    user_id integer,
    post_id integer,
    foreign key(user_id) references users(id),
    foreign key(post_id) references posts(id)
);

-- https://github.com/luminus-framework/jdbc-ring-session
CREATE TABLE IF NOT EXISTS session_store (
    session_id       VARCHAR NOT NULL PRIMARY KEY,
    idle_timeout     INTEGER,
    absolute_timeout INTEGER,
    value            BLOB
);
