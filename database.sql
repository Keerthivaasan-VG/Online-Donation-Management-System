-- Run this file once in MySQL:  source database.sql;
CREATE DATABASE IF NOT EXISTS donation_db;
USE donation_db;

-- Table 1: campaigns (causes that people can donate to)
CREATE TABLE IF NOT EXISTS campaigns (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    description   VARCHAR(255),
    target_amount INT NOT NULL
);

-- Table 2: donations (one row for every donation made)
CREATE TABLE IF NOT EXISTS donations (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    donor_name    VARCHAR(100) NOT NULL,
    donor_email   VARCHAR(100) NOT NULL,
    campaign_id   INT NOT NULL,
    amount        INT NOT NULL,
    method        VARCHAR(30) NOT NULL,
    donated_on    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id)
);

-- Some starting campaigns
INSERT INTO campaigns (name, description, target_amount) VALUES
('Education for All',  'Books and fees for poor students',   100000),
('Food for the Needy', 'Meals for homeless families',          50000),
('Flood Relief Fund',  'Help for flood affected villages',    200000);
