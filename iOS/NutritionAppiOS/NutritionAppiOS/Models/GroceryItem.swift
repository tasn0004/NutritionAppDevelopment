import SwiftUI

struct GroceryItem: Identifiable {
    let id = UUID()
    let name: String
    var quantity: Double
    let quantityUnit: String
    let mealPlanAdded: Bool
    var isToggled: Bool
    
    init(_ name: String, _ quantity: Double, _ quantityUnit: String, _ mealPlanAdded: Bool, _ isToggled: Bool) {
        self.name = name
        self.quantity = quantity
        self.quantityUnit = quantityUnit
        self.mealPlanAdded = mealPlanAdded
        self.isToggled = isToggled
    }
}
