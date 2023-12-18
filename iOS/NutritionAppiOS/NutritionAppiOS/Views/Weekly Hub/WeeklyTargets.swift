import SwiftUI
import Parse

struct WeeklyTargets: View {
    
    @Environment(\.colorScheme) private var colorScheme
    
    @EnvironmentObject private var backendUtilities: BackendUtilities
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0
    
    let proteinString = "Protein"
    let carbString = "Carbs"
    let fatString = "Fats"
    
    let proteinColour = Color(.red)
    let carbColour = Color(.orange)
    let fatColour = Color(.blue)
    
    @State private var selectedDate = Date()
    @State private var selectedDay = "--"
    
    @State var nutrientsAchieved: [String: Double] = [:]
    @State var nutrientTargets: [String: Double] = [:]
    
    @State var initialLoad = true
    
    let nutrientKeyNamesOrdered = [
        "saturatedFat",
        "transFat",
        "cholesterol",
        "sodium",
        "potassium",
        "fibre",
        "sugar",
        "vitaminA",
        "vitaminC",
        "vitaminD",
        "vitaminB12",
        "calcium",
        "iron",
        "magnesium",
        "zinc",
        "folate"
    ]
    
    let nutrientTitlesOrdered = [
        "Saturated Fat",
        "Trans Fat",
        "Cholesterol",
        "Sodium",
        "Potassium",
        "Fibre",
        "Sugar",
        "Vitamin A",
        "Vitamin C",
        "Vitamin D",
        "Vitamin B12",
        "Calcium",
        "Iron",
        "Magnesium",
        "Zinc",
        "Folate"
    ]

    let foodGroupsKeyNamesOrdered = [
        "nutsLegumesServings",
        "meatServings",
        "dairyServings",
        
        "grainServings",
        "fruitServings",
        "vegetableServings",
        "fatOilServings"
    ]
    
    let foodGroupsTitlesOrdered = [
        "Nuts & Legumes",
        "Meat, Fish, Poultry",
        "No-Low Fat Dairy",
        
        "Grains",
        "Fruits",
        "Vegetables",
        "Fats & Oils"
    ]
    
