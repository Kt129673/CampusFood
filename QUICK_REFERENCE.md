# State Management - Quick Reference Card

## 🚀 Quick Start

### 1. Get ViewModel Instance
```kotlin
@Composable
fun MyScreen() {
    val cartViewModel: CartViewModel = viewModel()
}
```

### 2. Collect State
```kotlin
// Cart items
val cartState by cartViewModel.uiState.collectAsStateWithLifecycle()

// Cart count (automatically updates!)
val cartCount by cartViewModel.cartItemCount.collectAsStateWithLifecycle()

// Total amount (automatically updates!)
val total by cartViewModel.totalAmount.collectAsStateWithLifecycle()
```

### 3. Use in UI
```kotlin
Text("Cart: $cartCount items")
Text("Total: ₹${String.format("%.2f", total)}")
```

### 4. Handle User Actions
```kotlin
Button(onClick = { cartViewModel.addToCart(product) })
```

## 📋 CartViewModel API

### State Properties
```kotlin
// Primary state
val uiState: StateFlow<CartUiState>

// Derived states (auto-update)
val cartItemCount: StateFlow<Int>
val totalAmount: StateFlow<Double>
```

### Operations
```kotlin
// Add product to cart
fun addToCart(product: Product)

// Remove product from cart
fun removeFromCart(productId: Long)

// Update quantities
fun incrementQuantity(productId: Long)
fun decrementQuantity(productId: Long)
fun updateQuantity(productId: Long, newQuantity: Int)

// Clear cart
fun clearCart()

// Query cart
fun isInCart(productId: Long): Boolean
fun getProductQuantity(productId: Long): Int
```

## 🎯 Common Patterns

### Pattern 1: Display Cart Badge
```kotlin
BadgedBox(
    badge = {
        if (cartCount > 0) {
            Badge { Text("$cartCount") }
        }
    }
) {
    Icon(Icons.Default.ShoppingCart, "Cart")
}
```

### Pattern 2: Show Cart Items
```kotlin
when (val state = cartState) {
    is CartUiState.Loading -> LoadingIndicator()
    is CartUiState.Success -> {
        LazyColumn {
            items(state.items) { item ->
                CartItemCard(item)
            }
        }
    }
    is CartUiState.Error -> ErrorMessage(state.message)
}
```

### Pattern 3: Quantity Stepper
```kotlin
Row {
    IconButton(onClick = { 
        viewModel.decrementQuantity(productId) 
    }) {
        Icon(Icons.Default.Remove, "Decrease")
    }
    
    Text("$quantity")
    
    IconButton(onClick = { 
        viewModel.incrementQuantity(productId) 
    }) {
        Icon(Icons.Default.Add, "Increase")
    }
}
```

### Pattern 4: Add to Cart Button
```kotlin
Button(
    onClick = { 
        cartViewModel.addToCart(product)
        // UI updates automatically!
    }
) {
    Icon(Icons.Default.Add, null)
    Text("Add to Cart")
}
```

### Pattern 5: Checkout Section
```kotlin
Column {
    Text("Total: ₹${String.format("%.2f", total)}")
    
    Button(
        onClick = { /* Place order */ },
        enabled = cartCount > 0
    ) {
        Text("Place Order")
    }
}
```

## ⚡ State Updates

### Automatic Updates
```kotlin
// When you call any cart operation:
cartViewModel.addToCart(product)
cartViewModel.incrementQuantity(id)
cartViewModel.removeFromCart(id)

// These automatically update:
// ✅ cartItemCount
// ✅ totalAmount
// ✅ All UI screens
// ✅ Navigation badges
// ✅ Cart totals
```

### Manual Refresh (Not Needed!)
```kotlin
// ❌ DON'T DO THIS - Not needed!
cartViewModel.addToCart(product)
refreshUI() // Not needed!
updateBadge() // Not needed!
recalculateTotal() // Not needed!

// ✅ DO THIS - Everything updates automatically!
cartViewModel.addToCart(product)
// That's it! UI updates automatically.
```

## 🔍 Debugging

### Check Current State
```kotlin
// In ViewModel
println("Cart items: ${cartItems.size}")
println("Current state: ${_uiState.value}")

// In UI
LaunchedEffect(cartState) {
    println("Cart state changed: $cartState")
}
```

### Monitor State Changes
```kotlin
LaunchedEffect(cartCount) {
    println("Cart count changed to: $cartCount")
}

LaunchedEffect(total) {
    println("Total changed to: $total")
}
```

## 🧪 Testing

### Test ViewModel
```kotlin
@Test
fun `addToCart updates state`() = runTest {
    val viewModel = CartViewModel()
    val product = Product(id = 1, name = "Test", price = 10.0)
    
    viewModel.addToCart(product)
    
    val state = viewModel.uiState.value
    assertTrue(state is CartUiState.Success)
    assertEquals(1, viewModel.cartItemCount.value)
}
```

