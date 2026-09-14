INSERT INTO productos (activo, descripcion, nombre, precio, stock)
SELECT 1, 'Laptop para desarrollo y oficina', 'Laptop Lenovo ThinkPad', 850000.00, 15
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre = 'Laptop Lenovo ThinkPad');

INSERT INTO productos (activo, descripcion, nombre, precio, stock)
SELECT 1, 'Mouse inalambrico ergonomico', 'Mouse Logitech MX Master', 45000.00, 30
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre = 'Mouse Logitech MX Master');

INSERT INTO productos (activo, descripcion, nombre, precio, stock)
SELECT 1, 'Monitor 27 pulgadas 4K', 'Monitor Samsung 27"', 320000.00, 8
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre = 'Monitor Samsung 27"');
