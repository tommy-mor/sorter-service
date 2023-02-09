CREATE TABLE tags (
	   id SERIAL PRIMARY KEY,
	   access integer REFERENCES access(id),

	   title TEXT NOT NULL,
	   slug TEXT NOT NULL,
	   description TEXT,

	   domain_pk TEXT, -- PLHTRW7dzCGPzAC_kXkSyJ4Ru54Y9dMEjG
	   domain_pk_namespace TEXT, -- youtube.playlist

	   created_at TIMESTAMP NOT NULL DEFAULT NOW(),
	   edited_at TIMESTAMP NOT NULL DEFAULT NOW(),

	   UNIQUE (domain_pk_namespace, domain_pk),
	   CHECK (length(title) > 1)
)
--;;
CREATE TABLE items_in_tags (
	   item_id integer REFERENCES items(id) NOT NULL,
	   tag_id integer REFERENCES tags(id) NOT NULL,

	   PRIMARY KEY (item_id, tag_id)
)
