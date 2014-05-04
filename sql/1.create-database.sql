drop database cmsdb if exists;
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
       foreign key (locked_by) references cmsdb.user(id)
) engine=InnoDB character set=UTF8;

create table cmsdb.user(
       id int(11) auto_increment primary key,
       login varchar(255) not null,
       password varchar(255) not null
 ) engine=InnoDB character set=UTF8;

insert 
