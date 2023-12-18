import Parse

class BackendUtilities: ObservableObject  {
    
    @Published var _currentUser: PFUser?
    @Published var _isUserPaidAccount: Bool?
    
    /*
     
        Get/Set current user
     
     */
    var currentUser: PFUser {
        get {
            return _currentUser ?? PFUser.init()
        }
        set {
            _currentUser = newValue
        }
    }
    
    /*
     
        Get/Set is user paid account
     
     */
    var isUserPaidAccount: Bool {
        get {
            return _isUserPaidAccount ?? false
        }
        set {
            _isUserPaidAccount = newValue
        }
    }
    
    /*

        Initializes current user as the current user in the environment.

    */
    func initializeCurrentUser() {
        if let currentUserTemp = PFUser.current() {
            _currentUser = currentUserTemp
        }
    }
    
    /*

        Resets the current user in the environment to be an empty user.

    */
    func resetCurrentUser() {
        currentUser = PFUser()
    }
    
    /*

        Initializes is user paid account with value from current user

    */
    func initializeIsUserPaidAccount() {
        isUserPaidAccount = currentUser["isPaidAccount"] as? Bool ?? false
    }
    
    /*

       Returns the full name of the user..

    */
    func getUserFullName() -> String {
        let firstName = currentUser["firstName"] as? String ?? ""
        let lastName = currentUser["lastName"] as? String ?? ""
        return "\(firstName) \(lastName)"
    }

    /*
     
        Returns the current user's preferred start day for the week.
     
     */
    func getUserPreferredStartDay() -> String {
       return currentUser["preferredStartDayOfWeek"] as? String ?? ""
    }
    
    /*
     
        Returns the current user's health concerns.
     
     */
    func getUserHealthConcerns() -> [String] {
       return currentUser["healthConcerns"] as? [String] ?? []
    }
    
    /*
     
        Returns the current user's diet preferences.
     
     */
    func getUserDietPreferences() -> [String] {
       return currentUser["dietPreferences"] as? [String] ?? []
    }

    /*
     
        Returns the current user's nutrition profile dictionary.
     
     */
    func getUserNutritionProfile() -> [String: [Any]] {
        if let userNutritionProfile = currentUser["nutritionProfile"] as? [String: [Any]] {
            return userNutritionProfile
        }
        print("Unsuccessfully returned user's nutrition profile.\n")
        return [:]
    }
    
    /*
     
        Returns the current user's meal plan dictionary.
     
     */
    func getUserMealPlan() -> [String: [String: [String]]] {
        if let mealPlanDictionary = currentUser["mealPlan"] as? [String: [String: [String]]] {
            return mealPlanDictionary
        }
        print("Unsuccessfully returned user's meal plan.\n")
        return [:]
    }
    
    /*
     
        Returns the current user's meal plan for a specified day.
     
     */
    func getUserMealPlanDay(_ formattedDate: String) -> [String: [String]] {
        if let mealPlanDay = getUserMealPlan()[formattedDate] {
            return mealPlanDay
        }
        print("Unsuccessfully returned user's meal plan for " + formattedDate + "\n")
        return [:]
    }
    
    /*
     
        Returns the current user's meal plan history dictionary.
     
     */
    func getUserMealPlanHistory() -> [String: [String: [String]]] {
        if let userMealPlanHistory = currentUser["mealPlanHistory"] as? [String: [String: [String]]] {
            return userMealPlanHistory
        }
        print("Unsuccessfully returned user's meal plan history.\n")
        return [:]
    }
    
    /*
     
        Returns the current user's grocery list dictionary.
     
     */
    func getUserGroceryList() -> [String: [[String: Any]]] {
        if let userGroceryList = currentUser["groceryList"] as? [String: [[String: Any]]] {
            return userGroceryList
        }
        print("Unsuccessfully returned user's grocery list.\n")
        return [:]
    }
    
    /*
     
        Returns the current user's grocery list for a specified day.
     
     */
    func getUserGroceryListDay(_ formattedDate: String) -> [[String: Any]] {
        if let groceryListDay = getUserGroceryList()[formattedDate] {
            return groceryListDay
        }
        print("Unsuccessfully returned user's grocery list for " + formattedDate + "\n")
        return [[:]]
    }
    
