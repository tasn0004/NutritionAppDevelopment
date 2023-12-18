import Foundation
import Parse

class HomePageObservable: ObservableObject {

    @Published var _isCategorySelected = false
    @Published var _selectedCategoryObject = RecipeCategoryHash("", nil)
    
    @Published var _isRecipeSelected = false
    @Published var _selectedRecipeId = ""
    
    @Published var _tabViewSelection = 0
    @Published var _tabSelectedExplore = "Explore"
    
    @Published var _searchTerm = ""
    @Published var _searchResults = [RecipeHash]()
    
    var tabSelectedExplore: String {
        get {
            return _tabSelectedExplore
        }
        set {
            _tabSelectedExplore = newValue
        }
    }
    
    var isCategorySelected: Bool {
        get {
            return _isCategorySelected
        }
        set {
            _isCategorySelected = newValue
        }
    }

    var selectedCategoryObject: RecipeCategoryHash {
        get {
            return _selectedCategoryObject
        }
        set {
            _selectedCategoryObject = newValue
        }
    }
    
    var isRecipeSelected: Bool {
        get {
            return _isRecipeSelected
        }
        set {
            _isRecipeSelected = newValue
        }
    }

    var selectedRecipeId: String {
        get {
            return _selectedRecipeId
        }
        set {
            _selectedRecipeId = newValue
        }
    }
    
    var tabViewSelection: Int {
        get {
            return _tabViewSelection
        }
        set {
            _tabViewSelection = newValue
        }
    }
    
    
    var searchTerm: String {
        get {
            return _searchTerm
        }
        set {
            _searchTerm = newValue
        }
    }
    
    var searchResults: [RecipeHash] {
        get {
            return _searchResults
        }
        set {
            _searchResults = newValue
        }
    }
    
    func resetFields() {
        selectedRecipeId = ""
        isRecipeSelected = false
        selectedCategoryObject = RecipeCategoryHash("", nil)
        isCategorySelected = false
        navigateToHome() 
    }
    
    func navigateToHome() {
        tabViewSelection = 0
    }
    
    func navigateToMealPlan() {
        tabViewSelection = 1
    }
}
