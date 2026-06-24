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

create or replace function jsonb_extract_path_date(data jsonb, variadic path text[])
returns date
language sql
immutable
as $$
select nullif(jsonb_extract_path_text(data, variadic path), '')::date
$$;

create or replace function jsonb_extract_path_ts(data jsonb, variadic path text[])
returns timestamp
language sql
immutable
as $$
select nullif(jsonb_extract_path_text(data, variadic path), '')::timestamp
$$;

create or replace function jsonb_extract_path_tstz(data jsonb, variadic path text[])
returns timestamp with time zone
language sql
immutable
as $$
select nullif(jsonb_extract_path_text(data, variadic path), '')::timestamp with time zone
$$;

create or replace function jsonb_extract_path_uuid(data jsonb, variadic path text[])
returns uuid
language sql
immutable
as $$
select nullif(jsonb_extract_path_text(data, variadic path), '')::uuid
$$;
