package ServiceLayer;

import DomainModel.menu.Category;
import DomainModel.menu.Dish;
import DomainModel.search.DishSearchParameters;
import ORM.CategoryDAO;
import ORM.DishDAO;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MenuQueryService {

    private final DishDAO dishDAO;
    private final CategoryDAO categoryDAO;

    public MenuQueryService(DishDAO dishDAO, CategoryDAO categoryDAO) {
        this.dishDAO = dishDAO;
        this.categoryDAO = categoryDAO;
    }

    public Map<Category, List<Dish>> buildMenu(boolean onlyActiveCategories,
                                               boolean onlyAvailableDishes) throws SQLException {
        List<Category> categories = onlyActiveCategories
                ? categoryDAO.getActiveCategories()
                : categoryDAO.getAllCategories();

        Map<Category, List<Dish>> menu = new LinkedHashMap<>();
        for (Category category : categories) {
            List<Dish> dishes = dishDAO.getDishesByCategory(category.getId());
            if (onlyAvailableDishes) {
                dishes = dishes.stream()
                        .filter(Dish::isAvailable)
                        .collect(Collectors.toList());
            }
            menu.put(category, dishes);
        }
        return menu;
    }

    public List<Category> listCategories(boolean onlyActive) throws SQLException {
        return onlyActive ? categoryDAO.getActiveCategories() : categoryDAO.getAllCategories();
    }

    public List<Dish> listDishesByCategory(int categoryId, boolean onlyAvailable) throws SQLException {
        List<Dish> dishes = dishDAO.getDishesByCategory(categoryId);
        if (onlyAvailable) {
            return dishes.stream()
                    .filter(Dish::isAvailable)
                    .collect(Collectors.toList());
        }
        return dishes;
    }

    public List<Dish> listAllDishes(boolean onlyAvailable) throws SQLException {
        List<Dish> dishes = dishDAO.getAllDishes();
        if (onlyAvailable) {
            return dishes.stream()
                    .filter(Dish::isAvailable)
                    .collect(Collectors.toList());
        }
        return dishes;
    }

    public Optional<Dish> findDishById(int dishId) throws SQLException {
        return dishDAO.getDishById(dishId);
    }

    public List<Dish> searchDishes(String query, boolean onlyAvailable) throws SQLException {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        DishSearchParameters params = DishSearchParameters.builder()
                .setNameContains(query)
                .setOnlyAvailable(onlyAvailable);
        return dishDAO.searchDishes(params);
    }

    public List<Dish> searchDishes(DishSearchParameters params) throws SQLException {
        return dishDAO.searchDishes(params);
    }
}