import SwiftUI
import Parse

struct DailyListItem: View {

    @EnvironmentObject private var backendUtilities: BackendUtilities
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0
    
    //Alerts controls
    @State private var isAlertPresented = false
    @State private var alertTitle = ""
    @State private var alertMessage = ""
    
    //Animation controls
    @GestureState private var translation: CGSize = .zero
    @State private var offset: CGFloat = 0
    @State private var swipeCrossedThreshold = false
    
    var ingredient: GroceryItem
    var ingredients: [GroceryItem]
    var date: String
    
    @Binding var listItems: [String: [GroceryItem]]
    @Binding var shouldRefresh: Bool
    
    init(_ ingredient: GroceryItem, _ ingredients: [GroceryItem], _ date: String, _ listItems: Binding<[String: [GroceryItem]]>, _ shouldRefresh: Binding<Bool>) {
        self.ingredient = ingredient
        self.ingredients = ingredients
        self.date = date
        self._listItems = listItems
        self._shouldRefresh = shouldRefresh
    }
    
    /*
     
        View body
     
     */
    var body: some View {
        ZStack {
            // Toggle item
            Toggle(
                isOn: Binding<Bool>(
                    get: { ingredient.isToggled },
                    set: { newValue in
                        onToggleDaily(date, ingredient, ingredients, newValue)
                    }
                )
            ) {
                ZStack {
                    HStack {
                        Text(ingredient.name)
                        Spacer()
                        Text("\(ingredient.quantity) " + ingredient.quantityUnit)
                    }

                    if ingredient.isToggled {
                        Rectangle()
                            .frame(height: 1)
                            .foregroundColor(.black)
                    }
                }
            }
            .toggleStyle(iOSCheckboxToggleStyleGroceryList())
            .foregroundColor(ingredient.isToggled ? Color(.lightGray) : .black)
        }
        .offset(x: offset + translation.width)
        .highPriorityGesture(
            swipeGesture()
        )
        .alert(alertTitle, isPresented: $isAlertPresented) {
            Button("Cancel", role: .cancel) { print("Cancel") }
            Button("OK") {
                onToggleDaily(date, ingredient, ingredients, true)
            }
        } message: {
            Text(alertMessage)
        }
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
        }
    }
    
    func swipeGesture() -> some Gesture {
        DragGesture()
            .updating($translation) { value, state, _ in
                // Only allow leftward swipe
                if value.translation.width < 0 {
                    state = CGSize(width: max(value.translation.width, -screenWidth / 2), height: 0)
                }
            }
            .onEnded { gesture in
                if -gesture.translation.width > screenWidth / 2 {
                    
                    //if item user added, delete item
                    if !ingredient.mealPlanAdded {
                        deleteItemFromGroceryList(ingredient)
                        
                    } else {
                        self.alertTitle = "Error"
                        self.alertMessage = "You cannot delete ingredients from your meal plan. Would you like to toggle this ingredient instead?"
                        self.isAlertPresented = true
                    }

                    // Reset the offset here to animate back to the starting position
                    withAnimation {
                        self.offset = 0
                    }
                }
            }
    }
    
    /*
     
        Sets the isToggled value for an ingredient item in the daily list.
     
     */
    func onToggleDaily(_ date: String, _ ingredient: GroceryItem, _ ingredients: [GroceryItem], _ newValue: Bool) {
        if let index = ingredients.firstIndex(where: { $0.name == ingredient.name }) {
            listItems[date]?[index].isToggled = newValue
            updateGroceryList()
        }
    }
    
    /*
     
        Deletes a user defined item from the grocery list.
     
     */
    func deleteItemFromGroceryList(_ ingredient: GroceryItem) {
        var newGroceryList = backendUtilities.getUserGroceryList()
        var groceryListDay = newGroceryList[date] as? [[String: Any]] ?? [[:]]
        
        //remove the ingredient from grocery list
        if let index = groceryListDay.firstIndex(where: { $0["ingredientName"] as? String == ingredient.name }) {
            groceryListDay.remove(at: index)
        }
      
        //Update the newGroceryList dictionary with the modified array
        newGroceryList[date] = groceryListDay
        
        backendUtilities.updateUserGroceryList(newGroceryList) { success in
            if success {
                print("Successfully deleted \(ingredient.quantity) \(ingredient.quantityUnit) \(ingredient.name) from user grocery list.\n")
                alertTitle = "Success!"
                alertMessage = "Item successfully deleted from grocery list."
                isAlertPresented = true
                shouldRefresh = true
            } else {
                print("Unsuccessfully deleted \(ingredient.quantity) \(ingredient.quantityUnit) \(ingredient.name) from user grocery list.\n")
                self.alertTitle = "Error"
                self.alertMessage = "Deleting item unsuccessful."
                self.isAlertPresented.toggle()
            }
        }
    }
    
    /*
     
        Updates the grocery list with the new isToggled values if modifed on the list.
     
     */
    func updateGroceryList() {
        var newGroceryList = backendUtilities.getUserGroceryList()
        var tempIngredientArray = [[String: Any]]()

        for (date, items) in listItems {
            if items.isEmpty {
                continue
            }
            
            for ingredient in items {
                let newIngredient: [String: Any] = ["ingredientName": ingredient.name,
                                                    "quantity": ingredient.quantity,
                                                    "quantityUnit": ingredient.quantityUnit,
                                                    "mealPlanAdded": ingredient.mealPlanAdded,
                                                    "isToggled": ingredient.isToggled]
                
                tempIngredientArray.append(newIngredient)
            }
            
            newGroceryList[date] = tempIngredientArray
            tempIngredientArray = [[String: Any]]()
        }
        
        backendUtilities.updateUserGroceryList(newGroceryList) { success in
            if success {
                print("Successfully updated toggle state of grocery list item(s).\n")
            } else {
                print("Unsuccessfully updated toggle state of grocery list item(s).\n")
            }
        }
    }
}
