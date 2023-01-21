ALTER TABLE users ADD created_at TIMESTAMP NOT NULL DEFAULT NOW();
--;;
CREATE TABLE groups (
	   id SERIAL PRIMARY KEY,
	   user_id varchar(20) REFERENCES users(user_name) NOT NULL ON DELETE CASCADE,
	   title TEXT NOT NULL,
	   description TEXT NOT NULL,
)
--;;
CREATE TABLE roles (
	   id SERIAL PRIMARY KEY,
	   group_id SERIAL REFERENCES groups(id) NOT NULL,
	   title TEXT NOT NULL,

	   color TEXT NOT NULL,
	   UNIQUE (group_id, title)
)
--;;
CREATE TABLE users_in_groups (
	   user_id varchar(20) REFERENCES users(id) NOT NULL,
	   group_id SERIAL REFERENCES groups(id) NOT NULL,
	   role_id SERIAL REFERENCES roles(id)

	   PRIMARY KEY (user_id, group_id, role_id)
)

