// Copyright (c) 2022 Promineo Tech

package recipes.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import provided.util.DaoBase;
import recipes.entity.Category;
import recipes.entity.Ingredient;
import recipes.entity.Recipe;
import recipes.entity.Step;
import recipes.entity.Unit;
import recipes.exception.DbException;

/**
 * This class performs CRUD (Create, Read, Update and Delete) operations on
 * tables in the recipe schema. Connections are obtained from
 * {@link DbConnection#getConnection()}. All operations within a transaction
 * must be made on the same connection. The strategy is to use try-with-resource
 * to ensure that resources are always closed properly. The approach looks like
 * this:
 * 
 * <pre>
 * String sql = "...";
 * 
 * try(Connection conn = DbConnection.getConnection()) {
 *   try(PreparedStatement stmt = conn.prepareStatement(sql)) {
 *     setParameter(stmt, 1, parm1, Parm1.class);
 *     ...
 *     
 *     try(ResultSet rs = stmt.executeQuery()) {
 *       while(rs.next) {
 *         <em>Object<em> value = extract(rs, <em>Object</em>.class);
 *         // Where <em>Object</em> is the actual entity type: Recipe, etc.
 *       }
 *     }
 *     
 *     commitTransaction(conn);
 *     return value;
 *   }
 *   catch(Exception e) {
 *     rollbackTransaction(conn);
 *     throw new DbException(e);
 *   }
 * catch(SQLException e) {
 *   throw new DbException(e);
 * }
 * </pre>
 * 
 * @author Promineo
 *
 */
public class RecipeDao extends DaoBase {
  private static final String CATEGORY_TABLE = "category";
  private static final String INGREDIENT_TABLE = "ingredient";
  private static final String RECIPE_TABLE = "recipe";
  private static final String RECIPE_CATEGORY_TABLE = "recipe_category";
  private static final String STEP_TABLE = "step";
  private static final String UNIT_TABLE = "unit";

