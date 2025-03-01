create table knowledge
(
    id       int auto_increment primary key,
    user_id  varchar(255) not null,
    category varchar(255),
    content  text
);