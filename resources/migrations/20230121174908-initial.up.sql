ALTER TABLE users ADD created_at TIMESTAMP NOT NULL DEFAULT NOW();
--;;

-- dokuwiki: none, read, edit, create, upload, delete
-- my drawing. can_view, can_edit

-- each resource has a single access record, by default going to 0, which is private to only owner.
-- these are acitions that that each resource has control over.
-- for more complex actions (can_vote, can_suggest,
-- can_add) or that action has its own row in the table
 --       (default defers to edit permission of resource)


CREATE TABLE access (
  id SERIAL PRIMARY KEY,

  inherit BOOLEAN NOT NULL, -- if inherit is true, nulls defer to parent. otherwise, nulls = false

  -- roles/users/groups are ANDed together, only one needs to exist
  role_id SERIAL REFERENCES roles(id),
  user_id varchar(20) REFERENCES users(user_name),
  group_id SERIAL REFERENCES groups(id),



  owner BOOLEAN, -- can delete/change permissions/transfer owner. if its an action, owner=true means you can do the action
  can_edit BOOLEAN, 
  can_read BOOLEAN,

  _and SERIAL REFERENCES access(id),

  CHECK (role_id IS NOT NULL OR user_id IS NOT NULL OR group_id IS NOT NULL)
)
--;;
CREATE TABLE groups (
	   id SERIAL PRIMARY KEY,
	   access SERIAL REFERENCES access(id), -- edit access allows for changing roles and users in roles

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
--;;
