-- TODO Remove after prototyping
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id integer primary key autoincrement,
    username text,
    password text
);

-- TODO Remove after prototyping
DROP TABLE IF EXISTS posts;
CREATE TABLE posts (
    id integer primary key autoincrement,
    slug text,
    title text,
    description text,
    contents text,
    user_id integer,
    foreign key(user_id) references users(id)
);

-- TODO Remove after prototyping
DROP TABLE IF EXISTS comments;
CREATE TABLE comments (
    id integer primary key autoincrement,
    contents text,
    user_id integer,
    post_id integer,
    foreign key(user_id) references users(id),
    foreign key(post_id) references posts(id)
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
