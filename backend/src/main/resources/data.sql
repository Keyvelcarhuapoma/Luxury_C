-- =====================================================
-- SCRIPT DE INICIALIZACION - LUXURY
-- Se ejecuta cada vez que arranca Spring Boot.
-- Cada INSERT verifica que NO exista el dato antes de insertar.
-- =====================================================

-- 1. ROLES
ALTER TABLE roles DROP CONSTRAINT IF EXISTS roles_nombre_check;
ALTER TABLE roles ADD CONSTRAINT roles_nombre_check CHECK (nombre IN ('ADMIN', 'GERENTE', 'ANALISTA', 'AUDITOR', 'OPERADOR'));

INSERT INTO roles (nombre) SELECT 'ADMIN' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nombre = 'ADMIN');
INSERT INTO roles (nombre) SELECT 'GERENTE' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nombre = 'GERENTE');
INSERT INTO roles (nombre) SELECT 'ANALISTA' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nombre = 'ANALISTA');
INSERT INTO roles (nombre) SELECT 'AUDITOR' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nombre = 'AUDITOR');
INSERT INTO roles (nombre) SELECT 'OPERADOR' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE nombre = 'OPERADOR');



-- 4. USUARIO ADMIN (password: admin123 codificado con BCrypt)
INSERT INTO usuarios (nombres, apellidos, tipo_documento, numero_documento, telefono, correo, contrasena_hash, activo, fecha_registro, fecha_actualizacion)
SELECT 'Administrador', 'Luxury', 'DNI', '00000000', '000000000', 'admin@luxury.com', '$2a$10$7OmvdgcMLwyQCneq5vCxC.p96pS4psIw3nDJRp8JvHO/hXQX0MpX6', true, current_timestamp, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE correo = 'admin@luxury.com');

UPDATE usuarios
SET contrasena_hash = '$2a$10$7OmvdgcMLwyQCneq5vCxC.p96pS4psIw3nDJRp8JvHO/hXQX0MpX6',
    fecha_actualizacion = current_timestamp
WHERE correo = 'admin@luxury.com'
  AND contrasena_hash = '$2a$10$QO0pW9vW4K8E8b4jR4xVTuYn2F3v.V8QGZ.7oX.aG2.hT3.wK3.W2';

INSERT INTO usuarios_roles (usuario_id, rol_id)
SELECT u.id, r.id FROM usuarios u, roles r
WHERE u.correo = 'admin@luxury.com' AND r.nombre = 'ADMIN'
AND NOT EXISTS (SELECT 1 FROM usuarios_roles ur WHERE ur.usuario_id = u.id AND ur.rol_id = r.id);

-- USUARIOS CON DOMINIO .pe PARA COMPATIBILIDAD CON FRONTEND
INSERT INTO usuarios (nombres, apellidos, tipo_documento, numero_documento, telefono, correo, contrasena_hash, activo, fecha_registro, fecha_actualizacion)
SELECT 'Administrador', 'Luxury PE', 'DNI', '00000001', '000000001', 'admin@luxury.pe', '$2a$10$7OmvdgcMLwyQCneq5vCxC.p96pS4psIw3nDJRp8JvHO/hXQX0MpX6', true, current_timestamp, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE correo = 'admin@luxury.pe');

INSERT INTO usuarios_roles (usuario_id, rol_id)
SELECT u.id, r.id FROM usuarios u, roles r
WHERE u.correo = 'admin@luxury.pe' AND r.nombre = 'ADMIN'
AND NOT EXISTS (SELECT 1 FROM usuarios_roles ur WHERE ur.usuario_id = u.id AND ur.rol_id = r.id);

INSERT INTO usuarios (nombres, apellidos, tipo_documento, numero_documento, telefono, correo, contrasena_hash, activo, id_sede, fecha_registro, fecha_actualizacion)
SELECT 'Gerente', 'Luxury PE', 'DNI', '00000002', '000000002', 'gerente@luxury.pe', '$2a$10$7OmvdgcMLwyQCneq5vCxC.p96pS4psIw3nDJRp8JvHO/hXQX0MpX6', true, (SELECT id_sede FROM sedes LIMIT 1), current_timestamp, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE correo = 'gerente@luxury.pe');

