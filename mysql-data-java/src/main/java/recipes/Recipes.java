package recipes;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import recipes.entity.Category;
import recipes.entity.Ingredient;
import recipes.entity.Recipe;
import recipes.entity.Step;
import recipes.entity.Unit;
import recipes.exception.DbException;
import recipes.service.RecipeService;

/**
 * This class contains the "main" method - the entry point to a Java
 * application.
 * 
 * @author Promineo
 *
 */
public class Recipes {
  private Scanner scanner = new Scanner(System.in);
  private RecipeService recipeService = new RecipeService();
  private Recipe curRecipe;

  /* This is the list of available operations */
  // @formatter:off
  private List<String> operations = List.of(
      "1) Create and populate all tables",
      "2) Add a recipe",
      "3) List recipes",
      "4) Select current recipe",
      "5) Add ingredient to current recipe",
      "6) Add step to current recipe",
      "7) Add category to current recipe",
      "8) Modify step in current recipe",
      "9) Delete recipe"
  );
  // @formatter:on

  /**
   * Entry point for a Java application.
   * 
   * @param args Command line arguments. Ignored.
   */
  public static void main(String[] args) {
    new Recipes().displayMenu();
  }

  /**
   * This method displays the menu selections (available operations), gets the
   * user menu selection, and acts on that selection.
   */
  private void displayMenu() {
    boolean done = false;

    while (!done) {
      try {
        int operation = getOperation();

        switch (operation) {
          case -1:
            done = exitMenu();
            break;

          case 1:
            createTables();
            break;

          case 2:
            addRecipe();
            break;

          case 3:
            listRecipes();
            break;

          case 4:
            setCurrentRecipe();
            break;

          case 5:
            addIngredientToCurrentRecipe();
            break;

          case 6:
            addStepToCurrentRecipe();
            break;

          case 7:
            addCategoryToCurrentRecipe();
            break;

          case 8:
            modifyStepInCurrentRecipe();
            break;

          case 9:
            deleteRecipe();
            break;

          default:
            System.out.println("\n" + operation + " is not valid. Try again.");
            break;
        }
      } catch (Exception e) {
        System.out.println("\nError: " + e.toString() + " Try again.");
      }
    }
  }

  /**
   * This method deletes a recipe. First, it displays a list of recipes and
   * allows the user to select a recipe ID. Then, it calls the recipe service to
   * delete the recipe. If the recipe isn't found, the service throws an
   * unchecked exception, which gets picked up by the exception handler in
   * {@link #displayMenu()}.
   */
  private void deleteRecipe() {
    listRecipes();
    Integer recipeId = getIntInput("Enter the ID of the recipe to delete");

    if (Objects.nonNull(recipeId)) {
      recipeService.deleteRecipe(recipeId);

      System.out.println("You have deleted recipe " + recipeId);

      /*
       * If the deleted recipe is the current recipe, reset the current recipe
       * to null.
       */
      if (Objects.nonNull(curRecipe)
          && curRecipe.getRecipeId().equals(recipeId)) {
        curRecipe = null;
      }
    }
  }

  /**
   * This method prints the steps for the current recipe and allows the user to
   * modify a step.
   */
  private void modifyStepInCurrentRecipe() {
    if (Objects.isNull(curRecipe)) {
      System.out.println("\nPlease select a recipe first.");
      return;
    }

    List<Step> steps = recipeService.fetchSteps(curRecipe.getRecipeId());

    /* Print the recipe steps. */
    System.out.println("\nSteps for current recipe");
    steps.forEach(step -> System.out.println("   " + step));

    /* Get the ID of the step to modify. */
    Integer stepId = getIntInput("Enter step ID of step to modify");

    if (Objects.nonNull(stepId)) {
      String stepText = getStringInput("Enter new step text");

      if (Objects.nonNull(stepText)) {
        Step step = new Step();

        step.setStepId(stepId);
        step.setStepText(stepText);

        /*
         * Call the recipe service to modify the step text. We don't want to
         * change the step ID or the step order, so the only column that is
         * modified is the step text.
         */
        recipeService.modifyStep(step);

        /*
         * Call fetchRecipeById() to re-fetch the recipe, which will show the
         * newly modified step.
         */
        curRecipe = recipeService.fetchRecipeById(curRecipe.getRecipeId());
      }
    }
  }

