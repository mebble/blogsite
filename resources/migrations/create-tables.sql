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
    name text,
    email text
);
-- https://github.com/luminus-framework/jdbc-ring-session
CREATE TABLE session_store (
    session_id       VARCHAR NOT NULL PRIMARY KEY,
    idle_timeout     INTEGER,
    absolute_timeout INTEGER,
    value            BLOB
);
