# Update activity and user
 
# --- !Ups
ALTER TABLE activity ADD create_date_time datetime;
ALTER TABLE activity MODIFY image LONGTEXT;
ALTER TABLE activity ADD description_image LONGTEXT;
ALTER TABLE activity ADD star tinyint(1) default 0;
ALTER TABLE activity ADD bname tinyint(1) default 0;
ALTER TABLE activity ADD btel tinyint(1) default 0;
ALTER TABLE activity ADD bgender tinyint(1) default 0;
ALTER TABLE activity ADD bage tinyint(1) default 0;
ALTER TABLE activity ADD baddress tinyint(1) default 0;

ALTER TABLE user ADD tel varchar(255) default "";
ALTER TABLE user ADD gender varchar(255) default "";
ALTER TABLE user ADD age integer default 0;
ALTER TABLE user ADD address varchar(255) default "";
 
# --- !Downs
ALTER TABLE activity DROP create_date_time;
ALTER TABLE activity DROP description_image;
ALTER TABLE activity DROP star;
ALTER TABLE activity DROP bname;
ALTER TABLE activity DROP btel;
ALTER TABLE activity DROP bgender;
ALTER TABLE activity DROP bage;
ALTER TABLE activity DROP baddress;


ALTER TABLE user DROP tel;
ALTER TABLE user DROP gender;
ALTER TABLE user DROP age;
ALTER TABLE user DROP address;