# ViewModel Integration - Complete Implementation Guide

## Overview
This document provides a complete guide to the enhanced state management implementation using ViewModel, StateFlow, and automatic UI updates in the Campus Food app.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                         UI Layer                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ MenuScreen   │  │  CartScreen  │  │ MainScreen   │      │
│  │              │  │              │  │ (Navigation) │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
│         │                 │                  │              │
│         │ collectAsState  │ collectAsState   │              │
│         ▼                 ▼                  ▼              │
├─────────────────────────────────────────────────────────────┤
│                    ViewModel Layer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │MenuViewModel │  │CartViewModel │  │AuthViewModel │      │
│  │              │  │              │  │              │      │
│  │ StateFlow    │  │ StateFlow    │  │ StateFlow    │      │
│  │ - products   │  │ - cartItems  │  │ - user       │      │
│  │ - loading    │  │ - count      │  │ - isLoggedIn │      │
│  │ - error      │  │ - total      │  │              │      │
│  └──────┬───────┘  └──────┬───────┘  └──────────────┘      │
│         │                 │                                 │
│         │ API calls       │ Local state                     │
│         ▼                 ▼                                 │
├─────────────────────────────────────────────────────────────┤
│                     Data Layer                               │
│  ┌──────────────┐  ┌──────────────┐                        │
│  │ RetrofitAPI  │  │ In-Memory    │                        │
│  │ (Backend)    │  │ Cart Storage │                        │
│  └──────────────┘  └──────────────┘                        │
└─────────────────────────────────────────────────────────────┘
```

## 1. CartViewModel - Complete Implementation

### State Definition

```kotlin
sealed interface CartUiState {
    data object Loading : CartUiState
    data class Success(val items: List<CartItem>) : CartUiState
    data class Error(val message: String) : CartUiState
}
```

### ViewModel Class

```kotlin
class CartViewModel : ViewModel() {
    // Primary state
    private val _uiState = MutableStateFlow<CartUiState>(
        CartUiState.Success(emptyList())
    )
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    // Derived state: Cart count (automatically updates)
    val cartItemCount: StateFlow<Int> = uiState
        .map { state ->
            when (state) {
                is CartUiState.Success -> state.items.sumOf { it.quantity }
                else -> 0
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Derived state: Total amount (automatically updates)
    val totalAmount: StateFlow<Double> = uiState
        .map { state ->
            when (state) {
                is CartUiState.Success -> 
                    state.items.sumOf { it.price * it.quantity }
                else -> 0.0
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    // Thread-safe cart storage
    private val cartItems = Collections.synchronizedList(
        mutableListOf<CartItem>()
    )

    // Operations
    fun addToCart(product: Product) { /* ... */ }
    fun removeFromCart(productId: Long) { /* ... */ }
    fun incrementQuantity(productId: Long) { /* ... */ }
    fun decrementQuantity(productId: Long) { /* ... */ }
    fun clearCart() { /* ... */ }
}
```

## 2. UI Integration - MainScreen

### Complete Example

```kotlin
@Composable
fun MainScreen() {
    // Create ViewModels (scoped to activity)
    val cartViewModel: CartViewModel = viewModel()
    val menuViewModel: MenuViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    // Collect states with lifecycle awareness
    val cartState by cartViewModel.uiState.collectAsStateWithLifecycle()
    val cartItemCount by cartViewModel.cartItemCount.collectAsStateWithLifecycle()
    val totalAmount by cartViewModel.totalAmount.collectAsStateWithLifecycle()

    // Navigation
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                cartItemCount = cartItemCount // Updates automatically!
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "menu"
        ) {
            composable("menu") {
                MenuScreen(
                    viewModel = menuViewModel,
                    cartViewModel = cartViewModel,
                    onCartClick = { navController.navigate("cart") }
                )
            }
            composable("cart") {
                CartScreen(
                    viewModel = cartViewModel,
                    totalAmount = totalAmount // Updates automatically!
                )
            }
        }
    }
}
```

## 3. MenuScreen Integration

```kotlin
@Composable
fun MenuScreen(
    viewModel: MenuViewModel,
    cartViewModel: CartViewModel,
    onCartClick: () -> Unit
) {
    // Collect menu state
    val menuState by viewModel.uiState.collectAsStateWithLifecycle()
    val isBackendOnline by viewModel.isBackendOnline.collectAsStateWithLifecycle()
    
    // Collect cart count for badge
    val cartItemCount by cartViewModel.cartItemCount.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu") },
                actions = {
                    // Cart button with badge
                    BadgedBox(
                        badge = {
                            if (cartItemCount > 0) {
                                Badge { Text("$cartItemCount") }
                            }
                        }
                    ) {
                        IconButton(onClick = onCartClick) {
                            Icon(Icons.Default.ShoppingCart, "Cart")
                        }
                    }
                }
            )
        }
    ) { padding ->
        when (val state = menuState) {
            is MenuUiState.Loading -> {
                LoadingIndicator()
            }
            is MenuUiState.Success -> {
                ProductList(
                    products = state.products,
                    onAddToCart = { product ->
                        // Add to cart - UI updates automatically!
                        cartViewModel.addToCart(product)
                    }
                )
            }
            is MenuUiState.Error -> {
                ErrorMessage(
                    message = state.message,
                    onRetry = { viewModel.getProducts() }
                )
            }
        }
    }
}
```

## 4. CartScreen Integration

```kotlin
@Composable
fun CartScreen(
    viewModel: CartViewModel,
    totalAmount: Double
) {
    val cartState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            // Sticky total section
            if (cartState is CartUiState.Success) {
                val items = (cartState as CartUiState.Success).items
                if (items.isNotEmpty()) {
                    CheckoutBar(
                        totalAmount = totalAmount, // Updates automatically!
                        itemCount = items.sumOf { it.quantity },
                        onCheckout = { /* Place order */ }
                    )
                }
            }
        }
    ) { padding ->
        when (val state = cartState) {
            is CartUiState.Success -> {
                if (state.items.isEmpty()) {
                    EmptyCartView()
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(padding)
                    ) {
                        items(state.items, key = { it.productId }) { item ->
                            CartItemCard(
                                item = item,
                                onIncrement = { 
                                    viewModel.incrementQuantity(item.productId)
                                },
                                onDecrement = { 
                                    viewModel.decrementQuantity(item.productId)
                                },
                                onRemove = { 
                                    viewModel.removeFromCart(item.productId)
                                }
                            )
                        }
                    }
                }
            }
            is CartUiState.Loading -> LoadingIndicator()
            is CartUiState.Error -> ErrorMessage(state.message)
        }
    }
}
```

## 5. Bottom Navigation with Cart Badge

```kotlin
@Composable
fun BottomNavigationBar(
    navController: NavController,
    cartItemCount: Int // Automatically updates!
) {
    NavigationBar {
        NavigationBarItem(
            icon = {
                BadgedBox(
                    badge = {
                        if (cartItemCount > 0) {
                            Badge(
                                containerColor = OrangePrimary,
                                contentColor = Color.White
                            ) {
                                Text(
                                    "$cartItemCount",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.ShoppingCart, "Cart")
                }
            },
            label = { Text("Cart") },
            selected = currentRoute == "cart",
            onClick = { navController.navigate("cart") }
        )
        // Other navigation items...
    }
}
```

## 6. State Flow Lifecycle

### Why collectAsStateWithLifecycle()?

```kotlin
// ✅ RECOMMENDED: Lifecycle-aware collection
val state by viewModel.state.collectAsStateWithLifecycle()

// ❌ AVOID: Not lifecycle-aware (can cause memory leaks)
val state by viewModel.state.collectAsState()
```

**Benefits:**
- Automatically starts/stops collection based on lifecycle
- Prevents memory leaks
- Handles configuration changes (rotation)
- Stops collection when app is in background

### SharingStarted.WhileSubscribed(5000)

```kotlin
.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = 0
)
```

**What it does:**
- Keeps flow active while there are subscribers
- Stops 5 seconds after last subscriber leaves
- Restarts when new subscriber arrives
- Optimizes memory and battery

## 7. Complete User Flow Example

### Scenario: User adds item to cart

```
1. User clicks "Add to Cart" button
   ↓
2. UI calls: cartViewModel.addToCart(product)
   ↓
3. ViewModel updates internal cart list
   ↓
4. ViewModel emits new state: _uiState.value = CartUiState.Success(items)
   ↓
5. StateFlow propagates change to all collectors
   ↓
6. Derived states automatically recalculate:
   - cartItemCount updates
   - totalAmount updates
   ↓
7. UI recomposes automatically:
   - Cart badge shows new count
   - Total amount updates
   - Cart screen shows new item
   ↓
8. User sees updated UI (no manual refresh needed!)
```

## 8. Key Benefits

### Automatic UI Updates
```kotlin
// No manual state synchronization needed!
cartViewModel.addToCart(product)
// UI automatically updates everywhere:
// - Cart badge
// - Cart screen
// - Total amount
// - Item count
```

### Single Source of Truth
```kotlin
// One ViewModel instance shared across screens
val cartViewModel: CartViewModel = viewModel() // In MainActivity

// All screens use the same instance
MenuScreen(cartViewModel = cartViewModel)
CartScreen(viewModel = cartViewModel)
```

### Thread Safety
```kotlin
// Synchronized operations prevent race conditions
synchronized(cartItems) {
    cartItems.add(item)
    emitCartState()
}
```

### Lifecycle Awareness
```kotlin
// Automatically handles:
// - Screen rotation
// - App backgrounding
// - Memory cleanup
val state by viewModel.state.collectAsStateWithLifecycle()
```

## 9. Testing

### ViewModel Unit Test

```kotlin
@Test
fun `addToCart updates state and count`() = runTest {
    val viewModel = CartViewModel()
    val product = Product(id = 1, name = "Test", price = 10.0)

    // Add product
    viewModel.addToCart(product)

    // Verify state
    val state = viewModel.uiState.value
    assertTrue(state is CartUiState.Success)
    assertEquals(1, (state as CartUiState.Success).items.size)

    // Verify derived state
    assertEquals(1, viewModel.cartItemCount.value)
    assertEquals(10.0, viewModel.totalAmount.value, 0.01)
}

@Test
fun `incrementQuantity updates count automatically`() = runTest {
    val viewModel = CartViewModel()
    val product = Product(id = 1, name = "Test", price = 10.0)

    viewModel.addToCart(product)
    assertEquals(1, viewModel.cartItemCount.value)

    viewModel.incrementQuantity(1)
    assertEquals(2, viewModel.cartItemCount.value)
    assertEquals(20.0, viewModel.totalAmount.value, 0.01)
}
```

## 10. Common Patterns

### Loading State
```kotlin
when (val state = uiState) {
    is CartUiState.Loading -> CircularProgressIndicator()
    is CartUiState.Success -> CartContent(state.items)
    is CartUiState.Error -> ErrorMessage(state.message)
}
```

### Empty State
```kotlin
if (state.items.isEmpty()) {
    EmptyCartView()
} else {
    CartItemsList(state.items)
}
```

### Error Handling
```kotlin
is CartUiState.Error -> {
    ErrorMessage(
        message = state.message,
        onRetry = { viewModel.retry() }
    )
}
```

## Summary

The enhanced state management provides:

✅ **Automatic UI Updates** - No manual synchronization
✅ **Derived States** - Cart count and total update automatically
✅ **Thread Safety** - Synchronized cart operations
✅ **Lifecycle Awareness** - Prevents memory leaks
✅ **Single Source of Truth** - One ViewModel instance
✅ **Testable** - Easy to unit test
✅ **Scalable** - Easy to add new features
✅ **Clean Architecture** - Separation of concerns

The cart count in bottom navigation updates automatically whenever items are added/removed, providing a seamless user experience!
