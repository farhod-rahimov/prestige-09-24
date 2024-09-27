-- create tables
-- products

create table if not exists common_brand_names (
    id serial primary key,
    brand_name varchar(100) unique not null
);

create table if not exists common_product_names (
    id serial primary key,
    product_name varchar(200) unique not null
);

create table if not exists common_products (
	id serial primary key,
    brand_name_id int references common_brand_names(id) not null,
	product_name_id int references common_product_names(id) not null,
	car_model varchar(1000),
	article_number varchar(1000),
	notes varchar(1000)
);

create table if not exists sklad_store_names(
	id serial primary key,
	store_name varchar(200) unique not null
);

create table if not exists sklad_products (
	id serial primary key,
	product_id int references common_products(id) not null,
	stock_balance int default 0,
	price_won numeric(15, 4) default 0,
	price_dollar numeric(15, 4) default 0,
	margin_percentage int default 0,
	store_name_id int references sklad_store_names(id) not null,
	date_time timestamp not null
);

create table if not exists prestige_store_names (
	id serial primary key,
	store_name varchar(200) unique not null
);

create table if not exists prestige_products (
	id serial primary key,
	product_id int references common_products(id) not null,
	price_dollar numeric(15, 4) default 0,
	stock_balance int default 0,
	store_name_id int references prestige_store_names(id) not null,
	date_time timestamp not null
);

-- sales

-- sklad

create table if not exists sklad_clients (
	id serial primary key,
	name varchar(100) not null,
	phone_number varchar(500) /* несколько номеров с комменариями */
);

create table if not exists sklad_baskets (
	id serial primary key,
	created_date_time TIMESTAMP not null,
    last_changed_date_time TIMESTAMP not null,
	client_id int references sklad_clients not null
);

create table if not exists sklad_all_sales (
	product_id int references common_products(id) not null,
    quantity int not null,
    sold_price_dollar numeric(15, 4) not null,
	basket_id int references sklad_baskets(id) not null
);

-- prestige

create table if not exists prestige_clients (
	id serial primary key,
	name varchar(100) not null,
	phone_number varchar(500) /* несколько номеров с комменариями */
);

create table if not exists prestige_baskets (
	id serial primary key,
	created_date_time TIMESTAMP not null,
	last_changed_date_time TIMESTAMP not null,
	client_id int references prestige_clients not null
);

create table if not exists prestige_all_sales(
	product_id int references common_products(id) not null,
	quantity int not null,
	sold_price_smn numeric(15, 4) not null,
	basket_id int references prestige_baskets(id) not null
);

create table if not exists prestige_settings(
    settings_name varchar(100) not null,
    settings_value varchar(100) not null
);