  /**
   * This method prints all the category names, then collects a category name
   * from the user. The category name will be used in a subquery to get the
   * category ID, which is used to add a recipe_category recipe. A category is
   * associated with a recipe by adding an entry in the join table,
   * recipe_category. The recipe_category row has two foreign key values. One
   * points to the current recipe (the recipe_id is the same as the current
   * recipe's recipe_id) and the other foreign key points to the selected
   * category row.
   */
  private void addCategoryToCurrentRecipe() {
    if (Objects.isNull(curRecipe)) {
      System.out.println("\nPlease select a recipe first.");
      return;
    }

    /* Retrieve and print the categories. */
    List<Category> categories = recipeService.fetchCategories();

    categories.forEach(
        category -> System.out.println("   " + category.getCategoryName()));

    /* Get the category name of the category to add to the recipe. */
    String category = getStringInput("Enter the category to add");

    /*
     * If the user did not enter a new category name just go back into the menu.
     */
    if (Objects.nonNull(category)) {
      recipeService.addCategoryToRecipe(curRecipe.getRecipeId(), category);

      /*
       * Retrieve the current recipe again so that the newly added category is
       * displayed.
       */
      curRecipe = recipeService.fetchRecipeById(curRecipe.getRecipeId());
    }
  }

  /**
   * This method adds a step to the current recipe. Steps are added in order
   * with the step order maintained by the DAO. There is no ability to change
   * step order or delete a step so the application isn't as robust as it could
   * be.
   */
  private void addStepToCurrentRecipe() {
    if (Objects.isNull(curRecipe)) {
      System.out.println("\nPlease select a recipe first.");
      return;
    }

    /*
     * The only input needed from the user is the step text. MySQL manages the
     * step ID (primary key) and the step order is calculated to be one more
     * than the current number of steps. The recipe ID is supplied by the
     * current step object.
     */
    String stepText = getStringInput("Enter the step text");

    if (Objects.nonNull(stepText)) {
      Step step = new Step();

      step.setRecipeId(curRecipe.getRecipeId());
      step.setStepText(stepText);

      /* Call the recipe service to add the step. */
      recipeService.addStep(step);

      /*
       * Retrieve the current recipe again so that the newly added step is
       * reflected in the display.
       */
      curRecipe = recipeService.fetchRecipeById(step.getRecipeId());
    }
  }

  /**
   * Collect the ingredient information from the user to add a new ingredient to
   * the current recipe.
   */
  private void addIngredientToCurrentRecipe() {
    if (Objects.isNull(curRecipe)) {
      System.out.println("\nPlease select a recipe first.");
      return;
    }

    /* Collect the ingredient data from the user. */
    String name = getStringInput("Enter the ingredient name");
    String instruction =
        getStringInput("Enter an instruction if any (like \"finely chopped\")");
    Double inputAmount =
        getDoubleInput("Enter the ingredient amount (like .25)");
    List<Unit> units = recipeService.fetchUnits();

    /*
     * Create a BigDecimal object from the Double value collected from the user.
     * BigDecimal is an immutable object. Once a value is assigned it can never
     * be changed. The setScale method sets the number of decimal places in the
     * BigDecimal object.
     */
    BigDecimal amount = Objects.isNull(inputAmount) ? null
        : new BigDecimal(inputAmount).setScale(2);

    /*
     * Print all the units, then get the unit ID to add to the ingredient from
     * the user.
     */
    System.out.println("Units:");

    units.forEach(unit -> System.out.println("   " + unit.getUnitId() + ": "
        + unit.getUnitNameSingular() + " (" + unit.getUnitNamePlural() + ")"));

    Integer unitId = getIntInput("Enter a unit ID (press Enter for none)");

    /* Create a new Unit object with only the unit ID set. */
    Unit unit = new Unit();
    unit.setUnitId(unitId);

    /* Create a new Ingredient from the user input. */
    Ingredient ingredient = new Ingredient();

    ingredient.setRecipeId(curRecipe.getRecipeId());
    ingredient.setUnit(unit);
    ingredient.setIngredientName(name);
    ingredient.setInstruction(instruction);
    ingredient.setAmount(amount);

    /*
     * Add the ingredient to the ingredient table. The recipe_id associates it
     * with the current recipe.
     */
    recipeService.addIngredient(ingredient);

    /*
     * Re-read the current recipe so that the newly added ingredient is
     * displayed.
     */
    curRecipe = recipeService.fetchRecipeById(ingredient.getRecipeId());
  }

