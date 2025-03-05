SELECT i.ingredient_name
FROM ingredient i
WHERE i.recipe_id = 4;

-- Inner join form #1
SELECT u.unit_id AS uid, 
       i.ingredient_id AS iid, 
       u.unit_name_singular AS unit_singular,
       u.unit_name_plural AS unit_plural,
       i.amount AS amount,
       i.ingredient_name AS ingredient
FROM ingredient i
INNER JOIN unit u USING (unit_id)
WHERE i.recipe_id = 1;

-- Inner join form #2
SELECT u.unit_id AS uid, 
       i.ingredient_id AS iid, 
       u.unit_name_singular AS unit_singular,
       u.unit_name_plural AS unit_plural,
       i.amount AS amount,
       i.ingredient_name AS ingredient
FROM ingredient i
INNER JOIN unit u ON u.unit_id = i.unit_id
WHERE recipe_id = 1;

-- Inner join form #3
SELECT u.unit_id AS uid, 
       i.ingredient_id AS iid, 
       u.unit_name_singular AS unit_singular,
       u.unit_name_plural AS unit_plural,
       i.amount AS amount,
       i.ingredient_name AS ingredient
FROM ingredient i, unit u
WHERE u.unit_id = i.unit_id
AND recipe_id = 1;

select * 
  from ingredient i 
 where recipe_id = 1; 

-- Left outer join
SELECT u.unit_id AS uid, 
       i.ingredient_id AS iid, 
       u.unit_name_singular AS unit_singular,
       u.unit_name_plural AS unit_plural,
       i.amount AS amount,
       i.ingredient_name AS ingredient
       FROM ingredient i
LEFT JOIN unit u USING (unit_id)
WHERE recipe_id = 1;

SELECT i.ingredient_id AS iid, 
       i.ingredient_name AS ingredient
       FROM ingredient i
WHERE recipe_id = 1;

-- Right outer join
SELECT u.unit_id AS uid, 
       i.ingredient_id AS iid, 
       u.unit_name_singular AS unit_singular,
       u.unit_name_plural AS unit_plural,
       i.amount AS amount,
       i.ingredient_name AS ingredient
  FROM ingredient i
RIGHT JOIN unit u USING (unit_id);
-- WHERE recipe_id = 1;

-- Cross join - someting you can do in MySQL, usually a bad thing to do.
SELECT u.unit_id AS uid, 
       i.ingredient_id AS iid, 
       u.unit_name_singular AS unit_singular,
       u.unit_name_plural AS unit_plural,
       i.amount AS amount,
       i.ingredient_name AS ingredient
       FROM ingredient i
CROSS JOIN unit u
WHERE recipe_id = 1;

SELECT * 
  FROM recipe_category rc   
INNER JOIN recipe r USING (recipe_id)  
 WHERE category_id = ( 
					 SELECT category_id 
    				 FROM category
    				 WHERE category_name = 'Side Dish');
    				
    				SELECT category_id 
    				 FROM category
    				 WHERE category_name = 'Side Dish';

