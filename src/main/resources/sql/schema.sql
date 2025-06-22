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
	engine_model varchar(1000),
	oem_article_number varchar(1000),
	cross_article_number varchar(1000),
	notes TEXT
);

create table if not exists common_product_equivalents (
    id serial primary key,
    group_id serial not null,
    product_id int not null references common_products(id),
    CONSTRAINT uq_group_product UNIQUE (group_id, product_id)
);

create table if not exists sklad_suppliers (
	id serial primary key,
	name varchar(100),
	phone_number varchar(500) /* несколько номеров с комменариями */,
	store_name varchar(200) unique not null
);

create table if not exists sklad_products (
	id serial primary key,
	common_product_id int references common_products(id) not null,
	stock_balance int default 0,
	price_won numeric(15, 4) default 0,
	price_dollar numeric(15, 4) default 0,
	margin_percentage int default 0,
	supplier_id int references sklad_suppliers(id) not null,
	date_time timestamp not null
);

create table if not exists prestige_suppliers (
	id serial primary key,
	name varchar(100),
	phone_number varchar(500) /* несколько номеров с комменариями */,
	store_name varchar(200) unique not null
);

create table if not exists prestige_supply_baskets (
	id serial primary key,
	created_date_time TIMESTAMP not null,
	last_changed_date_time TIMESTAMP not null,
	supplier_id int references prestige_suppliers(id) not null,
	total_sum numeric(15, 4) not null,
    notes TEXT,
    status int not null
);

create table if not exists prestige_all_supplies (
	id serial primary key,
	common_product_id int references common_products(id) not null,
	quantity int default 0,
	price_dollar numeric(15, 4) default 0,
	basket_id int references prestige_supply_baskets(id) not null
);

-- sales

-- sklad

create table if not exists sklad_clients (
	id serial primary key,
	name varchar(100) not null,
	phone_number varchar(500) /* несколько номеров с комменариями */
);

create table if not exists sklad_sales_baskets (
	id serial primary key,
	created_date_time TIMESTAMP not null,
    last_changed_date_time TIMESTAMP not null,
	client_id int references sklad_clients not null,
	total_sum numeric(15, 4) not null,
    notes TEXT,
    status int not null
);

create table if not exists sklad_all_sales (
	id serial primary key,
	common_product_id int references common_products(id) not null,
    quantity int not null,
    sold_price_dollar numeric(15, 4) not null,
    wholesale_price numeric(15, 4) not null,
	basket_id int references sklad_sales_baskets(id) not null
);

create table if not exists sklad_settings (
	id serial primary key,
    key varchar(100) unique not null,
    value varchar(100) not null
);

-- prestige

create table if not exists prestige_clients (
	id serial primary key,
	name varchar(100) not null,
	phone_number varchar(500) /* несколько номеров с комменариями */
);

create table if not exists prestige_sales_baskets (
	id serial primary key,
	created_date_time TIMESTAMP not null,
	last_changed_date_time TIMESTAMP not null,
	client_id int references prestige_clients not null,
	total_sum numeric(15, 4) not null,
    notes TEXT,
    status int not null
);

create table if not exists prestige_all_sales (
	id serial primary key,
	common_product_id int references common_products(id) not null,
	quantity int not null,
	sold_price_smn numeric(15, 4) not null,
    wholesale_price numeric(15, 4) not null,
	basket_id int references prestige_sales_baskets(id) not null
);

create table if not exists prestige_settings (
	id serial primary key,
    key varchar(100) unique not null,
    value varchar(100) not null
);