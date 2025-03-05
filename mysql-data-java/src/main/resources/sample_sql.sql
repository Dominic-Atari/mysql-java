SHOW DATABASES;

SHOW TABLES;

DESC recipe;

SELECT *
FROM recipe;

SELECT COUNT(*)
FROM recipe r;

SELECT *
FROM recipe r
LIMIT 3;

SELECT recipe_id, recipe_name
FROM   recipe;

SELECT recipe_id, recipe_name
FROM recipe
WHERE recipe_id = 1

SELECT i.ingredient_name
FROM ingredient i
WHERE i.recipe_id = 4;

SELECT recipe_id, recipe_name
FROM recipe
WHERE recipe_id = 1;

-- ORDER BY clause
SELECT *
FROM category
ORDER BY category_name;

-- AND clause
SELECT *
FROM category
WHERE category_name >= 'a'
  AND category_name <= 'd';

-- OR clause
 SELECT *
FROM category
WHERE category_name <= 'b'
  OR category_name >= 't';
 
-- IN clause
 SELECT *
FROM category
WHERE category_id IN (4, 6, 8);

-- BETWEEN clause
SELECT *
FROM category
WHERE category_id BETWEEN 8 AND 10;

-- LIKE clause
SELECT *
FROM category
WHERE category_name LIKE '_e%';

-- IS NULL clause
SELECT ingredient_name, amount
FROM ingredient
WHERE amount IS NULL;

-- NOT clause
SELECT *
FROM category
WHERE category_id NOT BETWEEN 3 AND 22;

-- ORDER BY clause
SELECT *
FROM category
ORDER BY category_name desc;

-- More than one ORDER BY clause
SELECT prep_time, recipe_name
FROM recipe
ORDER BY prep_time, recipe_name;

-- DISTINCT clause
SELECT DISTINCT 
  prep_time
FROM recipe
ORDER BY prep_time;

-- Same query without DISTINCT clause
SELECT prep_time
FROM recipe
ORDER BY prep_time;

-- LIMIT clause
SELECT recipe_name, prep_time
FROM recipe
ORDER BY prep_time
LIMIT 3;

SELECT recipe_name, prep_time
FROM recipe
ORDER BY prep_time
LIMIT 2 OFFSET 1;

-- GROUP BY clause
SELECT count(*) AS 'Num Recipes', prep_time
FROM recipe
GROUP BY prep_time;

-- HAVING clause
SELECT count(*) AS 'Num Recipes', prep_time
FROM recipe
GROUP BY prep_time
HAVING count(*) = 1;

-- Table aliases
SELECT i.ingredient_name
FROM ingredient i
WHERE i.recipe_id = 4;

-- INNER Join (form 1)
SELECT u.unit_id AS uid, i.ingredient_id 
  AS iid, u.unit_name_singular, u.unit_name_plural  AS unit, 
  i.ingredient_name AS ingredient
FROM ingredient i
INNER JOIN unit u USING (unit_id)
WHERE recipe_id = 1;

-- INNER Join (form 1)
SELECT u.unit_id AS uid, i.ingredient_id 
  AS iid, u.unit_name_singular, u.unit_name_plural, 
  i.ingredient_name AS ingredient
FROM ingredient i
INNER JOIN unit u ON u.unit_id = i.unit_id
WHERE recipe_id = 1;

-- INNER Join (form 3)
SELECT u.unit_id AS uid, i.ingredient_id 
  AS iid, u.unit_name_singular, u.unit_name_plural, 
  i.ingredient_name AS ingredient
FROM ingredient i, unit u
WHERE u.unit_id = i.unit_id
AND recipe_id = 1;

select * from ingredient i 
where recipe_id = 1;

-- LEFT OUTER Join
SELECT u.unit_id AS uid, i.ingredient_id 
  AS iid, u.unit_name_singular, u.unit_name_plural, 
  i.ingredient_name AS ingredient
FROM ingredient i
LEFT JOIN unit u USING (unit_id)
WHERE recipe_id = 1;

-- RIGHT OUTER Join
SELECT u.unit_id AS uid, i.ingredient_id 
  AS iid, u.unit_name_singular, u.unit_name_plural,  
  i.ingredient_name AS ingredient
FROM ingredient i
RIGHT JOIN unit u USING (unit_id);
-- WHERE recipe_id = 1

select * from unit;

-- CROSS Join
SELECT u.unit_id AS uid, i.ingredient_id 
  AS iid, u.unit_name_singular, u.unit_name_plural, 
  i.ingredient_name AS ingredient
FROM ingredient i
CROSS JOIN unit u
WHERE recipe_id = 1;

-- AVG example
SELECT AVG(i.amount) AS Average
FROM ingredient i;

-- CONCAT example
SELECT DISTINCT CONCAT(i.amount, ' ', 
  u.unit_name_plural) AS Amount
FROM ingredient i
INNER JOIN unit u USING(unit_id)
WHERE u.unit_name_plural LIKE 'ou%';

-- --------------------------------------------------------
select * from recipe where recipe_id = 7;
select * from ingredient where recipe_id = 7;
select * from step where recipe_id =7;
select * from recipe_category where recipe_id =7;