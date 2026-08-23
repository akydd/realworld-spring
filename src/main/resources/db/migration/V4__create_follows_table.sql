create table if not exists follows
(
    follower_id  integer not null references users (id) on delete cascade,
    following_id integer not null references users (id) on delete cascade,
    primary key (follower_id, following_id)
);