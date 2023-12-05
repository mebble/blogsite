-- TODO Remove after prototyping
DROP TABLE IF EXISTS blogs;
CREATE TABLE blogs (
    slug text,
    title text,
    description text,
    contents text
);

-- TODO Remove after prototyping
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    username text,
    password text
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
