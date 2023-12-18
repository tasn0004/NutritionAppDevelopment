import SwiftUI
import Parse

struct GroceryListWeekly: View {
    
    @Environment(\.colorScheme) var colorScheme
    
    @Binding var screenHeight: Double
    @Binding var screenWidth: Double
    
    
    
    @State var selectedItems: Set<String> = []
    
    //create date format for dictionary keys
    let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMMM d, yyyy"
        return formatter
    }()
    
    @State var weeklyListItems: [GroceryItem] = []
    
    init(_ screenWidth: Binding<Double>, _ screenHeight: Binding<Double>) {
        _screenWidth = screenWidth
        _screenHeight = screenHeight
    }
    
    var body: some View {
        VStack {
            Text("Weekly List")
                .foregroundColor(.white)
                .font(.title)
            
            ZStack {
                Rectangle()
                    .foregroundColor(.orange)
                    .frame(width: .infinity, height: 0.30*screenHeight)
                    .cornerRadius(10)

                ScrollView {
                    VStack() {
                        ForEach(weeklyListItems) { item in
                            HStack {
                                //Toggle logic for items on list
                                Toggle(isOn: Binding(
                                    get: {
                                        return selectedItems.contains(item.name)
                                    },
                                    set: { isSelected in
                                        if isSelected {
                                            selectedItems.insert(item.name)
                                        } else {
                                            selectedItems.remove(item.name)
                                        }
                                    }
                                )) {
                                    //View next to toggle
                                    ZStack {
                                        HStack {
                                            Text(item.name)
                                            Spacer()
                                            Text("\(item.quantity) " + item.quantityUnit)
                                        }
                                        .overlay(
                                            Rectangle()
                                                .frame(height: 1)
                                                .foregroundColor(.black)
                                                .opacity(selectedItems.contains(item.name) ? 1.0 : 0.0)
                                        )
                                    }
                                }
                                .toggleStyle(iOSCheckboxToggleStyleGroceryList(0.05*screenWidth, 0.025*screenHeight))
                                .foregroundColor(selectedItems.contains(item.name) ? Color(.lightGray) : .black)
                                
                                Spacer()
                            }
                            Divider()
                                .background(Color.black)
                        }
                    }
                    .modifier(CustomTextLabelStyle(fontSize: 0.045*screenWidth))
                    .padding(.vertical)
                    
                }//scroll view
                .frame(width: 0.85 * screenWidth, height: 0.30 * screenHeight)
                .clipped()

            }//ZStack
        }//Vstack
        .onAppear() {
            initializeWeeklyListItems()
        }
    }
    
    func initializeWeeklyListItems() {
        
        var tempRecipeIdArray: [String] = []
        
        let mealTypes = ["breakfast", "lunch", "dinner", "snacks", "dessert"]
        
        let daysOfWeek = 7
        var currentDate = calculateFirstDayOfWeek()
        
        let calendar = Calendar.current
        var dateComponents = DateComponents()
        dateComponents.day = 1
        
        //Get current user
        if let currentUser = PFUser.current() {
            
            //Get the meal plan dictionaries
            var mealPlanDictionary = currentUser["mealPlan"] as? [String: [String: [String]]] ?? [:]

            //loop through all 7 days of meal plans starting from user preferred start day
            for day in 0..<daysOfWeek {
                
                //format the current date to use as key of meal plan dictionary
                let formattedDate = dateFormatter.string(from: currentDate)
                var currentMealPlan = mealPlanDictionary[formattedDate] ?? [:]
                
                //loop through each meal type array
                for mealType in mealTypes {
                    var mealTypeArray = currentMealPlan[mealType] ?? []
                    
                    //loop through each recipeId and append to local copy of all recipe ids
                    for recipe in mealTypeArray {
                        tempRecipeIdArray.append(recipe)
                    }
                }
                
                //add 1 day to the current date to get the next days meal plan
                if let newDate = calendar.date(byAdding: dateComponents, to: currentDate) {
                    currentDate = newDate
                }

            }

            //Loop through the array of recipe IDs for the meal type. When it ends, we have all ingredient data for selected day stored and ready for use.
            for recipeId in tempRecipeIdArray {
                let query = PFQuery(className: "Recipes")
                
                // Search for the recipe by its ID
                query.getObjectInBackground(withId: recipeId) { (recipe: PFObject?, error: Error?) in
                    if let error = error {
                        print("Error fetching recipe with id: \(recipeId) \(error.localizedDescription)")
                        
                    } else if let recipe = recipe, let ingredientsArray = recipe["ingredients"] as? [[Any]] {
                        
                        //add each ingredient array into local copy
                        for ingredient in ingredientsArray {
                            if let ingredientName = ingredient[0] as? String, let ingredientQuantity = ingredient[1] as? Double {
                                
                                var existingIndex = -1
                                
                                //if the ingredient already exists in daily list items
                                if doesIngredientExist(weeklyListItems, ingredientName, &existingIndex) {
                                    
                                    var existingQuantity = weeklyListItems[existingIndex].quantity
                                    weeklyListItems[existingIndex].quantity = existingQuantity + ingredientQuantity
                                    
                                    
                                } else {
                                    //else append as new grocery item to dailyListItems array
                                    if let ingredientQuantityUnit = ingredient[2] as? String {
                                        var newGroceryItem = GroceryItem(name: ingredientName, quantity: ingredientQuantity, quantityUnit: ingredientQuantityUnit)
                                        
                                        weeklyListItems.append(newGroceryItem)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /*
        Loops through dailyListItems array checking if an ingredient already exists.
     */
    func doesIngredientExist(_ listItems: [GroceryItem], _ ingredientName: String, _ existingIndex: inout Int) -> Bool {
        for (index, ingredient) in listItems.enumerated() {
            if ingredient.name == ingredientName {
                existingIndex = index
                return true
            }
        }
        return false
    }

    /*
        Returns the date of the first day of week determined by the users preferred start day.
     */
    func calculateFirstDayOfWeek() -> Date {
        
        var currentDate = Date()
        
        //create date format for dictionary keys
        let dateFormatter: DateFormatter = {
            let formatter = DateFormatter()
            formatter.dateFormat = "E"
            return formatter
        }()
        
        //get day of current day
        var currentDayString = dateFormatter.string(from: currentDate)
        
        if let currentUser = PFUser.current(),
           let userPreferredStartDay = currentUser["preferredStartDayOfWeek"] as? String {
            //if the current day is already the first day of the week, just return the current date
            if userPreferredStartDay == currentDayString {
                return currentDate
            }
            
            let calendar = Calendar.current
            var dateComponents = DateComponents()
            dateComponents.day = -1
            
            //loop backwards until we are at the date of the last userPreferredStartDay
            while(true) {
                //find the new date and set as value for selectedDate
                if let newDate = calendar.date(byAdding: dateComponents, to: currentDate) {
                    currentDate = newDate
                    
                    currentDayString = dateFormatter.string(from: currentDate)
                    
                    if userPreferredStartDay == currentDayString {
                        return currentDate
                    }
                }
            }
        }
        return currentDate
    }
    
}
