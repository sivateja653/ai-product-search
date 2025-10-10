CREATE TABLE IF NOT EXISTS products (
  id UUID PRIMARY KEY,
  title TEXT NOT NULL,
  description TEXT,
  category TEXT,
  brand TEXT,
  price NUMERIC(10,2),
  rating NUMERIC(3,2),
  searchable_text TEXT,
  embedding VECTOR(1536)
);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_products_brand ON products(brand);