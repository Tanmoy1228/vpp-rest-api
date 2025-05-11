
-- Composite index to optimize postcode + capacity range filtering
CREATE INDEX idx_postcode_capacity ON batteries(postcode_numeric, capacity);
