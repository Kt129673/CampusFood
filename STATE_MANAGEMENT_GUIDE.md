# State Management Guide - Campus Food App

## Overview
This guide demonstrates the enhanced state management implementation using ViewModel, StateFlow, and automatic UI updates.

## Architecture

### State Management Pattern
```
ViewModel (StateFlow) → UI (Compose) → User Actions → ViewModel
     ↑                                                      ↓
     └──────────────── Automatic Recomposition ────────────┘
```

## CartViewModel - Enhanced Implementation

### Key Features

#### 1. Primary State (Cart Items)
```kotlin
private val _uiState = MutableStateFlow<CartUiState>(CartUiState.Success(emptyList()))
val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()
```

#### 2. Derived States (Automatically Update UI)
```kotlin
// Cart item count - updates automatically when cart changes
val cartItemCount: StateFlow<Int> = uiState.map { state ->
    when (state) {
        is CartUiState.Success -> state.items.sumOf { it.quantity }
        else -> 0
    }
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

// Total amount - updates automatically when cart changes
val totalAmount: StateFlow<Double> = uiState.map { state ->
    when (state) {
        is CartUiState.Success -> state.items.sumOf { it.price * it.quantity }
        else -> 0.0
    }
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
```

### Cart Operations

#### Add to Cart
```kotlin
fun addToCart(product: Product) {
    synchronized(cartItems) {
        val existing = cartItems.find { it.productId == product.id }
        if (existing != null) {
            // Increment quantity if already in cart
            val index = cartItems.indexOf(existing)
            cartItems[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            // Add new item to cart
            cartItems.add(CartItem(...))
        }
        emitCartState() // Triggers UI update
    }
}
```

#### Update Quantity
```kotlin
fun incrementQuantity(productId: Long)
fun decrementQuantity(productId: Long)
fun updateQuantity(productId: Long, newQuantity: Int)
```

#### Remove from Cart
```kotlin
fun removeFromCart(productId: Long)
fun clearCart()
```

#### Query Cart State
```kotlin
fun isInCart(productId: Long): Boolean
fun getProductQuantity(productId: Long): Int
```

## UI Integration

### 1. Collect State in Composable

```kotlin
@Composable
fun MainScreen() {
    val cartViewModel: CartViewModel = viewModel()
    
    // Collect cart state - UI updates automatically
    val cartState by cartViewModel.uiState.collectAsStateWithLifecycle()
    
    // Collect cart count - updates automatically when cart changes
    val cartItemCount by cartViewModel.cartItemCount.collectAsStateWithLifecycle()
    
    // Collect total amount - updates automatically
    val totalAmount by cartViewModel.totalAmount.collectAsStateWithLifecycle()
    
    // Use in UI
    Text("Cart: $cartItemCount items")
    Text("Total: ₹$totalAmount")
}
```

### 2. Handle User Actions

```kotlin
@Composable
fun MenuScreen(viewModel: MenuViewModel, cartViewModel: CartViewModel) {
    val menuState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when (val state = menuState) {
        is MenuUiState.Success -> {
            LazyColumn {
                items(state.products) { product ->
                    ProductCard(
                        product = product,
                        onAddToCart = { 
                            // Add to cart - UI updates automatically
                            cartViewModel.addToCart(product)
                        }
                    )
                }
            }
        }
        is MenuUiState.Loading -> LoadingIndicator()
        is MenuUiState.Error -> ErrorMessage(state.message)
    }
}
```

### 3. Bottom Navigation with Cart Badge

```kotlin
@Composable
fun BottomNavigation(cartViewModel: CartViewModel) {
    // Automatically updates when cart changes
    val cartItemCount by cartViewModel.cartItemCount.collectAsStateWithLifecycle()
    
    NavigationBar {
        NavigationBarItem(
            icon = {
                BadgedBox(
                    badge = {
                        if (cartItemCount > 0) {
                            Badge { Text("$cartItemCount") }
                        }
                    }
                ) {
                    Icon(Icons.Default.ShoppingCart, "Cart")
                }
            },
            label = { Text("Cart") },
            selected = true,
            onClick = { /* Navigate to cart */ }
        )
    }
}
```

### 4. Cart Screen with Quantity Controls

```kotlin
@Composable
fun CartScreen(viewModel: CartViewModel) {
    val cartState by viewModel.uiState.collectAsStateWithLifecycle()
    val totalAmount by viewModel.totalAmount.collectAsStateWithLifecycle()
    
    when (val state = cartState) {
        is CartUiState.Success -> {
            Column {
                // Cart items
                LazyColumn {
                    items(state.items) { item ->
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
                
                // Total section - updates automatically
                Text("Total: ₹${String.format("%.2f", totalAmount)}")
                
                Button(onClick = { /* Place order */ }) {
                    Text("Place Order")
                }
            }
        }
        is CartUiState.Loading -> LoadingIndicator()
        is CartUiState.Error -> ErrorMessage(state.message)
    }
}
```

## MenuViewModel - Enhanced Implementation

### Features

#### 1. Product State
```kotlin
private val _uiState = MutableStateFlow<MenuUiState>(MenuUiState.Loading)
val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()
```