  /**
   * Set and display the current recipe. This method prints the list of recipes
   * and recipe IDs on the console, then asks the user to input a recipe ID to
   * select. If the recipe ID is in the list of recipes, the current recipe is
   * set. Otherwise an error message is printed.
   */
  private void setCurrentRecipe() {
    /* Print the list of recipes and return that list. */
    List<Recipe> recipes = listRecipes();

    /* Input the selected recipe ID. */
    Integer recipeId = getIntInput("Select a recipe ID");

    /* Unselect any currently selected recipe. */
    curRecipe = null;

    /*
     * Loop through the list of recipes trying to find the ID that matches what
     * the user entered.
     */
    for (Recipe recipe : recipes) {
      if (recipe.getRecipeId().equals(recipeId)) {
        curRecipe = recipeService.fetchRecipeById(recipeId);
        break;
      }
    }

    /*
     * If curRecipe is still null, the recipe ID entered by the user is not
     * valid. Print an error message in this case and leave the current recipe
     * unselected.
     */
    if (Objects.isNull(curRecipe)) {
      System.out.println("\nInvalid recipe selected.");
    }
  }

  /**
   * Fetch the list of recipes, print the recipe IDs and names on the console,
   * and return the list.
   * 
   * @return The list of recipes
   */
  private List<Recipe> listRecipes() {
    List<Recipe> recipes = recipeService.fetchRecipes();

    System.out.println("\nRecipes:");

    /* Print the list of recipes using a Lambda expression. */
    recipes.forEach(recipe -> System.out
        .println("   " + recipe.getRecipeId() + ": " + recipe.getRecipeName()));

    /* This will print the list of recipes using an enhanced for loop. */
    // for (Recipe recipe : recipes) {
    // System.out.println(
    // " " + recipe.getRecipeId() + ": " + recipe.getRecipeName());
    // }

    return recipes;
  }

  /**
   * Add a recipe (without ingredients, steps, or categories).
   */
  private void addRecipe() {
    /* Get the recipe input from the user. */
    String name = getStringInput("Enter the recipe name");
    String notes = getStringInput("Enter the recipe notes");
    Integer numServings = getIntInput("Enter number of servings");
    Integer prepMinutes = getIntInput("Enter prep time in minutes");
    Integer cookMinutes = getIntInput("Enter cook time in minutes");

    LocalTime prepTime = minutesToLocalTime(prepMinutes);
    LocalTime cookTime = minutesToLocalTime(cookMinutes);

    /* Create a recipe object from the user input. */
    Recipe recipe = new Recipe();

    recipe.setRecipeName(name);
    recipe.setNotes(notes);
    recipe.setNumServings(numServings);
    recipe.setPrepTime(prepTime);
    recipe.setCookTime(cookTime);

    /*
     * Add the recipe to the recipe table. This will throw an unchecked
     * exception if there is an error. The exception is picked up by the
     * exception handler in displayMenu(). This keeps the code very clean and
     * readable.
     */
    Recipe dbRecipe = recipeService.addRecipe(recipe);
    System.out.println("You added this recipe:\n" + dbRecipe);

    /* Set the current recipe to the newly entered recipe. */
    curRecipe = recipeService.fetchRecipeById(dbRecipe.getRecipeId());
  }

