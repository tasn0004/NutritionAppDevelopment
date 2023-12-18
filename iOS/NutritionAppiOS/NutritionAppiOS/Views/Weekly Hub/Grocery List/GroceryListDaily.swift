import SwiftUI
import Parse

struct GroceryListDaily: View {
    
    @Environment(\.colorScheme) var colorScheme
    
    @Binding var screenHeight: Double
    @Binding var screenWidth: Double
    
    @Binding private var selectedDate: Date
    @State private var selectedDay = "--"
    
    @State var selectedItems: Set<String> = []
    
    @State var dailyListItems: [GroceryItem] = []
    
    init(_ selectedDate: Binding<Date>, _ screenWidth: Binding<Double>, _ screenHeight: Binding<Double>) {
        _selectedDate = selectedDate
        _screenWidth = screenWidth
        _screenHeight = screenHeight
    }
    
    var body: some View {
        VStack {
            Text("Daily List")
                .foregroundColor(.white)
                .font(.title)
            /*
                Date selector
             */
            SevenDaySelectorView($selectedDate, $selectedDay, screenWidth, screenHeight)
            
            ZStack {
                Rectangle()
                    .foregroundColor(.orange)
                    .frame(width: .infinity, height: 0.30*screenHeight)
                    .cornerRadius(10)

                ScrollView {
                    VStack() {
                        ForEach(dailyListItems) { item in
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
            initializeDailyListItems()
        }
        .onChange(of: selectedDate) { newValue in
            initializeDailyListItems()
        }
        
    }
    
    func initializeDailyListItems() {
        
        var tempRecipeIdArray: [String] = []
        
        let mealTypes = ["breakfast", "lunch", "dinner", "snacks", "dessert"]
        
        //create date format for dictionary keys
        let dateFormatter: DateFormatter = {
            let formatter = DateFormatter()
            formatter.dateFormat = "MMMM d, yyyy"
            return formatter
        }()
        
        //Get current user
        if let currentUser = PFUser.current() {
            
            //Get the meal plan dictionaries
            var mealPlanDictionary = currentUser["mealPlan"] as? [String: [String: [String]]] ?? [:]
            
            dailyListItems = []
            
            //Retrieve the selected days meal plan
            let formattedDate = dateFormatter.string(from: selectedDate)
            var currentMealPlan = mealPlanDictionary[formattedDate] ?? [:]
            
            //loop through each meal type array
            for mealType in mealTypes {
                var mealTypeArray = currentMealPlan[mealType] ?? []
                
                
                //loop through each recipeId and append to local copy
                for recipe in mealTypeArray {
                    tempRecipeIdArray.append(recipe)
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
                                if doesIngredientExist(dailyListItems, ingredientName, &existingIndex) {
                                    
                                    var existingQuantity = dailyListItems[existingIndex].quantity
                                    dailyListItems[existingIndex].quantity = existingQuantity + ingredientQuantity
                                    
                                    
                                } else {
                                    //else append as new grocery item to dailyListItems array
                                    if let ingredientQuantityUnit = ingredient[2] as? String {
                                        var newGroceryItem = GroceryItem(name: ingredientName, quantity: ingredientQuantity, quantityUnit: ingredientQuantityUnit)
                                        
                                        dailyListItems.append(newGroceryItem)
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
    func doesIngredientExist(_ dailyListItems: [GroceryItem], _ ingredientName: String, _ existingIndex: inout Int) -> Bool {
        for (index, ingredient) in dailyListItems.enumerated() {
            if ingredient.name == ingredientName {
                existingIndex = index
                return true
            }
        }
        return false
    }
}
