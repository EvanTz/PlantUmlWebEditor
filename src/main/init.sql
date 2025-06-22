-- Create database
CREATE DATABASE plantuml;

-- Switch to the new database
\c plantuml;

-- Create tables
CREATE TABLE users (
id BIGSERIAL PRIMARY KEY,
username VARCHAR(20) UNIQUE NOT NULL,
email VARCHAR(50) UNIQUE NOT NULL,
password VARCHAR(120) NOT NULL,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
id BIGSERIAL PRIMARY KEY,
name VARCHAR(20) NOT NULL
);

CREATE TABLE user_roles (
user_id BIGINT NOT NULL,
role_id BIGINT NOT NULL,
PRIMARY KEY (user_id, role_id),
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE projects (
id BIGSERIAL PRIMARY KEY,
name VARCHAR(100) NOT NULL,
description VARCHAR(500),
content TEXT,
user_id BIGINT NOT NULL,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);





-- Insert initial roles
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');


-- Create indexes for better performancee
CREATE INDEX idx_projects_user_id ON projects(user_id);
CREATE INDEX idx_projects_created_at ON projects(created_at);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);