  /**
   * Convert from an Integer minute value to a LocalTime object.
   * 
   * @param numMinutes The number of minutes. This may be {@code null}, in which
   *        case the number of minutes is set to zero.
   * @return A LocalTime object containing the number of hours and minutes.
   */
  private LocalTime minutesToLocalTime(Integer numMinutes) {
    int min = Objects.isNull(numMinutes) ? 0 : numMinutes;
    int hours = min / 60;
    int minutes = min % 60;

    return LocalTime.of(hours, minutes);
  }

  /**
   * This is called to programmatically drop all the tables, recreate them and
   * populate them with data. It resets the table data to a known, initial
   * state.
   */
  private void createTables() {
    recipeService.createAndPopulateTables();
    System.out.println("\nTables created and populated!");
  }

  /**
   * This is called when the user is exiting the menu. It simply returns
   * {@code true}, which will cause the menu to exit. This is not the best
   * approach, as there is no guarantee what the caller will do with the return
   * value. It does, however, keep the switch statement in the menu code
   * cleaner.
   * 
   * @return {@code true} to cause the menu to exit.
   */
  private boolean exitMenu() {
    System.out.println("\nExiting the menu. Bye🙋‍♂️");
    return true;
  }

  /**
   * This prints the available options and then asks the user to input a menu
   * selection. If the user enters nothing, -1 is returned, which will cause the
   * application to exit.
   * 
   * @return The user's menu selection, or -1 if the user pressed Enter without
   *         making a selection.
   */
  private int getOperation() {
    printOperations();

    Integer op =
        getIntInput("\nEnter an operation number (press Enter to quit)");

    return Objects.isNull(op) ? -1 : op;
  }

  /**
   * Print the available menu operations to the console, with each operation on
   * a separate line.
   */
  private void printOperations() {
    System.out.println();
    System.out.println("Here's what you can do:");

    /*
     * Every List has a forEach method. The forEach method takes a Consumer as a
     * parameter. This uses a Lambda expression to supply the Consumer. Consumer
     * is a functional interface that has a single abstract method accept. The
     * accept method returns nothing (void) and accepts a single parameter. This
     * Lambda expression returns nothing (System.out.println has a void return
     * value). It has a single parameter (op), which meets the requirements of
     * Consumer.accept().
     */
    operations.forEach(op -> System.out.println("   " + op));

    if (Objects.isNull(curRecipe)) {
      System.out.println("\nYou are not working with a recipe.");
    } else {
      System.out.println("\nYou are working with recipe " + curRecipe);
    }
  }

  /**
   * Get the user's input and convert it to an Integer.
   * 
   * @param prompt The prompt that is printed to the console.
   * @return This will return {@code null} if the user presses Enter without
   *         entering anything. It returns an Integer if the user enters a valid
   *         integer.
   * @throws DbException [Unchecked exception] Thrown if the input cannot be
   *         converted to an Integer.
   */
  private Integer getIntInput(String prompt) {
    String input = getStringInput(prompt);

    if (Objects.isNull(input)) {
      return null;
    }

    try {
      return Integer.parseInt(input);
    } catch (NumberFormatException e) {
      throw new DbException(input + " is not a valid number.");
    }
  }

  /**
   * Get the user's input and convert it to a Double.
   * 
   * @param prompt The prompt that is printed to the console.
   * @return This will return {@code null} if the user presses Enter without
   *         entering anything. It returns a Double if the user enters a valid
   *         Double.
   * @throws DbException [Unchecked exception] Thrown if the input cannot be
   *         converted to a Double.
   */
  private Double getDoubleInput(String prompt) {
    String input = getStringInput(prompt);

    if (Objects.isNull(input)) {
      return null;
    }

    try {
      return Double.parseDouble(input);
    } catch (NumberFormatException e) {
      throw new DbException(input + " is not a valid number.");
    }
  }

  /**
   * This prints the prompt to the console and collects the user's input. If the
   * user's input is all blank or empty, {@code null} is returned.
   * 
   * @param prompt The prompt that is printed to the console.
   * @return {@code null} if the input is empty or blank. Otherwise the trimmed
   *         input is returned.
   */
  private String getStringInput(String prompt) {
    System.out.print(prompt + ": ");
    String line = scanner.nextLine();

    return line.isBlank() ? null : line.trim();
  }

}