    /*
     
        Returns a string array containing the user's favourited recipe ids.
     
     */
    func getUserFavouritedRecipes() -> [String] {
        if let favouritesRecipeIds = currentUser["favouritedRecipes"] as? [String] {
            return favouritesRecipeIds
        }
        print("Unsuccessfully returned user's favourited recipes list.\n")
        return []
    }
    
    /*
     
        Returns a string array containing the user's liked recipe ids.
     
     */
    func getUserLikedRecipes() -> [String] {
        if let likedRecipeIds = currentUser["likedRecipes"] as? [String] {
            return likedRecipeIds
        }
        print("Unsuccessfully returned user's liked recipes list.\n")
        return []
    }
    
    
    /*
     
        Returns a supplied recipe object's image.
     
     */
    func getRecipeImage(_ recipe: PFObject, completion: @escaping (UIImage?) -> Void) {
        if let imageFile = recipe["image"] as? PFFileObject {
            imageFile.getDataInBackground { (data, error) in
                if let data = data, let image = UIImage(data: data) {
                    completion(image)
                } else {
                    print("Error fetching image data from image file: \(error?.localizedDescription ?? "Unknown error")\n")
                    completion(nil)
                }
            }
        } else {
            completion(nil)
        }
    }
    
    /*
     
        Returns a supplied category object's image.
     
     */
    func getCategoryImage(_ category: PFObject, completion: @escaping (UIImage?) -> Void) {
        if let imageFile = category["categoryImage"] as? PFFileObject {
            imageFile.getDataInBackground { (data, error) in
                if let data = data, let image = UIImage(data: data) {
                    completion(image)
                } else {
                    print("Error fetching image data from image file: \(error?.localizedDescription ?? "Unknown error")\n")
                    completion(nil)
                }
            }
        } else {
            completion(nil)
        }
    }
    
    /*
     
        Updates the user's liked recipes array by adding or removing a recipe id string.
     
     */
    func updateUserLikedRecipes(_ recipeId: String, _ isLiked: Bool, completion: @escaping (Bool) -> Void) {
        //Add or remove the recipe id from the user's favourites list depending on operation.
        if !isLiked {
            currentUser.addUniqueObject(recipeId, forKey: "likedRecipes")
            print("Adding recipeId: \(recipeId) to user's liked recipes list.\n")
        } else {
            currentUser["likedRecipes"] = (currentUser["likedRecipes"] as? [String] ?? []).filter { $0 != recipeId }
            print("Removing recipeId: \(recipeId) from user's liked recipes list.\n")
        }
        
        currentUser.saveInBackground { (success, error) in
            if let error = error {
                print("Unsuccessfully updated user's liked recipes list. \(error.localizedDescription)\n")
                completion(false)
            } else if success {
                print("Successfully updated user's liked recipes list.\n")
                completion(true)
            }
        }
    }
    
    /*
     
        Updates the user's favourited recipes array by adding or removing a recipe id string.
     
     */
    func updateUserFavouritedRecipes(_ recipeId: String, _ isFavourited: Bool, completion: @escaping (Bool) -> Void) {
        //Add or remove the recipe id from the user's favourites list depending on operation.
        if !isFavourited {
            currentUser.addUniqueObject(recipeId, forKey: "favouritedRecipes")
            print("Adding recipeId: \(recipeId) to user's favourited recipes list.\n")
        } else {
            currentUser["favouritedRecipes"] = (currentUser["favouritedRecipes"] as? [String] ?? []).filter { $0 != recipeId }
            print("Removing recipeId: \(recipeId) from user's favourited recipes list.\n")
        }
        
        currentUser.saveInBackground { (success, error) in
            if let error = error {
                print("Unsuccessfully updated user's favourited recipes list. \(error.localizedDescription)\n")
                completion(false)
            } else if success {
                print("Successfully updated user's favourited recipes list.\n")
                completion(true)
            }
        }
    }

