# --- !Ups

alter table activity drop closed;

alter table activity add status varchar(255) DEFAULT "publish";


# --- !Downs

alter table activity drop status;
