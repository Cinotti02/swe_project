import CLI.AuthCLI;
import CLI.CustomerCLI;
import CLI.OwnerCLI;
import CLI.StaffCLI;
import Controller.AuthController;
import Controller.CustomerController;
import Controller.CustomerProfileController;
import Controller.OwnerController;
import Controller.StaffController;
import ORM.*;
import ServiceLayer.*;

import java.util.Scanner;

public final class AppBootstrap {

    private AppBootstrap() {
    }

    public static Components build(Scanner scanner) {
        UserDAO userDAO = new UserDAO();
        CategoryDAO categoryDAO = new CategoryDAO();
        DishDAO dishDAO = new DishDAO();
        TableDAO tableDAO = new TableDAO();
        SlotDAO slotDAO = new SlotDAO();
        ReservationDAO reservationDAO = new ReservationDAO();
        NotificationDAO notificationDAO = new NotificationDAO();
        OrderDAO orderDAO = new OrderDAO();
        OrderItemDAO orderItemDAO = new OrderItemDAO();

        AuthService authService = new AuthService(userDAO);
        AuthController authController = new AuthController(authService);

        MenuQueryService menuQueryService = new MenuQueryService(dishDAO, categoryDAO);
        CartService cartService = new CartService();
        OrderService orderService = new OrderService(orderDAO, orderItemDAO);
        TableAllocationService tableAllocationService = new TableAllocationService();
        ReservationService reservationService = new ReservationService(
                reservationDAO,
                tableDAO,
                slotDAO,
                notificationDAO,
                tableAllocationService);
        OwnerAdminService ownerAdminService = new OwnerAdminService(dishDAO, categoryDAO, tableDAO, slotDAO);
        ProfileService profileService = new ProfileService(userDAO);
        StaffOperationService staffOperationService = new StaffOperationService(orderDAO, reservationService, notificationDAO);

        CustomerController customerController = new CustomerController(
                menuQueryService,
                cartService,
                orderService,
                reservationService);
        CustomerProfileController customerProfileController = new CustomerProfileController(profileService);
        OwnerController ownerController = new OwnerController(ownerAdminService, menuQueryService);
        StaffController staffController = new StaffController(staffOperationService);

        AuthCLI authCLI = new AuthCLI(authController, scanner);
        CustomerCLI customerCLI = new CustomerCLI(customerController, customerProfileController, scanner);
        OwnerCLI ownerCLI = new OwnerCLI(ownerController, scanner);
        StaffCLI staffCLI = new StaffCLI(staffController, scanner);

        return new Components(authCLI, customerCLI, ownerCLI, staffCLI);
    }

    public record Components(AuthCLI authCLI,
                             CustomerCLI customerCLI,
                             OwnerCLI ownerCLI,
                             StaffCLI staffCLI) {
    }
}