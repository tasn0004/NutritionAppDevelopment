import Foundation
import Parse

class MealPlanObservable: ObservableObject {
    
    @Published var _mealTypeSelected = ""
    @Published var _dateSelected = ""
    @Published var _recipeAddedToMealPlan = false
    
    var mealTypeSelected: String {
        get {
            return _mealTypeSelected
        }
        set {
            _mealTypeSelected = newValue
        }
    }
    
    var dateSelected: String {
        get {
            return _dateSelected
        }
        set {
            _dateSelected = newValue
        }
    }
    
    var recipeAddedToMealPlan: Bool {
        get {
            return _recipeAddedToMealPlan
        }
        set {
            _recipeAddedToMealPlan = newValue
        }
    }
}