### Test UI
```kotlin
@Test
fun `cart badge shows correct count`() {
    val viewModel = CartViewModel()
    
    composeTestRule.setContent {
        val count by viewModel.cartItemCount.collectAsStateWithLifecycle()
        Badge { Text("$count") }
    }
    
    viewModel.addToCart(product)
    
    composeTestRule.onNodeWithText("1").assertExists()
}
```

## ⚠️ Common Mistakes

### ❌ Creating Multiple ViewModel Instances
```kotlin
// WRONG - Different instances!
@Composable
fun Screen1() {
    val vm: CartViewModel = viewModel() // Instance 1
}

@Composable
fun Screen2() {
    val vm: CartViewModel = viewModel() // Instance 2 (different!)
}
```

### ✅ Share Single Instance
```kotlin
// CORRECT - Same instance!
@Composable
fun MainScreen() {
    val cartViewModel: CartViewModel = viewModel() // Create once
    
    Screen1(cartViewModel) // Pass to children
    Screen2(cartViewModel) // Pass to children
}
```

### ❌ Manual State Calculation
```kotlin
// WRONG - Manual calculation
val count = if (cartState is CartUiState.Success) {
    (cartState as CartUiState.Success).items.sumOf { it.quantity }
} else 0
```

### ✅ Use Derived State
```kotlin
// CORRECT - Use derived StateFlow
val count by cartViewModel.cartItemCount.collectAsStateWithLifecycle()
```

### ❌ Not Using Lifecycle-Aware Collection
```kotlin
// WRONG - Can cause memory leaks
val count by cartViewModel.cartItemCount.collectAsState()
```

### ✅ Use Lifecycle-Aware Collection
```kotlin
// CORRECT - Lifecycle-aware
val count by cartViewModel.cartItemCount.collectAsStateWithLifecycle()
```

## 📊 Performance Tips

### 1. Use Derived States
```kotlin
// ✅ Good - Calculated once in ViewModel
val count by viewModel.cartItemCount.collectAsStateWithLifecycle()

// ❌ Bad - Recalculated on every recomposition
val count = cartState.items.sumOf { it.quantity }
```

### 2. Use Keys in Lists
```kotlin
// ✅ Good - Efficient updates
LazyColumn {
    items(items, key = { it.productId }) { item ->
        CartItemCard(item)
    }
}

// ❌ Bad - Recomposes entire list
LazyColumn {
    items(items) { item ->
        CartItemCard(item)
    }
}
```

### 3. Avoid Unnecessary Recompositions
```kotlin
// ✅ Good - Only recomposes when count changes
val count by viewModel.cartItemCount.collectAsStateWithLifecycle()
Text("Cart: $count")

// ❌ Bad - Recomposes when entire state changes
val state by viewModel.uiState.collectAsStateWithLifecycle()
Text("Cart: ${state.items.size}")
```

## 🎓 Best Practices

1. **Always use `collectAsStateWithLifecycle()`** for state collection
2. **Share single ViewModel instance** across screens
3. **Use derived StateFlows** instead of manual calculations
4. **Don't create ViewModels in child composables** - pass from parent
5. **Use keys in LazyColumn/LazyRow** for efficient updates
6. **Keep UI logic in Composables** and business logic in ViewModels
7. **Test ViewModels independently** from UI
8. **Use sealed interfaces** for state (Loading, Success, Error)

## 📚 Resources

- **STATE_MANAGEMENT_GUIDE.md** - Comprehensive guide
- **VIEWMODEL_INTEGRATION.md** - Complete implementation
- **STATE_MANAGEMENT_COMPARISON.md** - Before/after comparison
- **ARCHITECTURE_DIAGRAM.md** - Visual architecture guide
- **CartStateExample.kt** - Working example code

## 🆘 Need Help?

### Issue: Cart count not updating
**Solution:** Make sure you're using `collectAsStateWithLifecycle()` and sharing the same ViewModel instance.

### Issue: Memory leak
**Solution:** Use `collectAsStateWithLifecycle()` instead of `collectAsState()`.

### Issue: State inconsistent across screens
**Solution:** Ensure all screens use the same ViewModel instance (pass from parent).

### Issue: UI not recomposing
**Solution:** Make sure you're collecting StateFlow with `by` delegate and `collectAsStateWithLifecycle()`.

## ✨ Summary

```kotlin
// This is all you need!
@Composable
fun MyScreen() {
    val cartViewModel: CartViewModel = viewModel()
    val cartCount by cartViewModel.cartItemCount.collectAsStateWithLifecycle()
    
    Text("Cart: $cartCount items")
    
    Button(onClick = { cartViewModel.addToCart(product) }) {
        Text("Add to Cart")
    }
}

// Everything updates automatically! 🎉
```
