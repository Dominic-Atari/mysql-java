-- Specify the schema to be used.
USE recipes;

-- Drop tables first so you can run this SQL script multile times to 
-- re-create the tables.  You always drop dependent (child) tables first.
DROP TABLE IF EXISTS ingredient;
DROP TABLE IF EXISTS step;
DROP TABLE IF EXISTS recipe_category;
DROP TABLE IF EXISTS unit;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS recipe;


-- Create recipe table.
CREATE TABLE recipe (
  recipe_id INT AUTO_INCREMENT NOT NULL,
  recipe_name VARCHAR(128) NOT NULL,
  notes TEXT,
  num_servings INT,
  prep_time TIME,
  cook_time TIME,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (recipe_id)
);

-- Create category table.
CREATE TABLE category (
  category_id INT AUTO_INCREMENT NOT NULL,
  category_name VARCHAR(64) NOT NULL,
  PRIMARY KEY (category_id)
);

-- Create unit table.
CREATE TABLE unit (
  unit_id INT AUTO_INCREMENT NOT NULL,
  unit_name_singular VARCHAR(32) NOT NULL,
  unit_name_plural VARCHAR(34) NOT NULL,
  PRIMARY KEY (unit_id)
);

-- Join table for recipe and category tables.
CREATE TABLE recipe_category (
  recipe_id INT NOT NULL,
  category_id INT NOT NULL,
  foreign key (recipe_id) REFERENCES recipe (recipe_id),
  foreign key(category_id) REFERENCES category (category_id),
  unique key (recipe_id, category_id)
);

-- Create step table.
CREATE TABLE step (
  step_id INT AUTO_INCREMENT NOT NULL,
  recipe_id INT NOT NULL,
  step_order INT NOT NULL,
  step_text TEXT NOT NULL,
  PRIMARY KEY (step_id),
  FOREIGN KEY (recipe_id) REFERENCES recipe (recipe_id) ON DELETE CASCADE
);

-- Create ingredient table.
CREATE TABLE ingredient (
  ingredient_id INT AUTO_INCREMENT NOT NULL,
  recipe_id INT NOT NULL,
  unit_id INT,
  ingredient_name VARCHAR(64),
  instruction VARCHAR(64),
  ingredient_order INT NOT NULL,
  amount DECIMAL(7, 2),
  PRIMARY KEY (ingredient_id),
  FOREIGN KEY (recipe_id) REFERENCES recipe (recipe_id) ON DELETE CASCADE,
  FOREIGN KEY (unit_id) REFERENCES unit (unit_id) 
);