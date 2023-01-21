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

	   user_id varchar(20) REFERENCES users(user_name) NOT NULL ON DELETE CASCADE, -- also used as slug
	   title TEXT NOT NULL,

	   description TEXT NOT NULL,

	   created_at TIMESTAMP NOT NULL,
	   edited_at TIMESTAMP NOT NULL,

	   CHECK (length(title) > 1)
)
--;;
CREATE TABLE roles (
	   id SERIAL PRIMARY KEY,

	   group_id SERIAL REFERENCES groups(id) NOT NULL,
	   title TEXT NOT NULL,

	   color TEXT NOT NULL,
	   UNIQUE (group_id, title)
	   CHECK (length(title) > 1)
)
--;;
CREATE TABLE users_in_groups (
	   user_id varchar(20) REFERENCES users(id) NOT NULL,
	   group_id SERIAL REFERENCES groups(id) NOT NULL,
	   role_id SERIAL REFERENCES roles(id)

	   PRIMARY KEY (user_id, group_id, role_id)
)
--;;
CREATE TABLE items (
	   id SERIAL PRIMARY KEY,
	   access SERIAL REFERENCES access(id) DEFAULT NULL, -- null = inherit -- TODO how would i do access control for things made by discord api? maybe just give access to dicsord api, answer is http call

	   domain_pk_namespace TEXT NOT NULL,
	   domain_pk TEXT NOT NULL,


	   title TEXT NOT NULL,
	   slug TEXT NOT NULL,

	   body TEXT, -- TODO make this jsonb/edn? only do that if i find use outside current model, cant think of it.
	   url TEXT,
	   -- typ TEXT, not needed for a while i think, domain_pk_namespace can be youtube.com, can adopt convention of org.java.util.customer.namespace/{youtube,github,etc} for type

	   created_at TIMESTAMP NOT NULL,
	   edited_at TIMESTAMP NOT NULL,

	   UNIQUE (domain_pk_namespace, domain_pk)
	   CHECK (length(title) > 1)
)
--;;
CREATE TABLE votes {aoietsrraietsnarst items_in_tag as well. then api key table....}
