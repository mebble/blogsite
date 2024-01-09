CREATE TABLE IF NOT EXISTS users (
    id serial primary key,
    username text,
    password text
);

CREATE TABLE IF NOT EXISTS posts (
    id serial primary key,
    slug text,
    title text,
    description text,
    contents text,
    user_id integer,
    foreign key (user_id) references users(id)
);

CREATE TABLE IF NOT EXISTS comments (
    id serial primary key,
    contents text,
    user_id integer,
    post_id integer,
    foreign key (user_id) references users(id),
    foreign key (post_id) references posts(id)
);

-- https://github.com/luminus-framework/jdbc-ring-session
CREATE TABLE IF NOT EXISTS session_store (
  session_id VARCHAR(36) NOT NULL PRIMARY KEY,
  idle_timeout BIGINT,
  absolute_timeout BIGINT,
  value BYTEA
)
