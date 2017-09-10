# --- !Ups

ALTER TABLE user ADD verified tinyint(1) DEFAULT 0;


# --- !Downs

ALTER TABLE user drop verified;
