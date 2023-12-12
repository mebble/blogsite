-- TODO Remove after prototyping
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id integer primary key autoincrement,
    username text,
    password text
);

-- TODO Remove after prototyping
DROP TABLE IF EXISTS blogs;
CREATE TABLE blogs (
    slug text,
    title text,
    description text,
    contents text,
    user_id integer,
    foreign key(user_id) references users(id)
);

-- https://github.com/luminus-framework/jdbc-ring-session
-- TODO Remove after prototyping
DROP TABLE IF EXISTS session_store;
CREATE TABLE session_store (
    session_id       VARCHAR NOT NULL PRIMARY KEY,
    idle_timeout     INTEGER,
    absolute_timeout INTEGER,
    value            BLOB
);
