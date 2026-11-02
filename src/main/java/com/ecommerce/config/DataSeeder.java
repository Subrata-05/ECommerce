package com.ecommerce.config;

import com.ecommerce.entity.Ad;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.repository.AdRepository;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

  private final AdRepository adRepository;
  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;
  private final EnvConfig envConfig;
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    seedAdminUser();
    if (productRepository.count() > 0) {
      log.info("Database already seeded with products. Skipping data seeding.");
      return;
    }

    log.info("Seeding categories and products...");

    // 1. Create and save categories
    Category shoes = getOrCreateCategory("Shoes", "Footwear for men and women");
    Category jewelery = getOrCreateCategory("Jewelery", "Fine jewelry and accessories");
    Category bags = getOrCreateCategory("Bags", "Handbags, backpacks, and luggage");
    Category electronics = getOrCreateCategory("Electronics", "Gadgets, devices, and accessories");
    Category clothes = getOrCreateCategory("Clothes", "Apparel and clothing items");

    // 2. Seed 6 products for each category

    seedShoes(shoes);
    seedJewelery(jewelery);
    seedBags(bags);
    seedElectronics(electronics);
    seedClothes(clothes);
    seedAds();
    log.info("Data seeding completed successfully!");
  }

  private void seedAds() {

    if (adRepository.count() <= 0) {
      log.info("Seeding promotional advertisements...");

      List<Ad> ads =
          List.of(
              createAd(
                  "Summer Flash Sale",
                  "Get up to 50% off on all trending footwear and summer clothes collection!",
                  "https://via.placeholder.com/800x400?text=Summer+Flash+Sale",
                  "/seedfile/ads/ad1.png",
                  true),
              createAd(
                  "New Electronics Arrival",
                  "Upgrade your setup with our latest noise-canceling headphones and gaming keyboards.",
                  "/seedfile/ads/ad3.png",
                  "/api/v1/products?category=electronics",
                  true),
              createAd(
                  "Luxury Jewelry Clearance",
                  "Exclusive discounts on sterling silver necklaces and diamond-cut rings. Limited time only!",
                  "/seedfile/ads/ad1.png",
                  "/api/v1/products?category=jewelery",
                  true),
              createAd(
                  "Free Shipping Worldwide",
                  "Enjoy free shipping on all orders over $50 across bags, apparel, and accessories.",
                  "/seedfile/ads/ad2.png",
                  "/api/v1/products",
                  true),
              createAd(
                  "Back to School Backpacks",
                  "Durable travel backpacks and executive briefcases starting at just $22.99.",
                  "/seedfile/ads/ad3.png",
                  "/api/v1/products?category=bags",
                  false // Example of an inactive or upcoming ad
                  ),
              createAd(
                  "Exclusive Member Discount",
                  "Sign up today and get an extra 15% off your first online purchase.",
                  "/seedfile/ads/ad4.png",
                  "/api/v1/auth/register",
                  true));

      adRepository.saveAll(ads);
    }
  }

  private void seedAdminUser() {
    String adminEmail = envConfig.getSupperAdminEmail();
    String adminPassword = envConfig.getSuperAdminPassword();

    if (userRepository.findByEmail(adminEmail).isPresent()) {
      log.info("Admin user already exists. Skipping admin seeding...");
      return;
    }

    log.info("Seeding default Admin user...");

    User admin =
        User.builder()
            .firstName("Supper")
            .lastName("Admin")
            .email(adminEmail)
            .password(passwordEncoder.encode(adminPassword))
            .role(Role.ROLE_ADMIN)
            .enabled(true)
            .build();

    userRepository.save(admin);
    log.info("Admin user seeded successfully with email: {}", adminEmail);
  }

  private Category getOrCreateCategory(String name, String description) {
    return categoryRepository
        .findByName(name)
        .orElseGet(
            () -> {
              Category cat = new Category();
              cat.setName(name);
              cat.setSlug("");
              cat.setThumbnail("");
              cat.setDescription(description);
              return categoryRepository.save(cat);
            });
  }

  private void seedShoes(Category category) {
    List<Product> products =
        List.of(
            createProduct(
                "Running Sneakers Pro",
                "Lightweight athletic running shoes with breathable mesh.",
                new BigDecimal("89.99"),
                45,
                category),
            createProduct(
                "Classic Leather Boots",
                "Durable handcrafted leather boots for everyday wear.",
                new BigDecimal("149.50"),
                20,
                category),
            createProduct(
                "Casual Canvas Loafers",
                "Comfortable slip-on canvas shoes for summer days.",
                new BigDecimal("45.00"),
                60,
                category),
            createProduct(
                "High-Top Basketball Shoes",
                "Ankle-support professional basketball shoes.",
                new BigDecimal("120.00"),
                15,
                category),
            createProduct(
                "Waterproof Hiking Shoes",
                "Rugged outdoor hiking shoes with enhanced grip soles.",
                new BigDecimal("110.00"),
                30,
                category),
            createProduct(
                "Minimalist Slip-on Sandals",
                "Comfortable beach and pool sandals.",
                new BigDecimal("25.99"),
                100,
                category));
    productRepository.saveAll(products);
  }

  private void seedJewelery(Category category) {
    List<Product> products =
        List.of(
            createProduct(
                "Sterling Silver Necklace",
                "Elegant 925 sterling silver chain with a teardrop pendant.",
                new BigDecimal("75.00"),
                25,
                category),
            createProduct(
                "Gold Plated Hoop Earrings",
                "Classic lightweight gold hoops for daily elegance.",
                new BigDecimal("35.50"),
                50,
                category),
            createProduct(
                "Diamond Cut Wedding Band",
                "14k white gold minimalist band.",
                new BigDecimal("299.99"),
                10,
                category),
            createProduct(
                "Vintage Gemstone Ring",
                "Statement ring featuring a deep blue topaz stone.",
                new BigDecimal("125.00"),
                12,
                category),
            createProduct(
                "Beaded Charm Bracelet",
                "Handcrafted leather and metal charm bracelet.",
                new BigDecimal("40.00"),
                40,
                category),
            createProduct(
                "Classic Chronograph Watch",
                "Stainless steel water-resistant wristwatch.",
                new BigDecimal("199.00"),
                18,
                category));
    productRepository.saveAll(products);
  }

  private void seedBags(Category category) {
    List<Product> products =
        List.of(
            createProduct(
                "Executive Leather Briefcase",
                "Professional laptop briefcase with multiple compartments.",
                new BigDecimal("159.99"),
                20,
                category),
            createProduct(
                "Canvas Travel Backpack",
                "Durable 40L multi-pocket backpack for travel and school.",
                new BigDecimal("65.00"),
                50,
                category),
            createProduct(
                "Designer Crossbody Purse",
                "Compact faux-leather handbag with gold chain strap.",
                new BigDecimal("85.00"),
                35,
                category),
            createProduct(
                "Gym Duffel Bag",
                "Spacious sports duffel with a separate shoe compartment.",
                new BigDecimal("45.00"),
                40,
                category),
            createProduct(
                "Rolling Carry-On Luggage",
                "Hard-shell TSA-approved lightweight suitcase.",
                new BigDecimal("130.00"),
                12,
                category),
            createProduct(
                "Minimalist Fanny Pack",
                "Water-resistant waist bag for running and festivals.",
                new BigDecimal("22.99"),
                75,
                category));
    productRepository.saveAll(products);
  }

  private void seedElectronics(Category category) {
    List<Product> products =
        List.of(
            createProduct(
                "Wireless Noise-Canceling Headphones",
                "Over-ear Bluetooth headphones with 30-hour battery life.",
                new BigDecimal("199.99"),
                30,
                category),
            createProduct(
                "Mechanical RGB Gaming Keyboard",
                "Tactile switches with customizable backlighting.",
                new BigDecimal("79.99"),
                45,
                category),
            createProduct(
                "Ultra-Slim Power Bank 20000mAh",
                "Fast-charging portable charger with dual USB ports.",
                new BigDecimal("49.99"),
                80,
                category),
            createProduct(
                "4K Action Camera",
                "Waterproof sports camera with remote control and accessories.",
                new BigDecimal("149.00"),
                25,
                category),
            createProduct(
                "Ergonomic Wireless Mouse",
                "Precision optical mouse designed for wrist comfort.",
                new BigDecimal("35.00"),
                60,
                category),
            createProduct(
                "Smart Fitness Tracker",
                "Heart rate monitor, sleep tracker, and step counter.",
                new BigDecimal("59.99"),
                55,
                category));
    productRepository.saveAll(products);
  }

  private void seedClothes(Category category) {
    List<Product> products =
        List.of(
            createProduct(
                "Classic Cotton Crewneck T-Shirt",
                "Soft breathable everyday regular-fit t-shirt.",
                new BigDecimal("19.99"),
                120,
                category),
            createProduct(
                "Slim-Fit Denim Jeans",
                "Stretchable blue denim jeans with classic styling.",
                new BigDecimal("59.50"),
                50,
                category),
            createProduct(
                "Fleece Zip-Up Hoodie",
                "Warm and cozy hoodie with front pockets.",
                new BigDecimal("49.99"),
                40,
                category),
            createProduct(
                "Waterproof Windbreaker Jacket",
                "Lightweight weather-resistant outdoor jacket.",
                new BigDecimal("85.00"),
                25,
                category),
            createProduct(
                "Formal Button-Down Shirt",
                "Wrinkle-resistant dress shirt for office and events.",
                new BigDecimal("42.00"),
                35,
                category),
            createProduct(
                "Athletic Jogger Pants",
                "Comfortable sweatpants with tapered ankles and drawstrings.",
                new BigDecimal("38.00"),
                60,
                category));
    productRepository.saveAll(products);
  }

  private Product createProduct(
      String name, String description, BigDecimal price, Integer stockQuantity, Category category) {
    return Product.builder()
        .name(name)
        .description(description)
        .price(price)
        .stockQuantity(stockQuantity)
        .imageUrl("https://via.placeholder.com/300?text=" + name.replaceAll(" ", "+"))
        .category(category)
        .build();
  }

  private Ad createAd(
      String title, String description, String imageUrl, String targetUrl, boolean active) {
    return Ad.builder()
        .title(title)
        .description(description)
        .imageUrl(imageUrl)
        .targetUrl(targetUrl)
        .active(
            active) // Change to 'status' or remove if your entity handles activation differently
        .createdAt(LocalDateTime.now())
        .build();
  }
}
