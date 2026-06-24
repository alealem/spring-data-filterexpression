create or replace function jsonb_extract_path_date(data jsonb, variadic path text[])
returns date
language sql
immutable
as $$
select nullif(jsonb_extract_path_text(data, variadic path), '')::date
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
