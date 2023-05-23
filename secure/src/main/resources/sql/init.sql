CREATE TABLE IF NOT EXISTS `user` (
    `id` char(36) PRIMARY KEY,
    `username` varchar(255) NOT NULL UNIQUE,
    `is_vendor` integer NOT NULL DEFAULT 0,
    `password` blob NOT NULL,
    `salt` blob NOT NULL,
    `private_key` blob NOT NULL,
    `public_key` blob NOT NULL,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS `product` (
    `id` char(36) PRIMARY KEY,
    `name` varchar(255) NOT NULL,
    `price` integer NOT NULL,
    `user_id` char(36) NOT NULL,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
);
CREATE TABLE IF NOT EXISTS `order` (
    `id` char(36) PRIMARY KEY,
    `user_id` integer NOT NULL,
    `product_id` char(36) NOT NULL,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `signature` blob NOT NULL,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`)
);
CREATE TABLE IF NOT EXISTS `review` (
    `id` char(36) PRIMARY KEY,
    `user_id` char(36) NOT NULL,
    `product_id` char(36) NOT NULL,
    `rating` char(36) NOT NULL,
    `comment` text NOT NULL,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`product_id`) REFERENCES `product`(`id`),
    UNIQUE (`user_id`, `product_id`)
);
CREATE TABLE IF NOT EXISTS `response` (
    `id` char(36) PRIMARY KEY,
    `review_id` char(36) NOT NULL,
    `user_id` char(36) NOT NULL,
    `comment` text NOT NULL,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`review_id`) REFERENCES `review`(`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
);
CREATE TABLE IF NOT EXISTS `chat` (
    `id` char(36) PRIMARY KEY,
    `sender_id` char(36) NOT NULL,
    `receiver_id` char(36) NOT NULL,
    `sender_key` text NOT NULL,
    `receiver_key` text NOT NULL
);
CREATE TABLE IF NOT EXISTS `message` (
    `id` char(36) PRIMARY KEY,
    `sender_id` char(36) NOT NULL,
    `receiver_id` char(36) NOT NULL,
    `message` text NOT NULL,
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`sender_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`receiver_id`) REFERENCES `user`(`id`)
);
