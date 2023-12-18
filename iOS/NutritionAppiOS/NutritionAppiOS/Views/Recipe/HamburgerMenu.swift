import SwiftUI
import Parse

struct HamburgerMenu: View {
    
    @Environment(\.colorScheme) private var colorScheme
    
    //Environment objects
    @EnvironmentObject private var backendUtilities: BackendUtilities
    @EnvironmentObject private var mealPlanData: MealPlanObservable
    @EnvironmentObject private var homePageData: HomePageObservable
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State private var screenWidth = 0.0
    @State private var screenHeight = 0.0
    
    //Popover controls
    @State private var mealTypeSelectedPopover = "--"
    @State private var mealPlanDateSelected = Date()

    //Flags
    @State private var showingPopover: Bool = false
    @State private var hasPopoverAddBeenSelected = false
    @State private var isMenuVisible = false
    
    private var isMealTypeSelected: Bool {
        return !mealPlanData.mealTypeSelected.isEmpty
    }

    private var isMealPlanDateSelected: Bool {
        return !mealPlanData.dateSelected.isEmpty
    }
    
    //Alerts controls
    @State private var isAlertPresented = false
    @State private var alertTitle = ""
    @State private var alertMessage = ""
    
    private var recipeId: String
    private var ingredients: [[Any]]
    private var videoUrl: String
    private var recipeName: String
    
    init(_ recipeId: String, _ ingredients: [[Any]], _ videoUrl: String, _ recipeName: String) {
        self.recipeId = recipeId
        self.ingredients = ingredients
        self.videoUrl = videoUrl
        self.recipeName = recipeName
    }
    