INSERT INTO usuarios_roles (usuario_id, rol_id)
SELECT u.id, r.id FROM usuarios u, roles r
WHERE u.correo = 'gerente@luxury.pe' AND r.nombre = 'GERENTE'
AND NOT EXISTS (SELECT 1 FROM usuarios_roles ur WHERE ur.usuario_id = u.id AND ur.rol_id = r.id);

INSERT INTO usuarios (nombres, apellidos, tipo_documento, numero_documento, telefono, correo, contrasena_hash, activo, id_sede, fecha_registro, fecha_actualizacion)
SELECT 'Operador', 'Luxury PE', 'DNI', '00000003', '000000003', 'operador@luxury.pe', '$2a$10$7OmvdgcMLwyQCneq5vCxC.p96pS4psIw3nDJRp8JvHO/hXQX0MpX6', true, (SELECT id_sede FROM sedes LIMIT 1), current_timestamp, current_timestamp
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE correo = 'operador@luxury.pe');

INSERT INTO usuarios_roles (usuario_id, rol_id)
SELECT u.id, r.id FROM usuarios u, roles r
WHERE u.correo = 'operador@luxury.pe' AND r.nombre = 'OPERADOR'
AND NOT EXISTS (SELECT 1 FROM usuarios_roles ur WHERE ur.usuario_id = u.id AND ur.rol_id = r.id);


-- 5. MONEDAS
INSERT INTO monedas (codigo, nombre) SELECT 'PEN', 'Sol peruano' WHERE NOT EXISTS (SELECT 1 FROM monedas WHERE codigo = 'PEN');
INSERT INTO monedas (codigo, nombre) SELECT 'USD', 'Dolar americano' WHERE NOT EXISTS (SELECT 1 FROM monedas WHERE codigo = 'USD');
INSERT INTO monedas (codigo, nombre) SELECT 'EUR', 'Euro' WHERE NOT EXISTS (SELECT 1 FROM monedas WHERE codigo = 'EUR');

-- 6. TIPOS DE CAMBIO
INSERT INTO tipos_cambio (id_moneda_origen, id_moneda_destino, valor, fecha, estado)
SELECT mo.id_moneda, md.id_moneda, 0.27, '2026-01-01', 'ACTIVO'
FROM monedas mo, monedas md
WHERE mo.codigo = 'PEN' AND md.codigo = 'USD'
AND NOT EXISTS (SELECT 1 FROM tipos_cambio tc WHERE tc.id_moneda_origen = mo.id_moneda AND tc.id_moneda_destino = md.id_moneda);

INSERT INTO tipos_cambio (id_moneda_origen, id_moneda_destino, valor, fecha, estado)
SELECT mo.id_moneda, md.id_moneda, 0.25, '2026-01-01', 'ACTIVO'
FROM monedas mo, monedas md
WHERE mo.codigo = 'PEN' AND md.codigo = 'EUR'
AND NOT EXISTS (SELECT 1 FROM tipos_cambio tc WHERE tc.id_moneda_origen = mo.id_moneda AND tc.id_moneda_destino = md.id_moneda);

-- 7. TIPOS DE RECURSO
INSERT INTO tipos_recurso (nombre, unidad_medida) SELECT 'Luz', 'kWh' WHERE NOT EXISTS (SELECT 1 FROM tipos_recurso WHERE nombre = 'Luz');
INSERT INTO tipos_recurso (nombre, unidad_medida) SELECT 'Agua', 'm3' WHERE NOT EXISTS (SELECT 1 FROM tipos_recurso WHERE nombre = 'Agua');
INSERT INTO tipos_recurso (nombre, unidad_medida) SELECT 'Gas', 'm3' WHERE NOT EXISTS (SELECT 1 FROM tipos_recurso WHERE nombre = 'Gas');

