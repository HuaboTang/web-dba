create schema if not exists `web_dba` collate utf8mb4_general_ci;

use web_dba;

create table if not exists datasource (
    id int not null primary key auto_increment,
    name varchar(255) not null,
    url varchar(1024) not null,
    username varchar(255) not null,
    password varchar(255) not null,
    create_time datetime not null default now()
);

alter table web_dba.datasource add column type char(10) not null;