create table users
(
    user_id       int          not null
        primary key,
    password_hash varchar(255) not null,
    email         varchar(255) not null,
    constraint email
        unique (email)
);

create table friends
(
    id         int auto_increment
        primary key,
    user_id    int                                       not null,
    friend_id  int                                       not null,
    created_at timestamp       default CURRENT_TIMESTAMP null,
    status     enum ('0', '1') default '1'               null comment '0 user cant send to friend',
    constraint unique_friend
        unique (user_id, friend_id),
    constraint friends_ibfk_1
        foreign key (user_id) references users (user_id),
    constraint friends_ibfk_2
        foreign key (friend_id) references users (user_id)
);

create index friend_id
    on friends (friend_id);

create table `groups`
(
    group_id   int auto_increment
        primary key,
    creator_id int                                 not null,
    created_at timestamp default CURRENT_TIMESTAMP null,
    constraint groups_ibfk_1
        foreign key (creator_id) references users (user_id)
);

create table chat_messages
(
    message_id    int auto_increment
        primary key,
    sender_id     int                                 not null,
    receiver_id   int                                 null,
    group_id      int                                 null,
    receiver_type enum ('user', 'group')              not null,
    send_time     timestamp default CURRENT_TIMESTAMP null,
    message_type  enum ('text', 'image', 'file')      not null,
    content       text                                null,
    file_path     varchar(255)                        null,
    constraint chat_messages_ibfk_1
        foreign key (sender_id) references users (user_id),
    constraint chat_messages_ibfk_2
        foreign key (receiver_id) references users (user_id),
    constraint chat_messages_ibfk_3
        foreign key (group_id) references `groups` (group_id)
);

create index group_id
    on chat_messages (group_id);

create index receiver_id
    on chat_messages (receiver_id);

create index sender_id
    on chat_messages (sender_id);

create table contact_apply
(
    apply_id     int auto_increment
        primary key,
    applicant_id int                                                                not null,
    contact_type enum ('friend', 'group')                                           not null,
    receiver_id  int                                                                null,
    group_id     int                                                                null,
    apply_time   timestamp                                default CURRENT_TIMESTAMP null,
    status       enum ('pending', 'approved', 'rejected') default 'pending'         null,
    message      text                                                               null,
    constraint contact_apply_ibfk_1
        foreign key (applicant_id) references users (user_id),
    constraint contact_apply_ibfk_2
        foreign key (receiver_id) references users (user_id),
    constraint contact_apply_ibfk_3
        foreign key (group_id) references `groups` (group_id)
            on delete set null
);

create index applicant_id
    on contact_apply (applicant_id);

create index receiver_id
    on contact_apply (receiver_id);

create table group_info
(
    group_id     int          not null
        primary key,
    group_name   varchar(100) not null,
    announcement text         null,
    avatar_path  varchar(255) null,
    constraint group_info_ibfk_1
        foreign key (group_id) references `groups` (group_id)
            on delete cascade
);

create table group_members
(
    id        int auto_increment
        primary key,
    group_id  int                                                not null,
    user_id   int                                                not null,
    joined_at timestamp                default CURRENT_TIMESTAMP null,
    role      enum ('member', 'owner') default 'member'          null,
    constraint unique_member
        unique (group_id, user_id),
    constraint group_members_ibfk_1
        foreign key (group_id) references `groups` (group_id),
    constraint group_members_ibfk_2
        foreign key (user_id) references users (user_id)
);

create index user_id
    on group_members (user_id);

create index creator_id
    on `groups` (creator_id);

create table user_profiles
(
    profile_id  int auto_increment
        primary key,
    user_id     int                         null,
    avatar_path varchar(255)                null,
    nickname    varchar(50)                 null,
    gender      enum ('male', 'female', '') null,
    region      varchar(100)                null,
    constraint unique_user_id
        unique (user_id),
    constraint user_profiles_ibfk_1
        foreign key (user_id) references users (user_id)
);


