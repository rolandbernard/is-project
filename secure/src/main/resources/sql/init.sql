CREATE TABLE IF NOT EXISTS `user` (
    `id` integer PRIMARY KEY AUTOINCREMENT,
    `username` varchar(255) NOT NULL UNIQUE,
    `password` varchar(255) NOT NULL,
    `is_vendor` integer NOT NULL DEFAULT 0,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS `product` (
    `id` integer PRIMARY KEY AUTOINCREMENT,
    `name` varchar(255) NOT NULL,
    `price` integer NOT NULL,
    `user_id` integer NOT NULL,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
);
CREATE TABLE IF NOT EXISTS `order` (
    `id` integer PRIMARY KEY AUTOINCREMENT,
    `user_id` integer NOT NULL,
    `product_id` integer NOT NULL,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`)
);
CREATE TABLE IF NOT EXISTS `review` (
    `id` integer PRIMARY KEY AUTOINCREMENT,
    `user_id` integer NOT NULL,
    `product_id` integer NOT NULL,
    `rating` integer NOT NULL,
    `comment` text NOT NULL,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`),
    UNIQUE (`user_id`, `product_id`)
);
CREATE TABLE IF NOT EXISTS `response` (
    `id` integer PRIMARY KEY AUTOINCREMENT,
    `review_id` integer NOT NULL,
    `user_id` integer NOT NULL,
    `comment` text NOT NULL,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`review_id`) REFERENCES `review`(`id`)
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
);
CREATE TABLE IF NOT EXISTS `message` (
    `id` integer PRIMARY KEY AUTOINCREMENT,
    `sender_id` integer NOT NULL,
    `receiver_id` integer NOT NULL,
    `message` text NOT NULL,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`sender_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`receiver_id`) REFERENCES `user`(`id`)
);
