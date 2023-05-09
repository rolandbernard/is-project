CREATE TABLE IF NOT EXISTS `user` (
    `id` integer PRIMARY KEY AUTOINCREMENT,
    `username` varchar(255) NOT NULL UNIQUE,
    `password` varchar(255) NOT NULL,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS `product` (
    `id` integer PRIMARY KEY AUTOINCREMENT,
    `name` varchar(255) NOT NULL,
    `price` int(11) NOT NULL,
    `user_id` int(11) NOT NULL,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS `order` (
    `id` integer PRIMARY KEY AUTOINCREMENT,
    `user_id` int(11) NOT NULL,
    `product_id` int(11) NOT NULL,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS `review` (
    `id` integer PRIMARY KEY AUTOINCREMENT,
    `user_id` int(11) NOT NULL,
    `product_id` int(11) NOT NULL,
    `rating` int(11) NOT NULL,
    `comment` varchar(255) NOT NULL,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS `response` (
    `id` integer PRIMARY KEY AUTOINCREMENT,
    `review_id` int(11) NOT NULL,
    `user_id` int(11) NOT NULL,
    `comment` varchar(255) NOT NULL,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
);
