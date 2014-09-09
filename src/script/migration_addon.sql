update sessions set width='1' where length='';
alter table sessions add column type varchar(32);
update sessions set type='session'