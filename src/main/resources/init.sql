create database if not exists test;
create table forupdate (id INT primary key AUTO_INCREMENT,name varchar(50),version int default 0);
insert into forupdate (name) values ('testforupdate');

