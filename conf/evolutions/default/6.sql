# --- !Ups

create table image (
  id                        varchar(255) not null,
  content_type              varchar(255),
  data                      LONGBLOB,
  primary key (id)
);
# Add some predefined images


# --- !Downs

drop table image;