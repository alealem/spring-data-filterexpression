create table contents (
                          id uuid primary key,
                          title text not null,
                          description text null
);

create table content_items (
                               id uuid primary key,
                               content_id uuid not null references contents(id) on delete cascade,
                               body jsonb not null,

                               body_tsv tsvector generated always as (
                                   to_tsvector('simple', coalesce(body::text, ''))
                                   ) stored
);

create index idx_content_items_content_id on content_items(content_id);
create index idx_content_items_body_tsv on content_items using gin (body_tsv);