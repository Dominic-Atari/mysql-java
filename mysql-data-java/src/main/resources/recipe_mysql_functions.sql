-- Math functions
SELECT COUNT(i.amount) AS Count
FROM ingredient i;

SELECT AVG(i.amount) AS Average
FROM ingredient i;

SELECT SUM(i.amount) AS Sum
FROM ingredient i;

SELECT MIN(i.amount) AS Min
FROM ingredient i;

SELECT MAX(i.amount) AS Max
FROM ingredient i;

-- String functions
SELECT DISTINCT CONCAT(i.amount, ' ', u.unit_name_plural) AS Amount
FROM ingredient i
INNER JOIN unit u USING(unit_id)
WHERE u.unit_name_plural LIKE 'ou%';

SELECT UPPER(r.recipe_name) AS 'Name Upper'
FROM recipe r ;

SELECT LOWER(r.recipe_name) AS 'Name Lower'
FROM recipe r ;

-- Date / Time functions
SELECT CURRENT_TIMESTAMP() As Now;

SELECT CURDATE() As Now;

SELECT CURTIME() As Now;