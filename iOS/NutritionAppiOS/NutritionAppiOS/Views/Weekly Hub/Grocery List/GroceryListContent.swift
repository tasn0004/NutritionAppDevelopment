import SwiftUI
import Parse

struct GroceryListContent: View {
    
    @EnvironmentObject private var backendUtilities: BackendUtilities
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0

    //Alerts controls
    @State private var isAlertPresented = false
    @State private var alertTitle = ""
    @State private var alertMessage = ""
    
    //Date control
    @State private var selectedDate = Date()
    @State private var selectedDay = "--"
    
    //Flags
    @State var shouldRefresh = false
    @State var initialLoad = true

    //Collections for grocery list items
    @State var listItems: [String: [GroceryItem]] = [:]
    @State var weeklyListItems: [String: GroceryItem] = [:]
    
    var listType: String
    
    let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMMM d, yyyy"
        return formatter
    }()
    
    init(_ listType: String) {
        self.listType = listType
    }
    
    /*
     
        View body
     
     */
    var body: some View {
        VStack {
            //Date selector when on daily
            SevenDayDateSelector($selectedDate, $selectedDay)
                .opacity(listType == "daily" ? 1.0 : 0.0)
                .disabled(listType != "daily")

            listTitle()
                .padding(.vertical)
            
            ScrollView {
                listView()
                
                if listType == "daily" {
                    AddItemButton($shouldRefresh, $selectedDate)
                        .padding(.top)
                }
            }
            .clipped()
            .padding(.horizontal)
        }
        .onChange(of: shouldRefresh) { _ in
            shouldRefresh = false
            initializeListItems()
        }
        .onChange(of: selectedDate) { _ in
            handleSelectedDateChange()
        }
        .onAppear() {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
            initializeListItems()
        }
    }
    
    /*
     
        Called every time the date selected is changed. Has logic to handle initial load tracking, otherwise should refresh would trigger redundant reinitialization on initial page load.
     
     */
    func handleSelectedDateChange() {
        switch initialLoad {
        case true:
            initialLoad = false
        case false:
            shouldRefresh = true
        }
    }
    
    /*
     
        Displays the correct list that corresponds to listType.
     
     */
    func listView() -> some View {
        return listType == "daily" ? AnyView(dailyListView()) : AnyView(weeklyListView())
    }
    
    /*
     
        Returns a formatted list title depending on list type.
     
     */
    func listTitle() -> some View {
        Text(listType == "daily" ? "Daily List" : "Weekly List")
            .foregroundColor(.white)
            .modifier(CustomTextLabelStyle(0.08*screenWidth, false))
    }
    
    /*

        Creates the list content background rectangle.

    */
    func listBackgroundRectangle() -> some View {
        RoundedRectangle(cornerRadius: 20)
            .foregroundColor(.orange)
            .shadow(radius: 10)
    }
    
    /*
     
        Loops through listItems to create the daily list content.
     
     */
    func dailyListView() -> some View {
        LazyVStack {
            ForEach(listItems.sorted(by: { $0.key < $1.key }), id: \.key) { date, ingredients in
                ForEach(ingredients) { ingredient in
                    DailyListItem(ingredient, ingredients, date, $listItems, $shouldRefresh)
                    Divider()
                }
            }
        }
        .padding()
        .background(
            listBackgroundRectangle()
        )
    }

    /*
     
        Loops through weeklyListItems to create the weekly list content.
     
     */
    func weeklyListView() -> some View {
        LazyVStack {
            ForEach(weeklyListItems.sorted(by: { $0.key < $1.key }), id: \.key) { key, ingredient in
                Toggle(isOn: Binding<Bool>(
                    get: { ingredient.isToggled },
                    set: { newValue in
                        onToggleWeekly(ingredient, newValue)
                    }
                )) {
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
                
                Divider()
            }
        }
        .padding()
        .background(
            listBackgroundRectangle()
        )
    }
    
    /*
     
        Gets the initial toggle state value ofr an ingredinet on the weekly list. A weekly list item is toggled when all instances of that ingredient in the grocery list has isToggled = true.
        Function returns false if any isToggled = false.
     
     */
    func getInitialToggleStateWeekly(_ ingredientToggle: GroceryItem) -> Bool {
        //Check all isToggled values for this ingredient in listItems(groceryList)
        for(_, ingredients) in listItems {
            for ingredient in ingredients {
                //find ingredient being toggled in grocery list
                if ingredient.name == ingredientToggle.name {
                    //if isToggled is false then weekly list item toggle is false.
                    if !ingredient.isToggled {
                        return false
                    }
                }
            }
        }
        //all instances of weekly list item in grocery list isToggled state = true
        return true
    }
    
    /*
     
        Returns the quantity for a weekly list item based on the the toggle states of all daily list items.
     
     */
    func calculateWeeklyItemQuantity(_ ingredientName: String) -> Double {
        var finalQuantity = 0.0
        
        //Loop through daily list items and find match with ingredientName. Add matched item quantity to final quantity if untoggled.
        for (_, items) in listItems {
            for item in items {
                if item.name == ingredientName && !item.isToggled {
                    finalQuantity += item.quantity
                }
            }
        }
        return finalQuantity
    }
    
    /*
     
        Sets the isToggled value for an ingredient item in the weekly list.
     
     */
    func onToggleWeekly(_ ingredient: GroceryItem, _ newValue: Bool) {
        //loop through daily list items and set toggle state for each matching ingredient to new value
        for (date, items) in listItems {
           if let index = items.firstIndex(where: { $0.name == ingredient.name }) {
               //set toggle state of current daily item
               listItems[date]?[index].isToggled = newValue
           }
        }
        
        //set weekly item toggle state
        weeklyListItems[ingredient.name]?.isToggled = newValue
        
        //when toggle value true, weekly item quantity is 0, else reset
        if newValue {
            weeklyListItems[ingredient.name]?.quantity = 0.0
        } else {
            weeklyListItems[ingredient.name]?.quantity = calculateWeeklyItemQuantity(ingredient.name)
        }
        //save modifications to grocery list
        updateGroceryList()
    }
    
    /*
     
        Creates and returns a new GroceryItem object based on an ingredient object passed in.
     
     */
    func createNewGroceryItem(_ ingredient: [String: Any]) -> GroceryItem {
        let ingredientName = ingredient["ingredientName"] as? String ?? ""
        let ingredientQuantity = ingredient["quantity"] as? Double ?? 0
        let ingredientUnit = ingredient["quantityUnit"] as? String ?? ""
        let mealPlanAdded = ingredient["mealPlanAdded"] as? Bool ?? false
        let isToggled = ingredient["isToggled"] as? Bool ?? false

        return GroceryItem(ingredientName, ingredientQuantity, ingredientUnit, mealPlanAdded, isToggled)
    }
    
    
    /*
     
        Resets both listItems and weeklyListItems. Calls the appropriate initialize function depending on list type.
     
     */
    func initializeListItems() {
        self.listItems = [:]
        self.weeklyListItems = [:]
        
        if listType == "daily" {
            initializeDailyListItems()
        } else {
            initializeWeeklyListItems()
        }
    }
    
    /*
     
        Initializes the daily list items array with ingredient data from the grocery list. Called on page appear and refresh.
     
     */
    func initializeDailyListItems() {
        let formattedDate = dateFormatter.string(from: selectedDate)
        let groceryListDay = backendUtilities.getUserGroceryListDay(formattedDate)
        
        //Reset old data
        listItems[formattedDate] = [GroceryItem]()
        
        //add each ingredient array into local copy
        for ingredient in groceryListDay {
            //skip empty array
            if ingredient.isEmpty {
                continue
            }
            //Create new grocery item from ingredient and append to listItems
            listItems[formattedDate]?.append(createNewGroceryItem(ingredient))
        }
    }
    
    /*
     
        Initializes the weekly list items array with ingredient data from the grocery list. Called on page appear and refresh.
     
     */
    func initializeWeeklyListItems() {
        let userGroceryList = backendUtilities.getUserGroceryList()
        
        //create daily list from all 7 days of grocery list
        for (date, ingredients) in userGroceryList {
            //create key with empty array of grocery items as value
            listItems[date] = [GroceryItem]()

            for ingredient in ingredients {
                //skip empty array
                if ingredient.isEmpty {
                    continue
                }
                //Create new grocery item from ingredient and append to listItems
                listItems[date]?.append(createNewGroceryItem(ingredient))
            }
        }
        
        //create weekly list
        for (_, items) in listItems {
            for item in items {
                //if item doesnt exist in weekly items, create it
                if !weeklyListItems.keys.contains(item.name) {
                    
                    let toggleState = getInitialToggleStateWeekly(item)
                    
                    weeklyListItems[item.name] = item
                    weeklyListItems[item.name]?.isToggled = toggleState
                    weeklyListItems[item.name]?.quantity = calculateWeeklyItemQuantity(item.name)
                    
                    //if all daily list items for this weekly item are toggled, then set the quantity to 0
                    if toggleState == true {
                        self.weeklyListItems[item.name]?.quantity = 0.0
                    }
                }
            }
        }
    }
    
    /*
     
        Updates the grocery list with the new isToggled values if modifed on the list.
     
     */
    func updateGroceryList() {
        var newGroceryList = [String: [[String: Any]]]()
        var tempIngredientArray = [[String: Any]]()

        //loop through the current state of listItems and create a dictionary to swap with the groceryList
        for (date, items) in listItems {
            //Skip if empty ingredient
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