    let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMMM d, yyyy"
        return formatter
    }()
    
    /*
     
        View body
     
     */
    var body: some View {
        ZStack {
            Color("background").ignoresSafeArea()

            VStack() {
                //Date selector
                SevenDayDateSelector($selectedDate, $selectedDay)
                Spacer()
                
                //Scrolling content area
                ScrollView {
                    LazyVStack(spacing: 0.015*screenHeight) {
                        caloriesTracker()
                        Spacer()
                        macroTrackers()
                        Spacer()
                        foodServingsProgressArea()
                        Spacer()
                        microProgressArea()
                        Spacer()
                    }
                }
                .padding(.top)
                .frame(width: 0.95*screenWidth)
                .clipped()
            }
        }
        .onChange(of: selectedDate) { _ in
            initializeAchieved()
            initializeTargets()
        }
        .onAppear {
            startupOperation()
        }
    }
    
    /*
     
        Styles the veiw onAppear
     
     */
    func startupOperation() {
        UISegmentedControl.appearance().selectedSegmentTintColor = .orange
        screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
        
        initializeAchieved()
        initializeTargets()
    }
    
    /*
     
        Called every time the date selected is changed. Has logic to handle initial load tracking, otherwise redundant reinitialization on initial page load would occur.
     
     */
    func handleSelectedDateChange() {
        switch initialLoad {
            case true:
                initialLoad = false
            case false:
                initializeAchieved()
        }
    }
    
    /*
     
        Creates a divider with custom styling.
     
     */
    func divider() -> some View {
        Divider()
            .foregroundColor(.black)
    }

    /*
     
        Creates the content area for macro-nutrient progress.
     
     */
    func caloriesTracker() -> some View {
        Group {
            let achievedValue = nutrientsAchieved["calories"] as? Double ?? 0.0
            let targetValue = nutrientTargets["calories"] as? Double ?? 0.0
            let clampedAchievedValue = max(0.0, min(achievedValue, targetValue))
            
            ProgressView("Calories: \(achievedValue, specifier: "%.0f") / \(targetValue, specifier: "%.0f")g",
                         value: clampedAchievedValue,
                         total: targetValue)
        }
        .tint(.red)
        .padding()
        .background {
            RoundedRectangle(cornerRadius: 10)
                .stroke(.black, lineWidth: 0.005*screenWidth)
                .shadow(radius: 1)
        }
        .background(.white)
        .foregroundColor(.black)
        .cornerRadius(10)
    }
    
    /*
     
        Creates the macro tracker circles.
     
     */
    func macroTrackers() -> some View {
        VStack {
            //Tracker circles
            HStack {
                createMacroTracker(proteinString,
                                   nutrientsAchieved["protein"] ?? 0.0,
                                   nutrientTargets["protein"] ?? 0.0)
                Spacer()
                createMacroTracker(carbString,
                                   nutrientsAchieved["carbohydrates"] ?? 0.0,
                                   nutrientTargets["carbohydrates"] ?? 0.0)
                Spacer()
                createMacroTracker(fatString,
                                   nutrientsAchieved["fat"] ?? 0.0,
                                   nutrientTargets["fat"] ?? 0.0)
            }
        }
        .padding(.horizontal)
        .background {
            RoundedRectangle(cornerRadius: 10)
                .stroke(.black, lineWidth: 0.005*screenWidth)
                .shadow(radius: 1)
        }
        .background(.white)
        .foregroundColor(.black)
        .cornerRadius(10)
    }
    
    /*
     
        Creates a progress tracker circle by supplying the macro name, amount achieved, target amount.
     
     */
    func createMacroTracker(_ macroName: String, _ macroAchieved: Double, _ macroTarget: Double) -> some View {
        var circleColour: Color
        
        switch macroName {
        case proteinString:
            circleColour = proteinColour
            break
        case carbString:
            circleColour = carbColour
            break
        case fatString:
            circleColour = fatColour
            break
        default:
            circleColour = .black
        }
        
        return
            VStack {
                Spacer()
                Text("\(macroName)")
                    .foregroundColor(.black)
                    .modifier(CustomTextLabelStyle(0.05*screenWidth))
                     
                ZStack(){
                    Text("\(macroAchieved/macroTarget*100, specifier: "%.1f")%")
                        .foregroundColor(.black)
                        
                    //Tracker circle
                    ZStack() {
                        //full circle
                        Circle()
                            .stroke(lineWidth: 0.03*screenWidth)
                            .frame(width: 0.20*screenWidth)
                            .foregroundColor(circleColour)
                            .opacity(0.5)
                        
                        //progress circle
                        Circle()
                            .trim(from: 0, to: macroAchieved/macroTarget)
                            .stroke(lineWidth: 0.03*screenWidth)
                            .frame(width: 0.20*screenWidth)
                            .foregroundColor(circleColour)
                            .opacity(0.8)
                            .rotationEffect(Angle(degrees: -90))
                            
                        //Outer black border
                        Circle()
                            .stroke(lineWidth: 0.001*screenWidth)
                            .frame(width: 0.23*screenWidth)
                            .foregroundColor(.black)
                            .opacity(0.6)
                        
                        //inner black border
                        Circle()
                            .stroke(lineWidth: 0.001*screenWidth)
                            .frame(width: 0.1689*screenWidth)
                            .foregroundColor(.black)
                            .opacity(0.6)
                        
                        VStack {
                            //12 0'clock marker
                            Rectangle()
                                .foregroundColor(.black)
                                .frame(width: 0.002*screenWidth, height: 0.016*screenHeight)
                                .opacity(0.7)
                            Spacer()
                        }
                        
                    }
                }
                //Number representation
                Text("\(macroAchieved, specifier: "%.0f") / \(macroTarget, specifier: "%.0f")g")
                    .foregroundColor(.black)
                Spacer()
            }
            .modifier(CustomTextLabelStyle(0.04*screenWidth))
    }
    
    /*
     
        Creates a food servings progress tracker.
     
     */
    func foodServingTracker(_ index: Int) -> some View {
        VStack {
            Text("\(nutrientsAchieved[foodGroupsKeyNamesOrdered[index]] ?? 0.0, specifier: "%.0f") / \(nutrientTargets[foodGroupsKeyNamesOrdered[index]] ?? 0.0, specifier: "%.0f")")
                .modifier(CustomTextLabelStyle(0.055*screenWidth))
                .lineLimit(1)
                .minimumScaleFactor(0.5)
            
            Text(foodGroupsTitlesOrdered[index])
                .modifier(CustomTextLabelStyle(0.03*screenWidth))
                .lineLimit(1)
                .minimumScaleFactor(0.5)
        }
    }
    
    /*
     
        Creates the content area for food servings progress.
     
     */
    func foodServingsProgressArea() -> some View {
        VStack() {
            //Top row
            HStack {
                ForEach(0..<3, id: \.self) { index in
                    Spacer()
                    VStack {
                        foodServingTracker(index)
                    }
                    Spacer()
                }
            }
            Spacer()
            
            //Bottom row
            HStack {
                ForEach(3..<7, id: \.self) { index in
                    Spacer()
                    VStack {
                        foodServingTracker(index)
                    }
                    Spacer()
                }
            }
        }
        .foregroundColor(.black)
        .padding()
        .background {
            RoundedRectangle(cornerRadius: 10)
                .stroke(.black, lineWidth: 0.005*screenWidth)
                .shadow(radius: 1)
        }
        .background(.white)
        .foregroundColor(.black)
        .cornerRadius(10)
    }
    
    /*
     
        Creates the content area for micronutrient progress. Has a progress view title and bar for every nutrient in sorted order.
     
     */
    func microProgressArea() -> some View {
        VStack {
            ForEach(nutrientKeyNamesOrdered.indices, id: \.self) { index in
                let achievedValue = nutrientsAchieved[nutrientKeyNamesOrdered[index]] as? Double ?? 0.0
                let targetValue = nutrientTargets[nutrientKeyNamesOrdered[index]] as? Double ?? 0.0
                let clampedAchievedValue = max(0.0, min(achievedValue, targetValue))
                
                ProgressView("\(nutrientTitlesOrdered[index]): \(achievedValue, specifier: "%.0f") / \(targetValue, specifier: "%.0f")g",
                             value: clampedAchievedValue,
                             total: targetValue)
                divider()
            }
            .tint(.red)
        }
        .padding()
        .background {
            RoundedRectangle(cornerRadius: 10)
                .stroke(.black, lineWidth: 0.005*screenWidth)
                .shadow(radius: 1)
        }
        .background(.white)
        .foregroundColor(.black)
        .cornerRadius(10)
    }
    
    /*
     
        Initialize the targets dictionary from the users nutrition profile by looping through each nutrient and adding names as keys and nutrient amounts as values.
     
     */
    func initializeTargets() {
        let userNutritionProfile = backendUtilities.getUserNutritionProfile()
        nutrientTargets = [:]
        
        for (nutrientName, valuesArray) in userNutritionProfile {
            nutrientTargets[nutrientName] = valuesArray[0] as? Double ?? 0.0
        }
    }
    
    /*
     
        Initialize the achieved dictionary from the users meal plan by looping through each recipe, getting its nutrition information, and adding its nutrient totals to the running total for that nutrient stored in nutrientsAchieved.
     
     */
    func initializeAchieved() {
        let userMealPlan = backendUtilities.getUserMealPlan()
        let mealPlanDay = userMealPlan[dateFormatter.string(from: selectedDate)] as? [String: [String]] ?? [:]
        
        let dispatchGroup = DispatchGroup() // Create a dispatch group
        
        // Initialize an empty dictionary to collect recipe nutrition info
        var recipeNutritionInfo: [String: [Any]] = [:]
        nutrientsAchieved = [:]
        
        // Loop through each meal type array
        for (_, mealTypeRecipes) in mealPlanDay {
            // Skip iteration if no recipes in mealType dict
            if mealTypeRecipes.isEmpty { continue }
            
            // Loop through each recipe id in a meal type array
            for recipeId in mealTypeRecipes {
                //Enter dispatch group
                dispatchGroup.enter()
                
                let query = PFQuery(className: "Recipes")
                query.getObjectInBackground(withId: recipeId) { (recipe: PFObject?, error: Error?) in
                    defer {
                        dispatchGroup.leave()
                    }
                    
                    if let error = error {
                        print("Error fetching recipe with id: \(recipeId) \(error.localizedDescription)\n")
                    } else if let recipe = recipe {
                        // Get the current recipe's nutrition information
                        recipeNutritionInfo = recipe["nutritionInformation"] as? [String: [Any]] ?? [:]
                        
                        // Loop through the recipe's nutrition info and update the achieved values
                        for (nutrientName, valuesArray) in recipeNutritionInfo {
                            
                            let recipeAmount = valuesArray[0] as? Double ?? 0.0
                            
                            // Update the achieved values
                            if let existingAmount = self.nutrientsAchieved[nutrientName] {
                                nutrientsAchieved[nutrientName] = existingAmount + recipeAmount
                            } else {
                                nutrientsAchieved[nutrientName] = recipeAmount
                            }
                        }
                    }
                }
            }
        }
        //Wait for all queries to complete
        dispatchGroup.notify(queue: .main) { }
    }
}
