# --- !Ups

ALTER TABLE article ADD type int(1) DEFAULT 0;


# --- !Downs

ALTER TABLE article drop type;
