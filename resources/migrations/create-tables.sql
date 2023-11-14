DROP TABLE IF EXISTS blogs;
CREATE TABLE blogs (
    slug text,
    title text,
    description text,
    contents text
);
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    name text,
    email text
);
