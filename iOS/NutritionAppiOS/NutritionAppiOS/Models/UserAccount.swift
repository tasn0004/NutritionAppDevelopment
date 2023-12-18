import Foundation
import Parse

class UserAccount: ObservableObject {

    private var _firstName: String
    private var _lastName: String
    private var _emailAddress: String
    private var _password: String
    private var _sex: String
    private var _birthdate: Date
    private var _weight: Double
    private var _weightUnit: String
    private var _height: Double
    private var _heightUnit: String
    private var _wristCircumference: Double
    private var _wristCircumferenceUnit: String
    private var _ethnicity: String
    private var _activityLevel: Double
    private var _dietPreferences: [String]
    private var _healthConcerns: [String]
    private var _weightManagementGoal: String
    private var _preferredStartDayOfWeek: String
    private var _favorites: [String]
    private var _likedRecipes: [String]
    private var _mealPlan: UserMealPlan?
    private var _groceryList: UserGroceryList?
    private var _nutritionProfile: UserNutritionProfile
    
    init() {
        _firstName = ""
        _lastName = ""
        _emailAddress = ""
        _password = ""
        _sex = ""
        _birthdate = Date()
        _weight = 0.0
        _weightUnit = ""
        _height = 0.0
        _heightUnit = ""
        _wristCircumference = 0.0
        _wristCircumferenceUnit = ""
        _ethnicity = ""
        _activityLevel = 0.0
        _dietPreferences = []
        _healthConcerns = []
        _weightManagementGoal = ""
        _preferredStartDayOfWeek = ""
        _favorites = []
        _likedRecipes = []
        _nutritionProfile = UserNutritionProfile()
    }
    
    // Getter and Setter for firstName
    var firstName: String {
        get {
            return self._firstName
        }
        set(newFirstName) {
            self._firstName = newFirstName
        }
    }
    
    // Getter and Setter for lastName
    var lastName: String {
        get {
            return self._lastName
        }
        set(newLastName) {
            self._lastName = newLastName
        }
    }
    
    // Getter and Setter for emailAddress
    var emailAddress: String {
        get {
            return self._emailAddress
        }
        set(newEmailAddress) {
            self._emailAddress = newEmailAddress
        }
    }
    
    // Getter and Setter for password
    var password: String {
        get {
            return self._password
        }
        set(newPassword) {
            self._password = newPassword
        }
    }
    
    // Getter and Setter for sex
    var sex: String {
        get {
            return self._sex
        }
        set(newSex) {
            self._sex = newSex
        }
    }
    
    // Getter and Setter for birthdate
    var birthdate: Date {
        get {
            return self._birthdate
        }
        set(newBirthdate) {
            self._birthdate = newBirthdate
        }
    }
    
    // Getter and Setter for weight
    var weight: Double {
        get {
            return self._weight
        }
        set(newWeight) {
            self._weight = newWeight
        }
    }
    
    // Getter and Setter for weightUnit
    var weightUnit: String {
        get {
            return self._weightUnit
        }
        set(newWeightUnit) {
            self._weightUnit = newWeightUnit
        }
    }
    
    // Getter and Setter for heightInches
    var height: Double {
        get {
            return self._height
        }
        set(newHeight) {
            self._height = newHeight
        }
    }
    
    // Getter and Setter for heightUnit
    var heightUnit: String {
        get {
            return self._heightUnit
        }
        set(newHeightUnit) {
            self._heightUnit = newHeightUnit
        }
    }
    
    // Getter and Setter for wristCircumference
    var wristCircumference: Double {
        get {
            return self._wristCircumference
        }
        set(newWristCircumference) {
            self._wristCircumference = newWristCircumference
        }
    }
    
    // Getter and Setter for wristCircumferenceUnit
    var wristCircumferenceUnit: String {
        get {
            return self._wristCircumferenceUnit
        }
        set(newWristCircumferenceUnit) {
            self._wristCircumferenceUnit = newWristCircumferenceUnit
        }
    }
    
