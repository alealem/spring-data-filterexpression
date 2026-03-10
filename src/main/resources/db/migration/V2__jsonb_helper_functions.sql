create or replace function jsonb_extract_path_numeric(data jsonb, variadic path text[])
returns numeric
language sql
immutable
as $$
select nullif(jsonb_extract_path_text(data, variadic path), '')::numeric
$$;

create or replace function jsonb_extract_path_bool(data jsonb, variadic path text[])
returns boolean
language sql
immutable
as $$
select nullif(jsonb_extract_path_text(data, variadic path), '')::boolean
$$;

create or replace function jsonb_extract_path_ts(data jsonb, variadic path text[])
returns timestamp
language sql
immutable
as $$
select nullif(jsonb_extract_path_text(data, variadic path), '')::timestamp
$$;
create or replace function jsonb_extract_path_numeric(data jsonb, variadic path text[])
returns numeric
language sql
immutable
as $$
select nullif(jsonb_extract_path_text(data, variadic path), '')::numeric
$$;