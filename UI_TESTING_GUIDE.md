# UI Testing Guide - Campus Food App

## Overview
This guide provides comprehensive testing strategies for the Campus Food Android app UI, including unit tests, integration tests, and accessibility tests.

## Test Structure

```
app/src/test/          # Unit tests
app/src/androidTest/   # Instrumented tests
```

## 1. Compose UI Testing

### Setup Dependencies
Add to `app/build.gradle.kts`:

```kotlin
androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")
```

### Basic Screen Test Example

```kotlin
@RunWith(AndroidJUnit4::class)
class MenuScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun menuScreen_displaysProducts() {
        // Given
        val mockProducts = listOf(
            Product(id = 1, name = "Samosa", price = 20.0, category = "Snacks"),
            Product(id = 2, name = "Coffee", price = 30.0, category = "Beverages")
        )
        
        // When
        composeTestRule.setContent {
            CampusFoodTheme {
                MenuScreen(
                    onProductClick = {},
                    onCartClick = {},
                    cartItemCount = 0
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Samosa").assertIsDisplayed()
        composeTestRule.onNodeWithText("Coffee").assertIsDisplayed()
    }
    
    @Test
    fun menuScreen_addToCart_showsSuccessAnimation() {
        composeTestRule.setContent {
            CampusFoodTheme {
                ProductCard(
                    product = Product(id = 1, name = "Samosa", price = 20.0),
                    onAddToCart = {}
                )
            }
        }
        
        // Click add to cart button
        composeTestRule.onNodeWithContentDescription("Add Samosa to cart")
            .performClick()
        
        // Verify success state
        composeTestRule.onNodeWithContentDescription("Added to cart")
            .assertIsDisplayed()
    }
    
    @Test
    fun menuScreen_searchProducts_filtersResults() {
        composeTestRule.setContent {
            CampusFoodTheme {
                MenuScreen(
                    onProductClick = {},
                    onCartClick = {},
                    cartItemCount = 0
                )
            }
        }
        
        // Type in search field
        composeTestRule.onNodeWithContentDescription("Search products")
            .performTextInput("coffee")
        
        // Verify filtered results
        composeTestRule.onNodeWithText("Coffee").assertIsDisplayed()
        composeTestRule.onNodeWithText("Samosa").assertDoesNotExist()
    }
}
```

### Cart Screen Tests

```kotlin
@RunWith(AndroidJUnit4::class)
class CartScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun cartScreen_emptyState_showsEmptyMessage() {
        composeTestRule.setContent {
            CampusFoodTheme {
                CartScreen(
                    onCheckoutClick = {},
                    onBackToMenu = {},
                    viewModel = mockCartViewModel(emptyList())
                )
            }
        }
        
        composeTestRule.onNodeWithText("Your cart is empty")
            .assertIsDisplayed()
    }
    
    @Test
    fun cartScreen_incrementQuantity_updatesTotal() {
        val item = CartItem(
            productId = 1,
            productName = "Samosa",
            quantity = 1,
            price = 20.0
        )
        
        composeTestRule.setContent {
            CampusFoodTheme {
                CartItemCard(
                    item = item,
                    onIncrement = {},
                    onDecrement = {},
                    onRemove = {}
                )
            }
        }
        
        // Click increment button
        composeTestRule.onNodeWithContentDescription("Increase quantity of Samosa")
            .performClick()
        
        // Verify quantity updated
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
    }
    
    @Test
    fun cartScreen_placeOrder_requiresAddress() {
        composeTestRule.setContent {
            CampusFoodTheme {
                CartScreen(
                    onCheckoutClick = {},
                    onBackToMenu = {},
                    viewModel = mockCartViewModel(listOf(mockCartItem()))
                )
            }
        }
        
        // Clear address field
        composeTestRule.onNodeWithText("Delivery Address")
            .performTextClearance()
        
        // Verify place order button is disabled
        composeTestRule.onNodeWithText("Place Order")
            .assertIsNotEnabled()
    }
}
```

## 2. Accessibility Testing

### TalkBack Testing

```kotlin
@RunWith(AndroidJUnit4::class)
class AccessibilityTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun productCard_hasProperContentDescription() {
        val product = Product(
            id = 1,
            name = "Samosa",
            category = "Snacks",
            price = 20.0,
            stock = 5,
            description = "Crispy and delicious"
        )
        
        composeTestRule.setContent {
            CampusFoodTheme {
                ProductCard(product = product, onAddToCart = {})
            }
        }
        
        // Verify semantic description
        composeTestRule.onNode(
            hasContentDescription("Samosa, Snacks, Price: ₹20 rupees, Only 5 left in stock, Crispy and delicious")
        ).assertExists()
    }
    
    @Test
    fun buttons_haveMininumTouchTargetSize() {
        composeTestRule.setContent {
            CampusFoodTheme {
                ProductCard(
                    product = Product(id = 1, name = "Samosa", price = 20.0),
                    onAddToCart = {}
                )
            }
        }
        
        // Verify button size is at least 48dp
        composeTestRule.onNodeWithContentDescription("Add to cart")
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)
    }
    
    @Test
    fun allIcons_haveContentDescriptions() {
        composeTestRule.setContent {
            CampusFoodTheme {
                MenuScreen(
                    onProductClick = {},
                    onCartClick = {},
                    cartItemCount = 0
                )
            }
        }
        
        // Verify all interactive icons have descriptions
        composeTestRule.onAllNodes(hasClickAction())
            .assertAll(hasContentDescription())
    }
}
```

### Contrast Ratio Testing