    /*
     
        View body
     
     */
    var body: some View {
        HStack {
            VStack {
                //Show menu options when opened
                if isMenuVisible {
                    menuOptionsList()
                }
                Spacer()
            }
            
            VStack {
                //Menu button
                Button(action: {
                    isMenuVisible.toggle()
                }) {
                    Image(systemName: "line.3.horizontal")
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 0.08*screenWidth)
                        .foregroundColor(.white)
                        .padding(0.02*screenWidth)
                        .background {
                            RoundedRectangle(cornerRadius: 10)
                                .foregroundColor(Color(.darkGray))
                        }
                }
                //Push up
                Spacer()
            }
        }
        .alert(isPresented: $isAlertPresented) {
            Alert(title: Text(alertTitle), message: Text(alertMessage),
                  dismissButton: .default(Text("OK"),
                      action: {
                        isAlertPresented = false
                      }
                  ))
        }
        .popover(isPresented: $showingPopover) {
            addToMealPlanPopover(isMealTypeSelected)
        }
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
        }
    }
    
    /*
     
        Creates the list of options in the hamburger menu, with the background rectangle.
     
     */
    func menuOptionsList() -> some View {
        VStack(alignment: .leading) {
            addToMealPlanButton()
            
            Rectangle()
                .frame(height: 0.0005*screenHeight)
                .foregroundColor(.black)
                .opacity(0.5)

            shareRecipeButton()
        }
        .frame(width: 0.40*screenWidth, height: 0.10*screenHeight)
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 20)
                .cornerRadius(10)
                .foregroundColor(Color(.darkGray))
            )
    }
    
    /*
     
        Creates the add to meal plan button inside the hamburger menu.
     
     */
    func addToMealPlanButton() -> some View {
        Button(action: {
            onAddToMealPlanSelect()
        }) {
            HStack {
                Text("Add to meal plan")
                    .foregroundColor(.white)
            }
        }
        .contentShape(Rectangle())
    }
    
    /*
     
        Creates the share recipe button inside the hamburger menu.
     
     */
    func shareRecipeButton() -> some View {
        Button(action: {
            onShareRecipeSelect()
        }) {
            Text("Share recipe")
                .foregroundColor(.white)
        }
        .contentShape(Rectangle())
    }
    
    /*
     
        Calls the logic associated with adding a recipe to the meal plan upon "Add to meal plan" being selected in the hamburger menu.
     
     */
    func onAddToMealPlanSelect() {
        //if we have the context for adding a meal to the meal plan
        if isMealTypeSelected && isMealPlanDateSelected {
            print("Adding to meal plan: \(recipeId) under: \(mealPlanData.mealTypeSelected)")
            addToUserMealPlan()
            
            //reset home page environment variables and navigate to meal plan
            isMenuVisible = false
            homePageData.resetFields()
            homePageData.navigateToMealPlan()
            
        } else {
            //else we show the pop over to get the required data
            showingPopover = true
        }
    }
    
    /*
     
        Calls the logic associated with sharing a recipe accross mediums upon "Share recipe" being selected in the hamburger menu.
     
     */
    func onShareRecipeSelect() {
        guard let urlShare = URL(string: "https://www.youtube.com/embed/\(videoUrl)") else {
                return
            }
        let activityVC = UIActivityViewController(activityItems: [urlShare], applicationActivities: nil)
        UIApplication.shared.windows.first?.rootViewController?.present(activityVC, animated: true, completion: nil)
    }
    
    /*
     
        Creates the popover view when a user selects add meal with no date context selected. Contains logic for handling input + adding to meal plan.
     
     */
    func addToMealPlanPopover(_ isMealTypeSelected: Bool) -> some View {
        
        let mealTypeNames = ["--", "breakfast", "lunch", "dinner", "snacks", "dessert"]
        
        let dateFormatter: DateFormatter = {
            let formatter = DateFormatter()
            formatter.dateFormat = "MMMM d, yyyy"
            return formatter
        }()
        
        return
            VStack {
                HStack {
                    // Cancel button
                    Button(action: {
                        mealPlanData.mealTypeSelected = ""
                        showingPopover = false
                        hasPopoverAddBeenSelected = false
                        mealTypeSelectedPopover = "--"
                    }) {
                        Text("Cancel").fontWeight(.semibold)
                    }
                    .foregroundColor(.blue)
                    .padding()
                    
                    Spacer()
                    
                    //Add button
                    Button(action: {
                    }) {
                        Text("Add").fontWeight(.semibold)
                    }
                    .disabled(!isMealTypeSelected) //disable if we dont have the data
                    .simultaneousGesture(TapGesture().onEnded {
                        //Show alert when user presses done without selecting data. Date will be current day by default
                        if !isMealTypeSelected && mealTypeSelectedPopover == "--" {
                            if !hasPopoverAddBeenSelected {
                                hasPopoverAddBeenSelected = true
                            }
                        } else {
                            //We have everything we need to add to the meal plan
                            mealPlanData.mealTypeSelected = mealTypeSelectedPopover
                            mealPlanData.dateSelected = dateFormatter.string(from: mealPlanDateSelected)
                            
                            addToUserMealPlan()
                            
                            //reset home page environment variables and navigate to meal plan
                            homePageData.resetFields()
                            homePageData.navigateToMealPlan()
                            
                            //close popover and trigger navigation to the meal plan page
                            showingPopover = false
                        }
                    })
                    .foregroundColor(.blue)
                    .padding()
                }
                Divider()
                
                Text("Select the date you'd like to add to: ")
                    .font(.title)
                    .padding()
                
                //date picker that sets the value of dateSelected in the meal plan observable
                ZStack {
                    Rectangle()
                        .foregroundColor(ComponentColours.datePicker)
                        .frame(width: 0.3*screenWidth, height: 0.04*screenHeight)
                        .cornerRadius(8)
                    
                    DatePicker("", selection: $mealPlanDateSelected, in: Date()..., displayedComponents: [.date])
                        .labelsHidden()
                        .colorMultiply(colorScheme == .dark ? .white : .black)
                        .foregroundColor(colorScheme == .dark ? Color(.darkGray) : Color(.lightGray))
                        .brightness(0.085)
                        .cornerRadius(40)
                        .frame(width: 0.4*screenWidth, height: 0.04*screenHeight)
                        .clipped()
                }
                Spacer()
                
                Text("Select the meal you'd like to add to: ")
                    .font(.title)
                    .padding()
                
                //drop down that sets the value of mealTypeSelected in the meal plan observable
                Picker("", selection: $mealTypeSelectedPopover) {
                    ForEach(mealTypeNames, id: \.self) { day in
                        Text(day).tag(day)
                    }
                }
                .fixedSize()
                .labelsHidden()
                .colorMultiply(.white)
                .foregroundColor(.white)
                .scaledToFit()
                
                Text("You must pick a meal type.")
                    .foregroundColor(.red)
                    .opacity(hasPopoverAddBeenSelected && mealTypeSelectedPopover == "--" ? 1 : 0)
                
                Spacer()
            }
            .modifier(CustomTextLabelStyle(0.045*screenWidth))
            .background(Color("background"))
    }

    /*
     
        Adds the recipe Id from the recipe's page into the meal plan data structure for current user.
        Uses the context of meal type selected and date selected to add to the correct meal plan locations.
     
     */
    func addToUserMealPlan() {
        var newMealPlan = backendUtilities.getUserMealPlan()
        var mealPlanDay = backendUtilities.getUserMealPlanDay(mealPlanData.dateSelected)
        var mealTypeArray = mealPlanDay[mealPlanData.mealTypeSelected] as? [String] ?? []
        
        //add recipe ID to the array
        mealTypeArray.append(recipeId)
        
        mealPlanDay[mealPlanData.mealTypeSelected] = mealTypeArray
        newMealPlan[mealPlanData.dateSelected] = mealPlanDay

        backendUtilities.updateUserMealPlan(newMealPlan) { success in
            if success {
                print("Successfully updated meal plan after adding recipe.\n")
                addToGroceryList()
                
                mealPlanData.mealTypeSelected = ""
                mealTypeSelectedPopover = "--"
            } else {
                print("Unsuccessfully updated meal plan after adding recipe.\n")
            }
        }
    }
    
    /*
     
        Adds the recipe's ingredients into the users grocery list upon successfully adding it to the meal plan.
     
     */
    func addToGroceryList() {
        var userGroceryList = backendUtilities.getUserGroceryList()
        var groceryListDay = userGroceryList[mealPlanData.dateSelected] as? [[String: Any]] ?? [[:]]
        
        //loop through each ingredient for the recipe, grab the values, and do logic for adding ingredient to grocery list under the selected date
        for ingredient in ingredients {
            
            let ingredientName = ingredient[0] as? String ?? ""
            let ingredientQuantity = ingredient[1] as? Double ?? 0
            let ingredientUnit = ingredient[2] as? String ?? ""
            
            //check if ingredient exists in the grocery list
            if let existingIngredientIndex = groceryListDay.firstIndex(where: { $0["ingredientName"] as? String == ingredientName }) {
                //if exists, increment the existing quantity by the quantity from the recipe
                let existingQuantity = groceryListDay[existingIngredientIndex]["quantity"] as? Double ?? 0
                groceryListDay[existingIngredientIndex]["quantity"] = existingQuantity + ingredientQuantity
                
                //reset toggle state of item if adding modifying the existing quantity
                groceryListDay[existingIngredientIndex]["isToggled"] = false
                
            } else {
                //it doesnt exist and we append to the grocery list day
                let newIngredient: [String: Any] = ["ingredientName": ingredientName, "quantity": ingredientQuantity, "quantityUnit": ingredientUnit, "mealPlanAdded": true, "isToggled": false]
                groceryListDay.append(newIngredient)
            }
            
        }
        //Update the userGroceryList dictionary with the modified array
        userGroceryList[mealPlanData.dateSelected] = groceryListDay
        
        if let currentUser = PFUser.current() {
            //replace existing grocery list with modified one
            currentUser["groceryList"] = userGroceryList
            
            //save to database
            currentUser.saveInBackground { (success, error) in
                if let error = error {
                    print("Unsuccessfully saved current user after adding ingredients to grocery list upon adding recipe to meal plan. \(error.localizedDescription)")
                } else if success {
                    print("Successfully saved current user after adding ingredients to grocery list upon adding recipe to meal plan.")
                }
            }
        }
    }
}
