# --- !Ups

create table city (
  id                        varchar(255) not null,
  name                      varchar(255),
  country                   varchar(255),
  time_zone                 varchar(255),
  primary key (id)
);


# --- !Downs

drop table city;