```kotlin
@Test
fun verifyTextContrast() {
    // Use tools like Accessibility Scanner or manual verification
    // Ensure all text meets WCAG AA standards:
    // - Normal text: 4.5:1
    // - Large text (18sp+): 3.0:1
    
    val backgroundColor = Color.White
    val textColor = Color(0xFF1A1A1A)
    
    val contrastRatio = calculateContrastRatio(textColor, backgroundColor)
    assertTrue(contrastRatio >= 4.5)
}
```

## 3. Performance Testing

### Recomposition Testing

```kotlin
@Test
fun productList_doesNotRecomposeUnnecessarily() {
    var recompositionCount = 0
    
    composeTestRule.setContent {
        CampusFoodTheme {
            SideEffect {
                recompositionCount++
            }
            
            ProductCard(
                product = Product(id = 1, name = "Samosa", price = 20.0),
                onAddToCart = {}
            )
        }
    }
    
    // Initial composition
    assertEquals(1, recompositionCount)
    
    // Trigger unrelated state change
    composeTestRule.mainClock.advanceTimeBy(1000)
    
    // Should not recompose
    assertEquals(1, recompositionCount)
}
```

### Scroll Performance

```kotlin
@Test
fun productList_scrollsSmooth() {
    val products = List(100) { index ->
        Product(id = index, name = "Product $index", price = 20.0)
    }
    
    composeTestRule.setContent {
        CampusFoodTheme {
            LazyColumn {
                items(products) { product ->
                    ProductCard(product = product, onAddToCart = {})
                }
            }
        }
    }
    
    // Measure scroll performance
    composeTestRule.mainClock.autoAdvance = false
    
    composeTestRule.onNodeWithTag("productList")
        .performScrollToIndex(50)
    
    // Advance frame by frame and verify smooth scrolling
    repeat(60) { // 60 frames = 1 second at 60fps
        composeTestRule.mainClock.advanceTimeByFrame()
    }
}
```

## 4. Integration Testing

### Navigation Testing

```kotlin
@Test
fun navigation_menuToCart_works() {
    composeTestRule.setContent {
        CampusFoodTheme {
            MainScreen()
        }
    }
    
    // Click cart button
    composeTestRule.onNodeWithContentDescription("Cart")
        .performClick()
    
    // Verify cart screen is displayed
    composeTestRule.onNodeWithText("My Cart")
        .assertIsDisplayed()
}
```

### ViewModel Integration

```kotlin
@Test
fun addToCart_updatesViewModel() = runTest {
    val viewModel = CartViewModel()
    val product = Product(id = 1, name = "Samosa", price = 20.0)
    
    composeTestRule.setContent {
        CampusFoodTheme {
            MenuScreen(
                onProductClick = { viewModel.addToCart(it) },
                onCartClick = {},
                cartItemCount = 0
            )
        }
    }
    
    // Add product to cart
    composeTestRule.onNodeWithContentDescription("Add Samosa to cart")
        .performClick()
    
    // Verify ViewModel state
    advanceUntilIdle()
    val cartState = viewModel.uiState.value
    assertTrue(cartState is CartUiState.Success)
    assertEquals(1, (cartState as CartUiState.Success).items.size)
}
```

## 5. Visual Regression Testing

### Screenshot Testing

```kotlin
@Test
fun productCard_matchesScreenshot() {
    composeTestRule.setContent {
        CampusFoodTheme {
            ProductCard(
                product = Product(
                    id = 1,
                    name = "Samosa",
                    price = 20.0,
                    category = "Snacks"
                ),
                onAddToCart = {}
            )
        }
    }
    
    // Compare with baseline screenshot
    composeTestRule.onRoot()
        .captureToImage()
        .assertAgainstGolden("product_card_baseline")
}
```

## 6. Manual Testing Checklist

### Accessibility
- [ ] Test with TalkBack enabled
- [ ] Test with large font sizes (Settings > Display > Font size)
- [ ] Test with high contrast mode
- [ ] Verify all interactive elements are reachable via keyboard
- [ ] Check color contrast with Accessibility Scanner

### Responsiveness
- [ ] Test on different screen sizes (phone, tablet)
- [ ] Test in portrait and landscape orientations
- [ ] Test with different font scales
- [ ] Test with system animations disabled

### Performance
- [ ] Scroll through long lists smoothly
- [ ] No jank during animations
- [ ] Fast app startup time
- [ ] Smooth transitions between screens

### Edge Cases
- [ ] Empty states (no products, no orders)
- [ ] Error states (network failure, server error)
- [ ] Loading states (shimmer effects)
- [ ] Very long text (product names, descriptions)
- [ ] Out of stock items
- [ ] Large cart quantities

## 7. Automated Testing Tools

### Recommended Tools
1. **Espresso** - UI testing framework
2. **Compose Test** - Jetpack Compose testing
3. **Accessibility Scanner** - Accessibility issues detection
4. **Layout Inspector** - Visual debugging
5. **Perfetto** - Performance profiling

### CI/CD Integration

```yaml
# GitHub Actions example
name: UI Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v3
      - name: Run UI Tests
        run: ./gradlew connectedAndroidTest
      - name: Upload Test Results
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: app/build/reports/androidTests/
```

## 8. Best Practices

1. **Test Naming**: Use descriptive names that explain what is being tested
2. **Arrange-Act-Assert**: Structure tests clearly
3. **Test Isolation**: Each test should be independent
4. **Mock External Dependencies**: Use fake data for consistent tests
5. **Test User Flows**: Test complete user journeys, not just individual screens
6. **Accessibility First**: Include accessibility tests in every test suite
7. **Performance Benchmarks**: Set performance baselines and monitor regressions

## Resources

- [Compose Testing Docs](https://developer.android.com/jetpack/compose/testing)
- [Accessibility Testing Guide](https://developer.android.com/guide/topics/ui/accessibility/testing)
- [Android Testing Codelab](https://developer.android.com/codelabs/advanced-android-kotlin-training-testing-basics)
