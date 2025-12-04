SET CHARACTER SET utf8mb4;
SET COLLATE utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS ingredients (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Unique ingredient identifier',
  name VARCHAR(255) NOT NULL COMMENT 'Ingredient name (e.g., Frango, Arroz)',
  portion_unit VARCHAR(2) NOT NULL DEFAULT 'g' COMMENT 'Portion unit (g=grams, ml=milliliters)',
  energy_kcal DECIMAL(8,2) COMMENT 'Energy value in kcal (calories)',
  energy_kj DECIMAL(8,2) COMMENT 'Energy value in kJ (kilojoules)',
  carbohydrates DECIMAL(8,2) COMMENT 'Carbohydrates in grams',
  total_sugars DECIMAL(8,2) COMMENT 'Total sugars in grams',
  added_sugars DECIMAL(8,2) COMMENT 'Added sugars in grams',
  proteins DECIMAL(8,2) COMMENT 'Proteins in grams',
  total_fats DECIMAL(8,2) COMMENT 'Total fats in grams',
  saturated_fats DECIMAL(8,2) COMMENT 'Saturated fats in grams',
  trans_fats DECIMAL(8,2) COMMENT 'Trans fats in grams',
  dietary_fiber DECIMAL(8,2) COMMENT 'Dietary fiber in grams',
  sodium DECIMAL(8,2) COMMENT 'Sodium content in mg',
  tbca_code VARCHAR(255) UNIQUE COMMENT 'TBCA reference code (Brazilian Food Table)',
  category VARCHAR(100) COMMENT 'Food category (e.g., Carbohydrates, Proteins)',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Ingredient creation timestamp',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last modification timestamp',
  INDEX idx_name (name),
  INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Food ingredients database with complete nutritional information per 100g/100ml';


CREATE TABLE IF NOT EXISTS recipes (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Unique recipe identifier',
  name VARCHAR(255) NOT NULL COMMENT 'Recipe name (e.g., Frango Grelhado com Arroz)',
  preparation_method VARCHAR(20) NOT NULL COMMENT 'Cooking method: RAW, BOILED, FRIED, BAKED, GRILLED, STEAMED',
  total_portion DECIMAL(8,2) NOT NULL COMMENT 'Total portion size after cooking',
  portion_unit VARCHAR(2) NOT NULL DEFAULT 'g' COMMENT 'Portion unit (g=grams, ml=milliliters)',
  servings INT COMMENT 'Number of servings recipe yields',
  instructions TEXT COMMENT 'Preparation instructions/notes',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Recipe creation timestamp',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last modification timestamp',
  INDEX idx_name (name),
  INDEX idx_preparation_method (preparation_method),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='User-created recipes combining ingredients for nutritional analysis';


CREATE TABLE IF NOT EXISTS recipe_ingredients (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Unique line item identifier',
  recipe_id BIGINT NOT NULL COMMENT 'Reference to the recipe',
  ingredient_id BIGINT NOT NULL COMMENT 'Reference to the ingredient',
  quantity DECIMAL(8,2) NOT NULL COMMENT 'Quantity of ingredient in the recipe (in portion_unit)',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record modification timestamp',
  UNIQUE KEY uk_recipe_ingredient (recipe_id, ingredient_id),
  KEY fk_recipe_idx (recipe_id),
  KEY fk_ingredient_idx (ingredient_id),
  CONSTRAINT fk_recipe_ri FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_ingredient_ri FOREIGN KEY (ingredient_id) REFERENCES ingredients(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Junction table linking ingredients to recipes with their quantities';