#### 2. Backend Health Monitoring
```kotlin
private val _isBackendOnline = MutableStateFlow(true)
val isBackendOnline: StateFlow<Boolean> = _isBackendOnline.asStateFlow()
```

#### 3. Search and Filter State
```kotlin
private val _searchQuery = MutableStateFlow("")
val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

private val _selectedCategory = MutableStateFlow("All")
val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()
```

### Operations

```kotlin
// Fetch products
fun getProducts()

// Update search
fun updateSearchQuery(query: String)

// Update category
fun updateSelectedCategory(category: String)

// Refresh
fun refresh()
```

## State Flow Lifecycle

### SharingStarted.WhileSubscribed(5000)
- Keeps the flow active while there are subscribers
- Stops 5 seconds after the last subscriber leaves
- Restarts when a new subscriber arrives
- Optimizes memory and battery usage

### collectAsStateWithLifecycle()
- Automatically starts/stops collection based on lifecycle
- Prevents memory leaks
- Handles configuration changes (rotation, etc.)
- Recommended for Compose UI

## Benefits

### 1. Automatic UI Updates
- No manual state synchronization
- UI recomposes automatically when state changes
- Single source of truth

### 2. Thread Safety
- Synchronized cart operations
- Safe concurrent access
- No race conditions

### 3. Lifecycle Aware
- Respects Android lifecycle
- Prevents memory leaks
- Handles configuration changes

### 4. Testable
- Easy to test ViewModels in isolation
- Mock StateFlows for testing
- Predictable state transitions

### 5. Scalable
- Easy to add new states
- Derived states computed automatically
- Clean separation of concerns

## Example: Complete Flow

```kotlin
// 1. User clicks "Add to Cart" button
Button(onClick = { cartViewModel.addToCart(product) })

// 2. ViewModel updates cart state
fun addToCart(product: Product) {
    cartItems.add(CartItem(...))
    emitCartState() // Updates _uiState
}

// 3. Derived states automatically update
val cartItemCount: StateFlow<Int> = uiState.map { ... }

// 4. UI automatically recomposes
val cartItemCount by cartViewModel.cartItemCount.collectAsStateWithLifecycle()
Text("Cart: $cartItemCount items") // Shows updated count

// 5. Bottom navigation badge updates
Badge { Text("$cartItemCount") } // Shows new count
```

## Best Practices

### 1. Use StateFlow for State
```kotlin
// ✅ Good
private val _state = MutableStateFlow(initialValue)
val state: StateFlow<Type> = _state.asStateFlow()

// ❌ Avoid
var state by mutableStateOf(initialValue)
```

### 2. Collect with Lifecycle
```kotlin
// ✅ Good
val state by viewModel.state.collectAsStateWithLifecycle()

// ❌ Avoid
val state by viewModel.state.collectAsState()
```

### 3. Derive States
```kotlin
// ✅ Good - Automatically updates
val count: StateFlow<Int> = items.map { it.size }

// ❌ Avoid - Manual updates needed
val count: Int get() = items.value.size
```

### 4. Single ViewModel Instance
```kotlin
// ✅ Good - Shared across screens
@Composable
fun MainScreen() {
    val cartViewModel: CartViewModel = viewModel()
    // Pass to child composables
}

// ❌ Avoid - Multiple instances
@Composable
fun Screen1() {
    val cartViewModel: CartViewModel = viewModel() // Instance 1
}
@Composable
fun Screen2() {
    val cartViewModel: CartViewModel = viewModel() // Instance 2 (different!)
}
```

### 5. Thread-Safe Operations
```kotlin
// ✅ Good
fun addToCart(product: Product) {
    synchronized(cartItems) {
        cartItems.add(...)
        emitCartState()
    }
}

// ❌ Avoid
fun addToCart(product: Product) {
    cartItems.add(...) // Not thread-safe
    emitCartState()
}
```

## Testing

### ViewModel Testing
```kotlin
@Test
fun `addToCart updates state correctly`() = runTest {
    val viewModel = CartViewModel()
    val product = Product(id = 1, name = "Test", price = 10.0)
    
    viewModel.addToCart(product)
    
    val state = viewModel.uiState.value
    assertTrue(state is CartUiState.Success)
    assertEquals(1, (state as CartUiState.Success).items.size)
}

@Test
fun `cartItemCount updates automatically`() = runTest {
    val viewModel = CartViewModel()
    val product = Product(id = 1, name = "Test", price = 10.0)
    
    viewModel.addToCart(product)
    
    assertEquals(1, viewModel.cartItemCount.value)
    
    viewModel.addToCart(product)
    
    assertEquals(2, viewModel.cartItemCount.value)
}
```

## Summary

The enhanced state management provides:
- ✅ Automatic UI updates via StateFlow
- ✅ Derived states (cart count, total) that update automatically
- ✅ Thread-safe cart operations
- ✅ Lifecycle-aware state collection
- ✅ Single source of truth
- ✅ Clean separation of concerns
- ✅ Easy to test and maintain
- ✅ Scalable architecture