-- 8. SEDES
INSERT INTO sedes (nombre, ciudad, direccion, estado) SELECT 'Sede Lima', 'Lima', 'Av. Principal 100', 'ACTIVO' WHERE NOT EXISTS (SELECT 1 FROM sedes WHERE nombre = 'Sede Lima');
INSERT INTO sedes (nombre, ciudad, direccion, estado) SELECT 'Sede Piura', 'Piura', 'Av. Grau 200', 'ACTIVO' WHERE NOT EXISTS (SELECT 1 FROM sedes WHERE nombre = 'Sede Piura');
INSERT INTO sedes (nombre, ciudad, direccion, estado) SELECT 'Sede Trujillo', 'Trujillo', 'Av. Espana 300', 'ACTIVO' WHERE NOT EXISTS (SELECT 1 FROM sedes WHERE nombre = 'Sede Trujillo');

INSERT INTO usuarios (nombres, apellidos, tipo_documento, numero_documento, telefono, correo, contrasena_hash, activo, id_sede, fecha_registro, fecha_actualizacion)
SELECT 'Operador', 'Prueba', 'DNI', '22223333', '922223333', 'operador@luxury.com', '$2a$10$7OmvdgcMLwyQCneq5vCxC.p96pS4psIw3nDJRp8JvHO/hXQX0MpX6', true, s.id_sede, current_timestamp, current_timestamp
FROM sedes s WHERE s.nombre = 'Sede Lima' AND NOT EXISTS (SELECT 1 FROM usuarios WHERE numero_documento = '22223333' OR correo = 'operador@luxury.com');

INSERT INTO usuarios_roles (usuario_id, rol_id)
SELECT u.id, r.id FROM usuarios u, roles r WHERE u.numero_documento = '22223333' AND r.nombre = 'OPERADOR' AND NOT EXISTS (SELECT 1 FROM usuarios_roles ur WHERE ur.usuario_id = u.id AND ur.rol_id = r.id);

INSERT INTO usuarios (nombres, apellidos, tipo_documento, numero_documento, telefono, correo, contrasena_hash, activo, id_sede, fecha_registro, fecha_actualizacion)
SELECT 'Auditor', 'Prueba', 'DNI', '33334444', '933334444', 'auditor@luxury.com', '$2a$10$7OmvdgcMLwyQCneq5vCxC.p96pS4psIw3nDJRp8JvHO/hXQX0MpX6', true, s.id_sede, current_timestamp, current_timestamp
FROM sedes s WHERE s.nombre = 'Sede Lima' AND NOT EXISTS (SELECT 1 FROM usuarios WHERE numero_documento = '33334444' OR correo = 'auditor@luxury.com');

INSERT INTO usuarios_roles (usuario_id, rol_id)
SELECT u.id, r.id FROM usuarios u, roles r WHERE u.numero_documento = '33334444' AND r.nombre = 'AUDITOR' AND NOT EXISTS (SELECT 1 FROM usuarios_roles ur WHERE ur.usuario_id = u.id AND ur.rol_id = r.id);

-- 9. TARIFAS (referencias Lima/Peru 2026, sin IGV)
-- Luz: Osinergmin/Pluz Energia, BT5B no residencial: 62.63 ctm S/kWh = S/ 0.6263 por kWh.
-- Agua: Sedapal, categoria comercial y otros Grupo 1, 0-1000 m3: agua S/ 8.82 + saneamiento S/ 4.21 = S/ 13.03 por m3.
-- Gas: Calidda julio 2026, categoria B comercial aproximada: precio gas S/ 0.48442453 + distribucion variable S/ 0.40345519 = S/ 0.88787972 por m3.
INSERT INTO tarifas_recurso (id_sede, id_tipo_recurso, precio_unitario_pen, fecha_inicio, estado)
SELECT s.id_sede,
       t.id_tipo_recurso,
       CASE
         WHEN UPPER(t.nombre) LIKE '%AGUA%' THEN 13.0300
         WHEN UPPER(t.nombre) LIKE '%GAS%' THEN 0.8879
         ELSE 0.6263
       END,
       '2026-01-01',
       'ACTIVO'
