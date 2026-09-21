-- =====================================
-- Massa de Dados – Gestão de Pedidos
-- =====================================

-- 1️⃣ CLIENTES
INSERT INTO customers (name, email, created_at) VALUES
('João Silva', 'joao@email.com', CURRENT_TIMESTAMP),
('Maria Souza', 'maria@email.com', CURRENT_TIMESTAMP),
('Carlos Lima', 'carlos@email.com', CURRENT_TIMESTAMP);

-- 2️⃣ PRODUTOS
INSERT INTO products (name, category, price_cents, active) VALUES
('Camiseta', 'Roupas', 4990, 1),
('Calça Jeans', 'Roupas', 8990, 1),
('Tênis Esportivo', 'Calçados', 15990, 1),
('Relógio Clássico', 'Acessórios', 25990, 0);

-- 3️⃣ PEDIDO
-- customer_id precisa existir
INSERT INTO orders (customer_id, status, created_at)
VALUES (1, 'NEW', CURRENT_TIMESTAMP);

-- 4️⃣ ITENS DO PEDIDO
-- order_id e product_id precisam existir
INSERT INTO order_items (order_id, product_id, quantity, unit_price_cents)
VALUES
(1, 1, 2, 4990),
(1, 2, 1, 8990);

-- 5️⃣ PAGAMENTO PARCIAL
INSERT INTO payments (order_id, method, amount_cents, paid_at)
VALUES (1, 'PIX', 5000, CURRENT_TIMESTAMP);

-- 6️⃣ PAGAMENTO COMPLEMENTAR
INSERT INTO payments (order_id, method, amount_cents, paid_at)
VALUES (1, 'CARD', 13970, CURRENT_TIMESTAMP);

-- 7️⃣ ATUALIZAÇÃO MANUAL (opcional)
UPDATE orders
SET status = 'PAID'
WHERE id = 1;

-- 8️⃣ CONSULTAS ÚTEIS
SELECT * FROM customers;
SELECT * FROM products;
SELECT * FROM orders;
SELECT * FROM order_items;
SELECT * FROM payments;
