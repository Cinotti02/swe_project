package test.ServiceLayer;

import DomainModel.menu.Category;
import DomainModel.menu.Dish;
import DomainModel.search.DishSearchParameters;
import ORM.CategoryDAO;
import ORM.DishDAO;
import ServiceLayer.MenuQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MenuQueryServiceTest {

    private FakeDishDAO dishDAO;
    private FakeCategoryDAO categoryDAO;
    private MenuQueryService service;
    private Category category;
    private Dish available;
    private Dish unavailable;

    @BeforeEach
    void setUp() {
        dishDAO = new FakeDishDAO();
        categoryDAO = new FakeCategoryDAO();
        service = new MenuQueryService(dishDAO, categoryDAO);
        category = new Category("Pizze", "Pizze");
        category.setId(2);
        available = dish(1, "Margherita", true);
        unavailable = dish(2, "Stagionale", false);
        categoryDAO.categories = List.of(category);
        dishDAO.dishes = List.of(available, unavailable);
    }

    @Test
    void buildMenuKeepsCategoryOrderAndFiltersUnavailableDishes() throws Exception {
        Map<Category, List<Dish>> menu = service.buildMenu(true, true);
        assertEquals(List.of(category), menu.keySet().stream().toList());
        assertEquals(List.of(available), menu.get(category));
        assertTrue(categoryDAO.activeQueryUsed);
    }

    @Test
    void listMethodsRespectAvailabilityFlag() throws Exception {
        assertEquals(List.of(available), service.listAllDishes(true));
        assertEquals(List.of(available, unavailable), service.listAllDishes(false));
        assertEquals(List.of(available), service.listDishesByCategory(2, true));
    }

    @Test
    void blankSearchReturnsEmptyWithoutCallingDao() throws Exception {
        assertTrue(service.searchDishes(" ", true).isEmpty());
        assertNull(dishDAO.lastSearch);
    }

    @Test
    void textSearchBuildsExpectedCriteria() throws Exception {
        service.searchDishes("pizza", true);
        assertEquals("pizza", dishDAO.lastSearch.getNameContains().orElseThrow());
        assertTrue(dishDAO.lastSearch.getOnlyAvailable().orElseThrow());
    }

    @Test
    void findDishDelegatesToDao() throws Exception {
        dishDAO.found = available;
        assertSame(available, service.findDishById(1).orElseThrow());
    }

    private Dish dish(int id, String name, boolean active) {
        Dish dish = new Dish(name, "Descrizione", 8.0, category);
        dish.setId(id);
        dish.setAvailable(active);
        return dish;
    }

    private static class FakeDishDAO extends DishDAO {
        private List<Dish> dishes = List.of();
        private Dish found;
        private DishSearchParameters lastSearch;

        @Override
        public List<Dish> getDishesByCategory(int categoryId) {
            return dishes;
        }

        @Override
        public List<Dish> getAllDishes() {
            return dishes;
        }

        @Override
        public Optional<Dish> getDishById(int id) {
            return Optional.ofNullable(found);
        }

        @Override
        public List<Dish> searchDishes(DishSearchParameters params) {
            lastSearch = params;
            return dishes;
        }
    }

    private static class FakeCategoryDAO extends CategoryDAO {
        private List<Category> categories = List.of();
        private boolean activeQueryUsed;

        @Override
        public List<Category> getActiveCategories() {
            activeQueryUsed = true;
            return categories;
        }

        @Override
        public List<Category> getAllCategories() {
            return categories;
        }
    }
}
