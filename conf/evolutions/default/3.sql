# Update activity

# --- !Ups
ALTER TABLE activity ADD start_date_time datetime;
ALTER TABLE activity ADD end_date_time datetime;
ALTER TABLE activity ADD time_zone varchar(255);


# --- !Downs
ALTER TABLE activity DROP start_date_time;
ALTER TABLE activity DROP end_date_time;
ALTER TABLE activity DROP time_zone;
