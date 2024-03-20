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


create table if not exists `data_sync_job` (
    id int not null primary key auto_increment,
    name varchar(255) not null,
    cron varchar(1024) not null,
    source_data_source_id int not null,
    target_data_source_id int not null,
    source_schema varchar(255) not null,
    source_table varchar(255) not null,
    target_schema varchar(255) not null,
    target_table varchar(255) not null,
    order_by_column varchar(255) not null,
    running tinyint(1) not null default 0,
    last_run_time datetime null,
    last_run_success tinyint(1) not null default 0,
    create_time datetime not null default now(),
    update_time datetime not null default now()
);