FROM sedes s, tipos_recurso t
WHERE NOT EXISTS (SELECT 1 FROM tarifas_recurso tr WHERE tr.id_sede = s.id_sede AND tr.id_tipo_recurso = t.id_tipo_recurso);

UPDATE tarifas_recurso tr
SET precio_unitario_pen = CASE
    WHEN UPPER(t.nombre) LIKE '%AGUA%' THEN 13.0300
    WHEN UPPER(t.nombre) LIKE '%GAS%' THEN 0.8879
    ELSE 0.6263
  END
FROM tipos_recurso t
WHERE tr.id_tipo_recurso = t.id_tipo_recurso
  AND tr.estado = 'ACTIVO';

-- 10. UMBRALES (un umbral por cada recurso en todas las Sedes)
INSERT INTO umbrales (id_sede, id_tipo_recurso, limite_consumo, limite_presupuesto_pen, fecha_inicio, estado)
SELECT s.id_sede, t.id_tipo_recurso, 1000, 800, '2026-01-01', 'ACTIVO'
FROM sedes s, tipos_recurso t
WHERE NOT EXISTS (SELECT 1 FROM umbrales u WHERE u.id_sede = s.id_sede AND u.id_tipo_recurso = t.id_tipo_recurso);

-- 11. CONSUMOS DE PRUEBA (una fila por sede + recurso + mes, idempotente por sede/periodo)
-- Marzo 2026 - todas las sedes
INSERT INTO consumos (id_sede, id_tipo_recurso, id_tarifa, id_usuario_registro, cantidad_consumida, fecha_consumo, periodo, creado_en)
SELECT s.id_sede, t.id_tipo_recurso, tr.id_tarifa, u.id,
       ROUND((500 + random() * 1000)::numeric, 4),
       '2026-03-15', '2026-03', current_timestamp
FROM sedes s, tipos_recurso t, tarifas_recurso tr, usuarios u
WHERE u.correo = 'admin@luxury.com'
  AND tr.id_sede = s.id_sede AND tr.id_tipo_recurso = t.id_tipo_recurso
  AND NOT EXISTS (SELECT 1 FROM consumos c WHERE c.periodo = '2026-03' AND c.id_sede = s.id_sede);

-- Abril 2026 - todas las sedes
INSERT INTO consumos (id_sede, id_tipo_recurso, id_tarifa, id_usuario_registro, cantidad_consumida, fecha_consumo, periodo, creado_en)
SELECT s.id_sede, t.id_tipo_recurso, tr.id_tarifa, u.id,
       ROUND((500 + random() * 1000)::numeric, 4),
       '2026-04-15', '2026-04', current_timestamp
FROM sedes s, tipos_recurso t, tarifas_recurso tr, usuarios u
WHERE u.correo = 'admin@luxury.com'
  AND tr.id_sede = s.id_sede AND tr.id_tipo_recurso = t.id_tipo_recurso
  AND NOT EXISTS (SELECT 1 FROM consumos c WHERE c.periodo = '2026-04' AND c.id_sede = s.id_sede);

-- Mayo 2026 - todas las sedes
INSERT INTO consumos (id_sede, id_tipo_recurso, id_tarifa, id_usuario_registro, cantidad_consumida, fecha_consumo, periodo, creado_en)
SELECT s.id_sede, t.id_tipo_recurso, tr.id_tarifa, u.id,
       ROUND((500 + random() * 1000)::numeric, 4),
       '2026-05-15', '2026-05', current_timestamp