    // Getter and Setter for ethnicity
    var ethnicity: String {
        get {
            return self._ethnicity
        }
        set(newEthnicity) {
            self._ethnicity = newEthnicity
        }
    }
    
    // Getter and Setter for activityLevel
    var activityLevel: Double {
        get {
            return self._activityLevel
        }
        set(newActivityLevel) {
            self._activityLevel = newActivityLevel
        }
    }
    
    // Getter and Setter for dietPreferences
    var dietPreferences: [String] {
        get {
            return self._dietPreferences
        }
        set(newDietPreferences) {
            self._dietPreferences = newDietPreferences
        }
    }
    
    // Getter and Setter for healthConcerns
    var healthConcerns: [String] {
        get {
            return self._healthConcerns
        }
        set(newHealthConcerns) {
            self._healthConcerns = newHealthConcerns
        }
    }
    
    // Getter and Setter for healthConcerns
    var weightManagementGoal: String {
        get {
            return self._weightManagementGoal
        }
        set(newWeightManagementGoal) {
            self._weightManagementGoal = newWeightManagementGoal
        }
    }
    
    // Getter and Setter for healthConcerns
    public var preferredStartDayOfWeek: String {
        get {
            return self._preferredStartDayOfWeek
        }
        set(newPreferredStartDayOfWeek) {
            self._preferredStartDayOfWeek = newPreferredStartDayOfWeek
        }
    }
    
    /*
        Used at the end of the account creation process to add the new user to the user table.
     */
    func signUp() {
        
        let user = PFUser()
        
        user.username = emailAddress
        user.email = emailAddress
        user.password = password
        
        user["isPaidAccount"]  = true
        
        user["firstName"] = firstName
        user["lastName"] = lastName
        user["birthdate"] = birthdate
        user["sex"] = sex
        user["height"] = height
        user["heightUnit"] = heightUnit
        user["weight"] = weight
        user["weightUnit"] = weightUnit
        user["wristCircumference"] = wristCircumference
        user["wristCircumferenceUnit"] = wristCircumferenceUnit
        user["ethnicity"] = ethnicity
        user["activityLevel"] = activityLevel
        user["dietPreferences"] = dietPreferences
        user["healthConcerns"] = healthConcerns
        user["weightManagementGoal"] = weightManagementGoal
        user["preferredStartDayOfWeek"] = preferredStartDayOfWeek
        user["favorites"] = _favorites
        user["likedRecipes"] = _likedRecipes
        
        let mealPlan = UserMealPlan(preferredStartDayOfWeek)
        user["mealPlan"] = mealPlan.mealPlanDictionary
        
        let groceryList = UserGroceryList(preferredStartDayOfWeek)
        user["groceryList"] = groceryList.groceryListDictionary
        
        user["nutritionProfile"] = createNutritionProfile()

        user.signUpInBackground {
            (success: Bool, error: Error?) in
                if (success) {
                    print("Successful user sign up\n")
                    
                } else {
                    print("Unsuccessful user sign up\n")
                }
        }
    }
    
    func createNutritionProfile() -> [String: [Any]] {
        var nutritionProfileHeight = height
        var nutritionProfileWeight = weight
        
        //if height is in inches convert to cm
        if heightUnit == "in" {
            nutritionProfileHeight = height * 2.54
        }
        //if weight is in pounds convert to kg
        if weightUnit == "lb" {
            nutritionProfileWeight = weight / 2.2
        }
        
        let calories = _nutritionProfile.calculateTotalCalories(sex, nutritionProfileWeight, nutritionProfileHeight, calculateAge(birthdate), activityLevel, weightManagementGoal)
        let nutritionProfile = UserNutritionProfile(sex, calories)
        nutritionProfile.setNutrientValuesByModifier("hypertension")
        
        return nutritionProfile.nutritionProfileDictionary
    }
    
    func calculateAge(_ birthdate: Date) -> Double {
        let calendar = Calendar.current
        let now = Date()
        
        let ageComponents = calendar.dateComponents([.year], from: birthdate, to: now)
        let age = Double(ageComponents.year ?? 0)
        
        return age
    }
    
}
