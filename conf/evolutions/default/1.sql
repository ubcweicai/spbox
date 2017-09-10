# --- !Ups

create table activity (
  id                        varchar(255) not null,
  name                      varchar(255),
  folder                    varchar(255),
  image                     varchar(255),
  brief_description         LONGTEXT,
  description               LONGTEXT,
  closed                    tinyint(1) default 0,
  deleted                   tinyint(1) default 0,
  date_time                 varchar(255),
  venue                     varchar(255),
  organizer                 varchar(255),
  constraint pk_activity primary key (id))
;

create table setting (
  name                      varchar(255) not null,
  value                     varchar(255),
  constraint pk_setting primary key (name))
;

create table ticket (
  id                        varchar(255) not null,
  purchase_date_time        datetime,
  title                     varchar(255),
  date_time                 varchar(255),
  venue                     varchar(255),
  type_name                 varchar(255),
  price                     double,
  seat                      varchar(255),
  checked                   tinyint(1) default 0,
  checked_time              datetime,
  checked_by                varchar(255),
  bundle_id                 varchar(255),
  paid                      tinyint(1) default 0,
  payment_failure_handled   tinyint(1) default 0,
  deleted                   tinyint(1) default 0,
  qrcode                    LONGTEXT,
  assigned_to_email         varchar(255),
  folder                    varchar(255),
  activity_id               varchar(255),
  constraint pk_ticket primary key (id))
;

create table ticket_type (
  id                        varchar(255) not null,
  activity_id               varchar(255),
  type_name                 varchar(255),
  quantity                  integer,
  booked                    integer,
  price                     double,
  currency                  varchar(255),
  seat                      varchar(255),
  deleted                   tinyint(1) default 0,
  constraint pk_ticket_type primary key (id))
;

create table user (
  email                     varchar(255) not null,
  name                      varchar(255),
  password                  varchar(255),
  role                      varchar(255),
  constraint pk_user primary key (email))
;

alter table ticket add constraint fk_ticket_assigned_to_1 foreign key (assigned_to_email) references user (email) on delete restrict on update restrict;
create index ix_ticket_assigned_to_1 on ticket (assigned_to_email);
alter table ticket add constraint fk_ticket_activity_2 foreign key (activity_id) references activity (id) on delete restrict on update restrict;
create index ix_ticket_activity_2 on ticket (activity_id);
alter table ticket_type add constraint fk_ticket_type_activity_3 foreign key (activity_id) references activity (id) on delete restrict on update restrict;
create index ix_ticket_type_activity_3 on ticket_type (activity_id);



# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table activity;

drop table setting;

drop table ticket;

drop table ticket_type;

drop table user;

SET FOREIGN_KEY_CHECKS=1;
