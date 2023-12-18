import SwiftUI
import Parse
    
struct WeeklyMealPlan: View {
    
    @Environment(\.colorScheme) private var colorScheme
    
    @EnvironmentObject private var backendUtilities: BackendUtilities
    @EnvironmentObject private var homePageData: HomePageObservable
    @EnvironmentObject private var mealPlanData: MealPlanObservable
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0
    
    @State private var selectedDate = Date()
    @State private var selectedDay = "--"
    
    //Meal type arrays
    @State private var breakfastRecipes: [RecipeHash] = []
    @State private var lunchRecipes: [RecipeHash] = []
    @State private var dinnerRecipes: [RecipeHash] = []
    @State private var snacksRecipes: [RecipeHash] = []
    @State private var dessertRecipes: [RecipeHash] = []
    
    @State private var selectedRecipeID: String = ""
    
    @State private var isMealTypeSelected: Bool = false
    
    @State private var shouldRefresh = false
    @State private var initialLoad = true
    
    let mealTypes = ["breakfast", "lunch", "dinner", "snacks", "dessert"]
    let mealTypeTitles = ["Breakfast", "Lunch", "Dinner", "Snacks", "Dessert"]
    
    let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMMM d, yyyy"
        return formatter
    }()
    
    /*
     
        View body.
     
     */
    var body: some View {
        ZStack {
            Color("background").ignoresSafeArea()
            
            VStack() {
                //Date selector
                SevenDayDateSelector($selectedDate, $selectedDay)
                
                //Content area fro user meal plan
                ScrollView {
                    LazyVStack {
                        mealYTypeSections()
                    }
                }
                .frame(width: 0.95*screenWidth)
                .clipped()
            }
            
            //Hide the content if user not paid
            if !backendUtilities.isUserPaidAccount {
                unpaidAccountOverlay()
            }
        }
        .onAppear {
            startupOperation()
        }
        .onChange(of: shouldRefresh) { _ in
            if shouldRefresh {
                shouldRefresh = false
                initializeAllMealTypes()
            }
        }
        .onChange(of: selectedDate) { _ in
            handleSelectedDateChange()
        }
    }
    
    /*
     
        Creates the overlay that blocks content for unpaid users.
     
     */
    func unpaidAccountOverlay() -> some View {
        Group {
            Color.black
                .opacity(0.8)
            Color.white
                .opacity(0.8)
            HStack {
                Spacer()
                Text("Upgrade your account to gain access to the weekly hub.")
                    .foregroundColor(.white)
                    .modifier(CustomTextLabelStyle(0.08*screenWidth, false))
                    .shadow(color: .black, radius: 1)
                    .padding()
                    .background {
                        RoundedRectangle(cornerRadius: 10)
                            .foregroundColor(.orange.opacity(0.9))
                            .shadow(color: .black, radius: 1)
                    }
                Spacer()
            }
            .padding(.bottom, 0.50*screenHeight)
        }
        .cornerRadius(10)
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
     
        Checks if meal plan needs resetting, and adds component styling.
     
     */
    func startupOperation() {
        screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
        UISegmentedControl.appearance().selectedSegmentTintColor = .orange
        
        //Reset grocery list when new week has started
        if shouldResetMealPlan() {
            backendUtilities.resetMealPlanData()
        }
        
        initializeAllMealTypes()
    }
    
    /*
     
        Determines if the current date is present as a key in the meal plan. If not, this indicates that the week is over and we need to reset the meal plan for the new week, starting from the date of user preferred start day.
     
     */
    func shouldResetMealPlan() -> Bool {
        let formattedCurrentDate = dateFormatter.string(from: Date())
        let userMealPlan = backendUtilities.getUserMealPlan()
        let keyExists = userMealPlan.contains { $0.key == formattedCurrentDate }

        return keyExists ? false : true
    }
    
    /*
     
        Creates the 5 meal type sections which displays the recipe tiles in the users meal plan.
     
     */
    func mealYTypeSections() -> some View {
        ForEach(mealTypes.indices, id: \.self) { index in
            createMealHeader(mealTypeTitles[index])

            ForEach(getRecipeList(mealTypes[index]), id: \.self) { recipe in
                RecipeButtonView(recipe, $shouldRefresh, $selectedDate)
            }
            
            MealPlanAddMealButton(mealTypes[index], $selectedDate)
        }
    }

    /*
     
        Creates a meal header by suppliying the name of the meal.
     
     */
    func createMealHeader(_ mealName: String) -> some View {
        VStack(spacing: 0) {
            HStack() {
                Text(mealName)
                    .foregroundColor(colorScheme == .dark ? Color(.white) : Color(.black))
                    .font(.system(size: 0.05*screenWidth, weight: .heavy))
                    .padding(.vertical, 0.006*screenHeight)
                    Spacer()
            }
            Rectangle()
                .foregroundColor(.orange)
                .frame(height: 0.002*screenHeight)
        }
    }
    
    /*
     
        Loops through meal types and calls the initialize function for each.
     
     */
    func initializeAllMealTypes() {
        for mealType in mealTypes {
            initializeMealTypeArray(mealType)
        }
    }
    
    /*
     
        Initializes the meal type array of recipes which corresponds to the mealType value. Fetches the recipe ids in the meal plan from Recipes table to build the data for each recipe object.
     
     */
    func initializeMealTypeArray(_ mealType: String) {
        let formattedDate = dateFormatter.string(from: selectedDate)

        let mealPlanDay = backendUtilities.getUserMealPlanDay(formattedDate)
        let mealTypeArray = mealPlanDay[mealType] ?? []

        //Set as empty array if no recipes in meal type
        if mealTypeArray.isEmpty {
            setMealTypeRecipeArray(mealType, [])
            return
        }

        let dispatchGroup = DispatchGroup()
        var tempRecipeArray: [RecipeHash] = []

        for recipeId in mealTypeArray {
            dispatchGroup.enter()

            let query = PFQuery(className: "Recipes")
            query.getObjectInBackground(withId: recipeId) { (recipe: PFObject?, error: Error?) in
                defer { dispatchGroup.leave() }

                if let error = error {
                    print("Error fetching recipe with id: \(recipeId) \(error.localizedDescription)\n")
                    
                } else if let recipe = recipe {
                    backendUtilities.getRecipeImage(recipe) { image in
                        if let image = image {
                            let newRecipe = createNewRecipeHash(recipe, image, mealType)
                            tempRecipeArray.append(newRecipe)

                            //If all recipes have been looped through, sort and initialize a mealTypeRecipeArray
                            if tempRecipeArray.count == mealTypeArray.count {
                                tempRecipeArray.sort { $0.name.localizedCaseInsensitiveCompare($1.name) == .orderedAscending }
                                setMealTypeRecipeArray(mealType, tempRecipeArray)
                            }
                        } else {
                            print("Unsuccessfully fetched image data.\n")
                        }
                    }
                }
            }
        }
        dispatchGroup.notify(queue: .main) {}
    }
    
    /*
     
        Creates and returns a new recipe hash object with the data passed in.
     
     */
    func createNewRecipeHash(_ recipe: PFObject, _ image: UIImage, _ mealType: String) -> RecipeHash {
        let name = recipe["name"] as? String ?? ""
        let recipeId = recipe.objectId as? String ?? ""
        let ingredients = recipe["ingredients"] as? [[Any]] ?? [[]]

        return RecipeHash(name, image, recipeId, mealType, ingredients)
    }

    /*
     
        Sets the recipe tile array that corresponds with mealType.
     
     */
    func setMealTypeRecipeArray(_ mealType: String, _ newRecipeArray: [RecipeHash]) {
        switch mealType {
            case "breakfast":
                breakfastRecipes = newRecipeArray
            case "lunch":
                lunchRecipes = newRecipeArray
            case "dinner":
                dinnerRecipes = newRecipeArray
            case "snacks":
                snacksRecipes = newRecipeArray
            case "dessert":
                dessertRecipes = newRecipeArray
            default:
                break
        }
    }
    
    /*
     
        Returns the recipe tile array that corresponds to the meal type.
     
     */
    func getRecipeList(_ mealType: String) -> [RecipeHash] {
        switch mealType {
        case "breakfast":
           return breakfastRecipes
        case "lunch":
            return lunchRecipes
        case "dinner":
            return dinnerRecipes
        case "snacks":
            return snacksRecipes
        case "dessert":
            return dessertRecipes
        default:
            return []
        }
    }
}

