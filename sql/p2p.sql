drop database if exists `p2p`;
create database `p2p` default character set utf8mb4 collate utf8mb4_general_ci;
use p2p;

drop table if exists `user`;
create table `user` (
	`user_id` varchar(12) not null comment '�û�id�� ����',
	`password` varchar(64) not null comment '����',
	`phone` varchar(11) not null comment '�ֻ���',
	`id_card` varchar(20) not null comment '���֤',
	`third_party_id` varchar(12) not null comment '������ƽ̨�˺�',
	`name` varchar(64) not null comment '����',
	`address` varchar(255) not null comment '��ַ',
	primary key(`user_id`)
) engine=InnoDB default charset=utf8mb4 comment '�û���Ϣ��';

drop table if exists `credit_info`;
create table `credit_id` (
	`user_id` varchar(12) not null,
	`income` decimal(10,2) not null comment '����',
	`famiy_income` decimal(10,2) not null comment '��ͥ����',
	`assets` decimal(12,2) not null comment '�����ʲ�',
	`family_number` int(2) not null comment '��ͥ��Ա��',
	`debt` decimal(12,2) not null comment 'ծ��',
	`credit_score` int(3) not null comment '���û���',
	primary key(`user_id`)
-- 	foreign key(`user_id`) references `user`(`user_id`)
) engine=InnoDB default charset=utf8mb4 comment '�û����ű�';

drop table if exists `product`;
create table `product` (
	`product_id` int(12) not null auto_increment,
	`borrower_id` varchar(12) not null comment '������id',
	`state` int(1) not null comment '��Ʒ״̬',
	`amount`decimal(12, 2) not null comment '������',
	`interest_rate` decimal(5, 4) not null comment '����',
	`repay_deadline` date not null comment '��������',
	`purchase_deadline` date not null comment '�Ϲ�����',
	`repay_data` int(2) not null comment 'ÿ�»���ʱ��',
	`create_time` timestamp not null default current_timestamp comment '����ʱ��',
	`update_time` timestamp not null default current_timestamp on update current_timestamp comment '�޸�ʱ��',
	primary key (`product_id`)
-- 	foreign key (`borrower_id`) references `user`(`user_id`)
) engine=InnoDB default charset=utf8mb4 comment '��Ʒ��Ϣ��';

drop table if exists `purchase`;
create table `purchase` (
	`product_id` int(12) not null,
	`investor_id` varchar(12) not null comment 'Ͷ����id',
	`purchase_time` date not null comment '�Ϲ�ʱ��',
	`amount` decimal(12, 2) not null comment '���',
	`state` int(1) not null comment '״̬�� 0��ʾ��Լ�У�1��ʾ����',
	`repay_time` date not null comment '��������',
	primary key (`product_id`)
-- 	foreign key (`product_id`) references `product`(`product_id`),
-- 	foreign key (`investor_id`) references `user`(`user_id`)
) engine=InnoDB default charset=utf8mb4 comment '�Ϲ���Ϣ��';

drop table if exists `water_bill`;
create table `water_bill` (
	`water_bill_id` varchar(32) not null,
	`payee_id` varchar(12) not null comment '�տ���id',
	`payer_id` varchar(12) not null comment '֧����id',
	`amount` decimal(12, 2) not null comment '���',
	`mode` int(1) not null comment 'ģʽ��0Ϊ��� 1Ϊ����',
	`time` timestamp not null default current_timestamp comment '����ʱ��',
	primary key(`water_bill_id`)
-- 	foreign key(`payee_id`) references `user`(`user_id`),
-- 	foreign key(`payer_id`) references `user`(`user_id`)
) engine=InnoDB default charset=utf8mb4 comment '��ˮ��';