  /**
   * Returns the list of ingredients for a recipe, given the recipe ID. Note
   * that the Connection object is supplied, meaning that this method runs on
   * the current transaction that was started by the calling method.
   * 
   * @param conn The connection with a transaction underway.
   * @param recipeId The recipe ID of the recipe for which indredients are
   *        returned.
   * @return A list of ingredients for the recipe.
   * @throws SQLException Thrown if an error occurs.
   */
  private List<Ingredient> fetchRecipeIngredients(Connection conn,
      Integer recipeId) throws SQLException {
    /*
     * In the SQL, notice the left outer join between the ingredient table and
     * the unit table. Units are things like "tablespoon", etc. Not all
     * ingredients have units (like "salt to taste"). If an inner join is used,
     * ingredients without units will not be returned. This uses a left outer
     * join so that all the ingredients (the left table) will be returned.
     * Matching units (from the right table) are returned. If there is no
     * matching row (i.e., the ingredient does not have a unit), nulls are
     * returned in the unit columns. The ingredient table is the left table
     * because it is declared first (to the left of the unit table if the SQL
     * statement was stretched out in a long line).
     */
    // @formatter:off
    String sql = ""
        + "SELECT i.*, u.unit_name_singular, u.unit_name_plural "
        + "FROM " + INGREDIENT_TABLE + " i "
        + "LEFT JOIN " + UNIT_TABLE + " u USING (unit_id) "
        + "WHERE i.recipe_id = ? "
        + "ORDER BY i.ingredient_order";
    // @formatter:on

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      setParameter(stmt, 1, recipeId, Integer.class);

      try (ResultSet rs = stmt.executeQuery()) {
        List<Ingredient> ingredients = new LinkedList<>();

        while (rs.next()) {
          /*
           * The extract method matches column names in the result set with
           * instance names in the Java object. Since there are two unit columns
           * in the result set, extract will put them into a Unit object along
           * with the columns that match fields in the Ingredient object. If the
           * extract method is not used, you would have to manually load the
           * objects as shown below:
           */
          // Unit unit = new Unit();
          // unit.setUnitNamePlural(rs.getString("unit_name_plural"));
          // unit.setUnitNameSingular(rs.getString("unit_name_singular"));
          //
          // Ingredient ingredient = new Ingredient();
          // ingredient.setAmount(rs.getBigDecimal("amount"));
          // ingredient.setIngredientId(rs.getObject("ingredient_id",
          // Integer.class));
          // ingredient.setIngredientName(rs.getString("ingredient_name"));
          // ingredient.setIngredientOrder(rs.getObject("ingredient_order",
          // Integer.class));
          // ingredient.setInstruction(rs.getString("instruction"));
          // ingredient.setRecipeId(rs.getObject("recipe_id", Integer.class));
          // ingredient.setUnit(unit);

          Unit unit = extract(rs, Unit.class);
          Ingredient ingredient = extract(rs, Ingredient.class);

          ingredient.setUnit(unit);
          ingredients.add(ingredient);
        }

        return ingredients;
      }
    }
  }

  /**
   * This method returns all the steps for the recipe with the given ID. The
   * connection object is supplied, which means that this method runs in the
   * current transaction.
   * 
   * @param conn The Connection object.
   * @param recipeId The ID of the recipe for which to return the steps.
   * @return The recipe steps ordered by step number.
   * @throws SQLException Thrown if an error occurs.
   */
  private List<Step> fetchRecipeSteps(Connection conn, Integer recipeId)
      throws SQLException {
    String sql = "SELECT * FROM " + STEP_TABLE + " s WHERE s.recipe_id = ? "
        + "ORDER BY s.step_order";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      setParameter(stmt, 1, recipeId, Integer.class);

      try (ResultSet rs = stmt.executeQuery()) {
        List<Step> steps = new LinkedList<>();

        while (rs.next()) {
          /*
           * If the convenience method, extract, is not used, you would need to
           * load all the columns into a Step object manually by pulling them
           * from the result set.
           */
          steps.add(extract(rs, Step.class));
        }

        return steps;
      }
    }
  }

  /**
   * This method obtains a connection, then starts a transaction. Once the
   * transaction has been started, this method calls
   * {@link #fetchRecipeSteps(Connection, Integer)}.
   * 
   * @param recipeId The ID of the recipe for which to retrieve the steps.
   * @return The list of steps.
   */
  public List<Step> fetchRecipeSteps(Integer recipeId) {
    try (Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);

      try {
        List<Step> steps = fetchRecipeSteps(conn, recipeId);
        commitTransaction(conn);

        return steps;
      } catch (Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  /**
   * This method returns all the categories for the recipe with the given recipe
   * ID. The Connection object is supplied by the caller so that this method can
   * run under a current transaction.
   * 
   * The SQL statement uses an inner join to match the recipe ID in the
   * recipe_category join table with categories in the category table.
   * 
   * @param conn The connection object.
   * @param recipeId The recipe ID that matches the returned categories.
   * @return The categories that the recipe "belongs" to.
   * @throws SQLException Thrown if an error occurs.
   */
  private List<Category> fetchRecipeCategories(Connection conn,
      Integer recipeId) throws SQLException {
    // @formatter:off
    String sql = ""
        + "SELECT c.category_id, c.category_name "
        + "FROM " + RECIPE_CATEGORY_TABLE + " rc "
        + "JOIN " + CATEGORY_TABLE + " c USING (category_id) "
        + "WHERE rc.recipe_id = ?";
    // @formatter:on

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      setParameter(stmt, 1, recipeId, Integer.class);

      try (ResultSet rs = stmt.executeQuery()) {
        List<Category> categories = new LinkedList<>();

        while (rs.next()) {
          /*
           * If the convenience method, extract, isn't used, you would need to
           * create the Category objects and load them from the result set
           * manually.
           */
          categories.add(extract(rs, Category.class));
        }

        return categories;
      }
    }
  }

  /**
   * This method returns all recipes. The Recipe objects do not include
   * ingredients, steps, or categories.
   * 
   * @return The list of recipes.
   */
  public List<Recipe> fetchAllRecipes() {
    String sql = "SELECT * FROM " + RECIPE_TABLE + " ORDER BY recipe_name";

    try (Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);

      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        try (ResultSet rs = stmt.executeQuery()) {
          List<Recipe> recipes = new LinkedList<>();

          while (rs.next()) {
            /*
             * If the convenience method, extract, is not used, you would need
             * to manually create the Recipe objects and populate them from the
             * result set.
             */
            recipes.add(extract(rs, Recipe.class));
          }

          return recipes;
        }
      } catch (Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  /**
   * Insert a recipe into the recipe table. This uses a
   * {@link PreparedStatement} so that typed parameters can be passed into the
   * statement, allowing validation and safety checks to be performed. This will
   * mitigate against SQLInjection attacks.
   * 
   * @param recipe The recipe to be inserted.
   * @return The recipe with the primary key value.
   */
  public Recipe insertRecipe(Recipe recipe) {
    /*
     * Note that the primary key (recipe_id) is not included in the list of
     * fields in the insert statement. MySQL will set the correct primary key
     * value when the row is inserted.
     */
    // @formatter:off
    String sql = ""
        + "INSERT INTO " + RECIPE_TABLE + " "
        + "(recipe_name, notes, num_servings, prep_time, cook_time) "
        + "VALUES "
        + "(?, ?, ?, ?, ?)";
    // @formatter:on

    try (Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);

      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        setParameter(stmt, 1, recipe.getRecipeName(), String.class);
        setParameter(stmt, 2, recipe.getNotes(), String.class);
        setParameter(stmt, 3, recipe.getNumServings(), Integer.class);
        setParameter(stmt, 4, recipe.getPrepTime(), LocalTime.class);
        setParameter(stmt, 5, recipe.getCookTime(), LocalTime.class);

        /*
         * Insert the row. Statement.executeUpdate() performs inserts,
         * deletions, and modifications. It does all operations that do not
         * return result sets.
         */
        stmt.executeUpdate();

        /*
         * Call a method in the base class to get the last insert ID (primary
         * key value) in the given table.
         */
        Integer recipeId = getLastInsertId(conn, RECIPE_TABLE);

        commitTransaction(conn);

        /*
         * Set the recipe ID primary key to the value obtained by
         * getLastInsertId(). This does not fill in the createdAt field. To get
         * that value we would need to do a fetch on the recipe row.
         */
        recipe.setRecipeId(recipeId);
        return recipe;
      } catch (Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  /**
   * This method takes a list of SQL statements, which will be executed as a
   * batch.
   * 
   * @param sqlBatch A list of SQL statements that are executed in order.
   */
  public void executeBatch(List<String> sqlBatch) {
    try (Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);

      try (Statement stmt = conn.createStatement()) {
        /*
         * Add each SQL line to the Statement so they can be executed as a
         * batch.
         */
        for (String sql : sqlBatch) {
          stmt.addBatch(sql);
        }

        stmt.executeBatch();
        commitTransaction(conn);

      } catch (Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  /**
   * This method returns a recipe that matches the given recipe ID. All
   * ingredients, steps, and categories are returned. The recipe is returned in
   * an Optional object, which will be empty if there is no recipe in the table
   * with the given ID.
   * 
   * @param recipeId The recipe ID of the recipe to return.
   * @return An Optional object with the embedded Recipe Object, if a matching
   *         recipe is found. Otherwise, an empty Optional is returned.
   */
  public Optional<Recipe> fetchRecipeById(Integer recipeId) {
    String sql = "SELECT * FROM " + RECIPE_TABLE + " WHERE recipe_id = ?";

    try (Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);

      /*
       * This outer try block allows the ingredients, steps, and categories to
       * be returned within the current transaction. By wrapping all the
       * database calls within this try/catch block, the transaction can be
       * rolled back if any database operation fails.
       */
      try {
        Recipe recipe = null;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
          setParameter(stmt, 1, recipeId, Integer.class);

          try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
              recipe = extract(rs, Recipe.class);
            }
          }
        }

        /*
         * Technically, we don't need the null check because if the recipe isn't
         * found there will be no matching ingredients, steps, or categories.
         * This will cause the fetch methods to return an empty list. However,
         * the null check is performed to avoid three unnecessary database
         * calls.
         */
        if (Objects.nonNull(recipe)) {
          /*
           * Ingredients, steps, and categories are fetched under the current
           * transaction.
           */
          recipe.getIngredients()
              .addAll(fetchRecipeIngredients(conn, recipeId));

          recipe.getSteps().addAll(fetchRecipeSteps(conn, recipeId));
          recipe.getCategories().addAll(fetchRecipeCategories(conn, recipeId));
        }

        commitTransaction(conn);

        /*
         * Call Optional.ofNullable because the recipe variable will be null if
         * the recipe isn't found. If Optional.of is used instead, a
         * NullPointerException is thrown if recipe is null.
         */
        return Optional.ofNullable(recipe);

      } catch (Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  /**
   * This method retrieves all units from the unit table and orders them by the
   * unit name.
   * 
   * @return The list of units.
   */
  public List<Unit> fetchAllUnits() {
    String sql = "SELECT * FROM " + UNIT_TABLE + " ORDER BY unit_name_singular";

    try (Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);

      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        try (ResultSet rs = stmt.executeQuery()) {
          List<Unit> units = new LinkedList<>();

          while (rs.next()) {
            units.add(extract(rs, Unit.class));

            /* To use straight JDBC method calls, do this: */
            // Unit unit = new Unit();
            // unit.setUnitId(rs.getObject("unit_id", Integer.class));
            // unit.setUnitNamePlural(rs.getString("unit_name_plural"));
            // unit.setUnitNameSingular(rs.getString("unit_name_singular"));
            // units.add(unit);
          }

          return units;
        }
      } catch (Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  /**
   * This method uses the JDBC API to add an ingredient to a recipe.
   * 
   * @param ingredient The ingredient to add.
   */
  public void addIngredientToRecipe(Ingredient ingredient) {
    String sql = "INSERT INTO " + INGREDIENT_TABLE
        + " (recipe_id, unit_id, ingredient_name, instruction, ingredient_order, amount) "
        + "VALUES (?, ?, ?, ?, ?, ?)";

    try (Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);

      /*
       * Create a try/catch block so that the ingredient order can be obtained
       * inside the current transaction.
       */
      try {
        /*
         * The ingredient order is simply obtained by finding out how many
         * ingredients currently exist in the recipe and adding one to it. This
         * is not the most elegant approach as it doesn't allow for inserting
         * ingredients or reordering an ingredient.
         */
        Integer order = getNextSequenceNumber(conn, ingredient.getRecipeId(),
            INGREDIENT_TABLE, "recipe_id");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
          setParameter(stmt, 1, ingredient.getRecipeId(), Integer.class);
          setParameter(stmt, 2, ingredient.getUnit().getUnitId(),
              Integer.class);
          setParameter(stmt, 3, ingredient.getIngredientName(), String.class);
          setParameter(stmt, 4, ingredient.getInstruction(), String.class);
          setParameter(stmt, 5, order, Integer.class);
          setParameter(stmt, 6, ingredient.getAmount(), BigDecimal.class);

          /* The executeUpdate method handles every action except queries. */
          stmt.executeUpdate();
          commitTransaction(conn);
        }
      } catch (Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  /**
   * Add a step to the recipe. The recipe ID is supplied in the step object.
   * 
   * @param step The step to add.
   */
  public void addStepToRecipe(Step step) {
    String sql = "INSERT INTO " + STEP_TABLE
        + " (recipe_id, step_order, step_text)" + " VALUES (?, ?, ?)";

    try (Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);

      /*
       * Like the ingredient, this calls getNextSequenceNumber to find out how
       * many steps there currently are in the recipe and adds one to it.
       */
      Integer order = getNextSequenceNumber(conn, step.getRecipeId(),
          STEP_TABLE, "recipe_id");

      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        setParameter(stmt, 1, step.getRecipeId(), Integer.class);
        setParameter(stmt, 2, order, Integer.class);
        setParameter(stmt, 3, step.getStepText(), String.class);

        stmt.executeUpdate();
        commitTransaction(conn);
      } catch (Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  /**
   * This method retrieves all the categories ordered by the category name.
   * 
   * @return The list of categories.
   */
  public List<Category> fetchAllCategories() {
    String sql = "SELECT * FROM " + CATEGORY_TABLE + " ORDER BY category_name";

    try (Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);

      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        try (ResultSet rs = stmt.executeQuery()) {
          List<Category> categories = new LinkedList<>();

          while (rs.next()) {
            /*
             * If you don't want to use the extract method you can use straight
             * JDBC calls.
             */
            categories.add(extract(rs, Category.class));
          }

          return categories;
        }
      } catch (Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  /**
   * Add the category to the recipe with the given recipe ID. The category name
   * is given but the category ID is required. A subquery is used to return the
   * category ID given the category name. The category ID is then inserted into
   * the recipe_category join table.
   * 
   * @param recipeId The recipe ID.
   * @param category The category name.
   */
  public void addCategoryToRecipe(Integer recipeId, String category) {
    /*
     * Subqueries are surrounded by parentheses. A subquery can be substituted
     * for any (or nearly any) value in the INSERT statement. You could perform
     * a separate query for the category ID given the category name, then do the
     * insert once you had the category ID. Using a subquery allows us to do it
     * with a single statement.
     */
    String subQuery = "(SELECT category_id FROM " + CATEGORY_TABLE
        + " WHERE category_name = ?)";

    String sql = "INSERT INTO " + RECIPE_CATEGORY_TABLE
        + " (recipe_id, category_id) VALUES (?, " + subQuery + ")";

    /* Subqueries are handled just like everything else. */
    try (Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);

      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        setParameter(stmt, 1, recipeId, Integer.class);
        setParameter(stmt, 2, category, String.class);

        /*
         * With a subquery in an INSERT statement you do not call executeQuery.
         * Instead, it is treated just like a normal INSERT.
         */
        stmt.executeUpdate();
        commitTransaction(conn);
      } catch (Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  /**
   * This method modifies the recipe step text using JDBC calls. It does not
   * change the step ID, recipe ID, or step order - just the step text.
   * 
   * @param step The step to modify.
   * @return {@code true} if the step was actually modified. {@code false} if
   *         the step does not exist. This works because executeUpdate returns
   *         an integer that indicates the number of rows changed. If it returns
   *         one, a single step was modified and the method returns
   *         {@code true}. If executeUpdate returns zero, the step was not
   *         found. It will never return a value greater than one because the
   *         WHERE clause constrains the update to a single row. This is because
   *         the WHERE clause uses the primary key value, step_id. A primary key
   *         by definition, only applies to a single row.
   */

  public boolean modifyRecipeStep(Step step) {
    String sql =
        "UPDATE " + STEP_TABLE + " SET step_text = ? WHERE step_id = ?";

    try (Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);

      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        setParameter(stmt, 1, step.getStepText(), String.class);
        setParameter(stmt, 2, step.getStepId(), Integer.class);

        /*
         * The step was updated successfully if the return value from
         * executeUpdate equals 1.
         */
        boolean updated = stmt.executeUpdate() == 1;
        commitTransaction(conn);

        return updated;
      } catch (Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  /**
   * This method deletes a recipe from the recipe table if the recipe ID is
   * valid. If the recipe is deleted, all child records (ingredients, steps and
   * categories) are deleted as well because the foreign keys were created
   * specifying ON DELETE CASCADE.
   * 
   * As with {@link #modifyRecipeStep(Step)}, this method makes use of the fact
   * that executeUpdate returns the number of rows changed. If a single row was
   * changed, the recipe was deleted successfully. (executeUpdate does not
   * report the number of child rows deleted by ON DELETE CASCADE.) If
   * executeUpdate returns zero, the recipe ID is invalid.
   * 
   * @param recipeId
   * @return
   */
  public boolean deleteRecipe(Integer recipeId) {
    String sql = "DELETE FROM " + RECIPE_TABLE + " WHERE recipe_id = ?";;

    try (Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);

      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        setParameter(stmt, 1, recipeId, Integer.class);

        boolean deleted = stmt.executeUpdate() == 1;

        commitTransaction(conn);
        return deleted;
      } catch (Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }
}