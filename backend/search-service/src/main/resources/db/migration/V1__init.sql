-- === Extensions (idempotent) ===
CREATE EXTENSION IF NOT EXISTS pgcrypto;  -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS vector;    -- pgvector

-- === Products table ===
CREATE TABLE IF NOT EXISTS products (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),

  -- core fields
  title            text,
  description      text,
  category         text,
  brand            text,

  -- money-safe
  price            numeric(38,2),
  rating           numeric(38,2),

  -- canonical combined text (two independently-generated columns;
  -- neither references the other, to avoid 42P17)
  searchable_text  text GENERATED ALWAYS AS (
    trim(both from regexp_replace(
      coalesce(title,'') || ' ' ||
      coalesce(description,'') || ' ' ||
      coalesce(category,'') || ' ' ||
      coalesce(brand,''),
      '\s+', ' ', 'g'
    ))
  ) STORED,


  -- optional deterministic hash (computed in app code; column present for caching)
  searchable_hash  varchar(64),

  -- vector embedding
  embedding        vector(1536),
  embedding_ready  boolean NOT NULL DEFAULT false,

  -- FTS vector derived from the same base fields (not from generated cols)
  tsv              tsvector GENERATED ALWAYS AS (
    to_tsvector('english',
      trim(both from regexp_replace(
        coalesce(title,'') || ' ' ||
        coalesce(description,'') || ' ' ||
        coalesce(category,'') || ' ' ||
        coalesce(brand,''),
        '\s+', ' ', 'g'
      ))
    )
  ) STORED,

  created_at       timestamptz NOT NULL DEFAULT now(),
  updated_at       timestamptz NOT NULL DEFAULT now()
);

-- updated_at trigger
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  NEW.updated_at := now();
  RETURN NEW;
END $$;

DROP TRIGGER IF EXISTS trg_products_updated_at ON products;
CREATE TRIGGER trg_products_updated_at
BEFORE UPDATE ON products
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- indexes
CREATE INDEX IF NOT EXISTS idx_products_tsv_gin
  ON products USING gin (tsv);

CREATE INDEX IF NOT EXISTS idx_products_embedding_ready
  ON products (embedding_ready);

-- (optional, after data) IVFFlat index for faster vector search
-- CREATE INDEX IF NOT EXISTS idx_products_embedding_ivfflat
--   ON products USING ivfflat (embedding vector_l2_ops) WITH (lists = 100);