    /*
     
        Updates the user's grocery list dictionary with the new collection newGroceryList.
     
     */
    func updateUserGroceryList(_ newGroceryList: [String: [[String: Any]]], completion: @escaping (Bool) -> Void) {
        //replace existing grocery list with modified one
        currentUser["groceryList"] = newGroceryList
        print("Updating grocery list to: \n\(newGroceryList)\n")

        currentUser.saveInBackground { (success, error) in
            if let error = error {
                print("Unsuccessfully updated user's grocery list. \(error.localizedDescription)\n")
                completion(false)
            } else if success {
                print("Successfully updated user's grocery list.\n")
                completion(true)
            }
        }
    }
    
    /*
     
        Updates the user's meal plan dictionary with the new collection newMealPlan.
     
     */
    func updateUserMealPlan(_ newMealPlan: [String: [String: [String]]], completion: @escaping (Bool) -> Void) {
        //replace existing meal plan with modified one
        currentUser["mealPlan"] = newMealPlan
        print("Updating meal plan to: \n\(newMealPlan)\n")

        currentUser.saveInBackground { (success, error) in
            if let error = error {
                print("Unsuccessfully updated user's meal plan. \(error.localizedDescription)\n")
                completion(false)
            } else if success {
                print("Successfully updated user's meal plan.\n")
                completion(true)
            }
        }
    }
    
    /*
     
        Updates the user's health concerns array with the new collection newHealthConcerns.
     
     */
    func updateUserHealthConcerns(_ newHealthConcerns: [String], completion: @escaping (Bool) -> Void) {
        //replace existing meal plan with modified one
        currentUser["healthConcerns"] = newHealthConcerns
        print("Updating meal plan to: \n\(newHealthConcerns)\n")

        currentUser.saveInBackground { (success, error) in
            if let error = error {
                print("Unsuccessfully updated user's health concerns. \(error.localizedDescription)\n")
                completion(false)
            } else if success {
                print("Successfully updated user's health concerns.\n")
                completion(true)
            }
        }
    }
    
    /*
     
        Updates the user's diet preferences array with the new collection newHealthConcerns.
     
     */
    func updateUserDietPreferences(_ newDietPreferences: [String], completion: @escaping (Bool) -> Void) {
        //replace existing meal plan with modified one
        currentUser["dietPreferences"] = newDietPreferences
        print("Updating diet preferences to: \n\(newDietPreferences)\n")

        currentUser.saveInBackground { (success, error) in
            if let error = error {
                print("Unsuccessfully updated user's diet preferences. \(error.localizedDescription)\n")
                completion(false)
            } else if success {
                print("Successfully updated user's diet preferences.\n")
                completion(true)
            }
        }
    }
    
    /*
     
        Reinitializes the user's grocery list data with a new one starting from the last user preferred start day.
     
     */
    func resetGroceryListData() {
        let newGroceryList = UserGroceryList(getUserPreferredStartDay())
        
        currentUser["groceryList"] = newGroceryList.groceryListDictionary
        print("Updating grocery list to: \n\(newGroceryList.groceryListDictionary)\n")
        
        currentUser.saveInBackground { (success, error) in
            if let error = error {
                print("Unsuccessfully reset user's grocery list. \(error.localizedDescription)\n")
            } else if success {
                print("Successfully reset user's grocery list.\n")
            }
        }
    }
    
    /*
     
        Reinitializes the user's meal plan data with a new one starting from the last user preferred start day.
     
     */
    func resetMealPlanData() {
        let newMealPlan = UserMealPlan(getUserPreferredStartDay())
        let oldMealPlan = getUserMealPlan()
        var mealPlanHistory = getUserMealPlanHistory()
        
        //Loop through each day of the current meal plan and add to user's meal plan history
        for (date, mealPlanDay) in oldMealPlan {
            mealPlanHistory[date] = mealPlanDay
        }
        
        currentUser["mealPlanHistory"] = mealPlanHistory
        currentUser["mealPlan"] = newMealPlan.mealPlanDictionary
        print("Updating meal plan to: \n\(newMealPlan)\n")
        
        currentUser.saveInBackground { (success, error) in
            if let error = error {
                print("Unsuccessfully reset user's meal plan. \(error.localizedDescription)\n")
            } else if success {
                print("Successfully reset user's meal plan.\n")
            }
        }
    }
}
