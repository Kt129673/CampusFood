package com.campusfood.config;

import com.campusfood.entity.Inventory;
import com.campusfood.entity.Product;
import com.campusfood.entity.User;
import com.campusfood.enums.UserRole;
import com.campusfood.repository.InventoryRepository;
import com.campusfood.repository.ProductRepository;
import com.campusfood.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds the database with sample products and users for development/testing.
 * Only runs if the database is empty.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (productRepository.count() > 0) {
            log.info("Database already seeded — skipping");
            return;
        }

        log.info("Seeding database with sample data...");
        seedUsers();
        seedProducts();
        log.info("Database seeding complete!");
    }

    private void seedUsers() {
        List<User> users = List.of(
                User.builder()
                        .name("Admin User")
                        .mobile("9999999999")
                        .email("admin@campusfood.com")
                        .passwordHash(Integer.toHexString("admin123".hashCode()))
                        .role(UserRole.ADMIN)
                        .build(),
                User.builder()
                        .name("Rahul Sharma")
                        .mobile("9876543210")
                        .email("rahul@campus.edu")
                        .passwordHash(Integer.toHexString("pass123".hashCode()))
                        .role(UserRole.CUSTOMER)
                        .build(),
                User.builder()
                        .name("Priya Patel")
                        .mobile("9876543211")
                        .email("priya@campus.edu")
                        .passwordHash(Integer.toHexString("pass123".hashCode()))
                        .role(UserRole.CUSTOMER)
                        .build(),
                User.builder()
                        .name("Delivery Boy 1")
                        .mobile("9111111111")
                        .email("delivery1@campusfood.com")
                        .passwordHash(Integer.toHexString("delivery123".hashCode()))
                        .role(UserRole.DELIVERY)
                        .build(),
                User.builder()
                        .name("Delivery Boy 2")
                        .mobile("9222222222")
                        .email("delivery2@campusfood.com")
                        .passwordHash(Integer.toHexString("delivery123".hashCode()))
                        .role(UserRole.DELIVERY)
                        .build()
        );

        userRepository.saveAll(users);
        log.info("Seeded {} users", users.size());
    }

    private void seedProducts() {
        List<ProductData> products = List.of(
                // 🍿 Snacks
                new ProductData("Maggi Noodles", "Classic Maggi masala noodles", "25.00", "Snacks", 50),
                new ProductData("Lays Classic Salted", "Crispy potato chips - classic salt", "20.00", "Snacks", 40),
                new ProductData("Kurkure Masala Munch", "Crunchy puffed corn snack", "20.00", "Snacks", 40),
                new ProductData("Biscuit Parle-G", "India's favorite glucose biscuit", "10.00", "Snacks", 100),
                new ProductData("Oreo Cookies", "Chocolate sandwich cookies with cream", "30.00", "Snacks", 30),
                new ProductData("Monaco Salted Biscuit", "Crispy salted crackers", "15.00", "Snacks", 35),

                // 🥤 Beverages
                new ProductData("Coca-Cola 250ml", "Chilled Coca-Cola can", "40.00", "Beverages", 60),
                new ProductData("Thumbs Up 250ml", "Chilled Thumbs Up can", "40.00", "Beverages", 50),
                new ProductData("Maaza Mango 250ml", "Mango fruit drink", "30.00", "Beverages", 45),
                new ProductData("Real Juice Mixed Fruit", "Mixed fruit juice 200ml", "25.00", "Beverages", 30),
                new ProductData("Bisleri Water 1L", "Packaged drinking water", "20.00", "Beverages", 100),
                new ProductData("Red Bull 250ml", "Energy drink", "125.00", "Beverages", 20),
                new ProductData("Amul Lassi 200ml", "Sweet mango lassi", "25.00", "Beverages", 25),

                // ☕ Hot Beverages
                new ProductData("Tea (Chai)", "Hot masala chai", "15.00", "Hot Beverages", 200),
                new ProductData("Coffee", "Hot filter coffee", "20.00", "Hot Beverages", 200),
                new ProductData("Hot Chocolate", "Rich hot chocolate drink", "40.00", "Hot Beverages", 30),

                // 🍔 Quick Bites
                new ProductData("Vada Pav", "Mumbai-style vada pav with chutney", "20.00", "Quick Bites", 30),
                new ProductData("Samosa (2 pcs)", "Crispy potato samosas", "20.00", "Quick Bites", 40),
                new ProductData("Bread Pakora", "Stuffed bread fritters", "25.00", "Quick Bites", 25),
                new ProductData("Sandwich - Veg", "Fresh vegetable grilled sandwich", "40.00", "Quick Bites", 20),
                new ProductData("Sandwich - Cheese", "Cheese grilled sandwich", "50.00", "Quick Bites", 15),
                new ProductData("Pav Bhaji", "Buttery pav with spiced bhaji", "50.00", "Quick Bites", 20),

                // 📦 Essentials
                new ProductData("Notebook (200 pages)", "Single-line ruled notebook", "40.00", "Essentials", 50),
                new ProductData("Pen (Blue)", "Ball-point pen - blue ink", "10.00", "Essentials", 100),
                new ProductData("Eraser", "Non-dust eraser", "5.00", "Essentials", 80),
                new ProductData("Sanitizer 50ml", "Hand sanitizer gel", "30.00", "Essentials", 40),
                new ProductData("Face Mask (Pack of 3)", "3-ply disposable masks", "15.00", "Essentials", 50),
                new ProductData("Tissue Pack", "Pocket tissue paper", "10.00", "Essentials", 60),

                // 🍫 Chocolates
                new ProductData("Dairy Milk Silk", "Cadbury Dairy Milk Silk bar", "80.00", "Chocolates", 25),
                new ProductData("KitKat", "Crispy wafer chocolate bar", "30.00", "Chocolates", 35),
                new ProductData("5 Star", "Cadbury 5 Star caramel bar", "20.00", "Chocolates", 40)
        );

        for (ProductData pd : products) {
            Product product = Product.builder()
                    .name(pd.name)
                    .description(pd.description)
                    .price(new BigDecimal(pd.price))
                    .category(pd.category)
                    .available(true)
                    .build();
            Product saved = productRepository.save(product);

            Inventory inventory = Inventory.builder()
                    .product(saved)
                    .quantity(pd.stock)
                    .build();
            inventoryRepository.save(inventory);
        }

        log.info("Seeded {} products with inventory", products.size());
    }

    private record ProductData(String name, String description, String price, String category, int stock) {}
}
