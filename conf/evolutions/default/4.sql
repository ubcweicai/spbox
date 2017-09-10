# Update ticket_type

# --- !Ups
ALTER TABLE ticket_type ADD tax_included boolean DEFAULT FALSE;

create table article (
  id                        varchar(255) not null,
  name                      varchar(255),
  title                     varchar(255),
  content                   LONGTEXT,
  primary key (id)
);


# --- !Downs
ALTER TABLE ticket_type DROP tax_included;

drop table article;