drop database cmsdb;
create database cmsdb default character set=UTF8;
grant all on cmsdb.* to cms@localhost identified by 'cms';
drop table if exists cmsdb.record;
drop table if exists cmsdb.user;
create table cmsdb.record(
       id int(11) auto_increment primary key,
       parent_id int(11),
       is_leaf boolean default false,
       is_root boolean default false,
       is_locked boolean default false,
       locked_by int(11) default null,
       locked_at datetime default null,  
       name varchar(1024) not null, 
       body text,
       CONSTRAINT foreign key (locked_by) references cmsdb.user(id)
) engine=InnoDB character set=UTF8;

create table cmsdb.user(
       id int(11) auto_increment primary key,
       login varchar(255) not null,
       password varchar(255) not null
 ) engine=InnoDB character set=UTF8;

alter table cmsdb.record add column file_path varchar(255) default null;

create table cmsdb.parent_2_children(
       parent_id int(11) not null,
       child_id int(11) not null,
       CONSTRAINT foreign key(parent_id) references cmsdb.record(id),
       CONSTRAINT foreign key(child_id) references cmsdb.record(id)
)engine=InnoDB character set=UTF8;