FROM sedes s, tipos_recurso t, tarifas_recurso tr, usuarios u
WHERE u.correo = 'admin@luxury.com'
  AND tr.id_sede = s.id_sede AND tr.id_tipo_recurso = t.id_tipo_recurso
  AND NOT EXISTS (SELECT 1 FROM consumos c WHERE c.periodo = '2026-05' AND c.id_sede = s.id_sede);

-- 12. COSTOS EN PEN para cada consumo (solo si no hay costos registrados)
INSERT INTO consumo_costos (id_consumo, id_moneda, monto_calculado, fecha_calculo)
SELECT c.id_consumo, m.id_moneda, ROUND((c.cantidad_consumida * tr.precio_unitario_pen), 4), current_timestamp
FROM consumos c
  JOIN tarifas_recurso tr ON c.id_tarifa = tr.id_tarifa,
  monedas m
WHERE m.codigo = 'PEN'
  AND NOT EXISTS (SELECT 1 FROM consumo_costos cc WHERE cc.id_consumo = c.id_consumo AND cc.id_moneda = m.id_moneda);

-- Costos en USD
INSERT INTO consumo_costos (id_consumo, id_moneda, monto_calculado, fecha_calculo)
SELECT c.id_consumo, m.id_moneda,
       ROUND((c.cantidad_consumida * tr.precio_unitario_pen * tc.valor), 4),
       current_timestamp
FROM consumos c
  JOIN tarifas_recurso tr ON c.id_tarifa = tr.id_tarifa,
  monedas mpen, monedas m, tipos_cambio tc
WHERE mpen.codigo = 'PEN' AND m.codigo = 'USD'
  AND tc.id_moneda_origen = mpen.id_moneda AND tc.id_moneda_destino = m.id_moneda
  AND NOT EXISTS (SELECT 1 FROM consumo_costos cc WHERE cc.id_consumo = c.id_consumo AND cc.id_moneda = m.id_moneda);

-- Costos en EUR
INSERT INTO consumo_costos (id_consumo, id_moneda, monto_calculado, fecha_calculo)
SELECT c.id_consumo, m.id_moneda,
       ROUND((c.cantidad_consumida * tr.precio_unitario_pen * tc.valor), 4),
       current_timestamp
FROM consumos c
  JOIN tarifas_recurso tr ON c.id_tarifa = tr.id_tarifa,
  monedas mpen, monedas m, tipos_cambio tc
WHERE mpen.codigo = 'PEN' AND m.codigo = 'EUR'
  AND tc.id_moneda_origen = mpen.id_moneda AND tc.id_moneda_destino = m.id_moneda
  AND NOT EXISTS (SELECT 1 FROM consumo_costos cc WHERE cc.id_consumo = c.id_consumo AND cc.id_moneda = m.id_moneda);

-- Recalcula costos existentes si las tarifas semilla cambiaron.
UPDATE consumo_costos
SET monto_calculado = (
  SELECT ROUND((c.cantidad_consumida * tr.precio_unitario_pen), 4)
  FROM consumos c
  JOIN tarifas_recurso tr ON c.id_tarifa = tr.id_tarifa
  WHERE c.id_consumo = consumo_costos.id_consumo
),
fecha_calculo = current_timestamp
WHERE id_moneda IN (SELECT id_moneda FROM monedas WHERE codigo = 'PEN');

UPDATE consumo_costos
SET monto_calculado = (
  SELECT ROUND((c.cantidad_consumida * tr.precio_unitario_pen * tc.valor), 4)
  FROM consumos c
  JOIN tarifas_recurso tr ON c.id_tarifa = tr.id_tarifa
  JOIN monedas mpen ON mpen.codigo = 'PEN'
  JOIN tipos_cambio tc ON tc.id_moneda_origen = mpen.id_moneda AND tc.id_moneda_destino = consumo_costos.id_moneda
  WHERE c.id_consumo = consumo_costos.id_consumo
),
fecha_calculo = current_timestamp
WHERE id_moneda IN (SELECT id_moneda FROM monedas WHERE codigo IN ('USD', 'EUR'));
