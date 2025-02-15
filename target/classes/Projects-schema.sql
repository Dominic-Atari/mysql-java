DROP TABLE IF EXISTS material;
DROP TABLE IF EXISTS step;
DROP TABLE IF EXISTS project_category;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS project;

CREATE TABLE project (
  project_id INT NOT NULL AUTO_INCREMENT,
  project_name VARCHAR(128) NOT NULL,
  estimated_hours DECIMAL(10, 2),
  actual_hours DECIMAL(10, 2),
  difficulty ENUM('FAIR', 'EASY', 'HARD', 'GOLD'),
  notes TEXT,
  PRIMARY KEY (project_id)
);

CREATE TABLE category (
  category_id INT NOT NULL AUTO_INCREMENT,
  category_name VARCHAR(128) NOT NULL,
  PRIMARY KEY (category_id)
);

CREATE TABLE project_category (
  project_id INT NOT NULL,
  category_id INT NOT NULL,
  PRIMARY KEY (project_id, category_id),
  FOREIGN KEY (project_id) REFERENCES project (project_id),
  FOREIGN KEY (category_id) REFERENCES category (category_id)
);

CREATE TABLE step (
  step_id INT NOT NULL AUTO_INCREMENT,
  project_id INT NOT NULL,
  step_text TEXT NOT NULL,
  step_order INT NOT NULL,
  PRIMARY KEY (step_id),
  FOREIGN KEY (project_id) REFERENCES project (project_id)
);

CREATE TABLE material (
  material_id INT NOT NULL AUTO_INCREMENT,
  project_id INT NOT NULL,
  material_name VARCHAR(64) NOT NULL,
  num_required INT NOT NULL,
  cost DECIMAL(7, 2),
  PRIMARY KEY (material_id),
  FOREIGN KEY (project_id) REFERENCES project (project_id)
);
