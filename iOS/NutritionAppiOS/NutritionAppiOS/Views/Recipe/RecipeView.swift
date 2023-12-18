import SwiftUI
import Parse

struct RecipeView: View {
    
    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.presentationMode) var presentationMode
    
    //Environment objects
    @EnvironmentObject private var backendUtilities: BackendUtilities
    @EnvironmentObject private var homePageData: HomePageObservable
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0
    
    //Recipe fields
    var recipeId: String
    @State var recipeName = ""
    @State var description = ""
    @State var background = ""
    @State var videoUrl = ""
    @State var recipeImage: UIImage?
    
    @State var timesLiked = 0
    @State var numberOfComments = 0
    
    @State var ingredients = [[Any]]()
    @State var nutritionInformation: [String: [Any]] = [:]
    
    //Flags
    @State private var isFavourited = false
    @State private var isLiked = false
    @State private var isVideoPlayerVisible = false
    @State private var isLoading = true
    
    let nutrientKeyNamesOrdered = [
        "calories",
        "fat",
        "saturatedFat",
        "transFat",
        "cholesterol",
        "sodium",
        "potassium",
        "carbohydrates",
        "fibre",
        "sugar",
        "protein",
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
        "Calories",
        "Fat",
        "Saturated Fat",
        "Trans Fat",
        "Cholesterol",
        "Sodium",
        "Potassium",
        "Carbohydrates",
        "Fibre",
        "Sugar",
        "Protein",
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
    
    init(_ recipeId: String) {
        self.recipeId = recipeId
    }
    
    /*
     
        View body
     
     */
    var body: some View {
        ZStack() {
            Color("background").ignoresSafeArea()
            
            if isLoading {
                LoadingSpinner($isLoading)
                
            } else {
                ScrollView {
                    VStack(alignment: .leading) {
                        //Image/video header
                        ZStack(alignment: .top) {
                            imageVideoPlayer()
                            headerButtons()
                        }
                        
                        //In between header and background
                        HStack {
                            //Recipe name
                            Text(recipeName)
                                .modifier(CustomTextLabelStyle(0.06*screenWidth, true))
                                .foregroundColor(colorScheme == .dark ? .white : .black)
                                .minimumScaleFactor(0.5)
                                .lineLimit(3)
                            Spacer()
                    
                            //Like, favourite, comment buttons
                            recipeIconsBar()
                                .frame(width: 0.50*screenWidth)
                        }
                        .padding(.horizontal)

                        Divider()
                            .background(.gray)
                        
                        //Recipe background
                        Text(background)
                            .font(.system(size: 0.05*screenWidth, weight: .medium))
                            .foregroundColor(colorScheme == .dark ? .white : .black)
                            .padding()
                        
                        //Disclosure groups for recipe ingredients, description and nutrition
                        VStack() {
                            ingredientsDisclosure()
                            descriptionDisclosure()
                            nutritionDisclosure()
                        }
                        .padding(.horizontal)
                        .accentColor(.orange)
                        .font(.system(size: 0.05*screenWidth, weight: .medium))
                    }
                }//Scroll view
            }
        }
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
            initializeRecipeData()
        }
    }
    
    /*
     
        Creates the back button and hamburger menu row that sits atop the image/video header.
     
     */
    func headerButtons() -> some View {
        Group {
            if !isVideoPlayerVisible {
                playButton()
                backButton()
                
                HStack {
                    Spacer()
                    HamburgerMenu(recipeId, ingredients, videoUrl, recipeName)
                        .padding(.top, 0.02*screenWidth)
                }
            } else {
                closeVideoButton()
            }
        }
        .padding()
    }
    
    /*
     
        Creates the back button. When selected, sets environment variables to initiate navigation.
     
     */
    func backButton() -> some View {
        HStack {
            VStack {
                Button(action: {
                    homePageData.selectedRecipeId = ""
                    homePageData.isRecipeSelected = false
                }) {
                    Image(systemName: "chevron.left")
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 0.04*screenWidth)
                        .foregroundColor(.blue)
                }
                //Push up
                Spacer()
            }
            //Push left
            Spacer()
        }
    }
    
    /*
     
        Creates the play button with logic to show the evideo play when selected.
     
     */
    func playButton() -> some View {
        HStack {
            VStack {
                //Push down
                Spacer()
                
                Button(action: {
                    isVideoPlayerVisible = true
                }) {
                    Image(systemName: "play")
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 0.04*screenWidth)
                        .foregroundColor(.white)
                        .padding()
                        .background {
                            Circle()
                                .foregroundColor(Color(.darkGray))
                        }
                }
            }
            //Push Left
            Spacer()
        }
    }
    
    /*
     
        Creates the close video X button with logic to close the video player and return to image when selected.
     
     */
    func closeVideoButton() -> some View {
        HStack {
            //Push left
            Spacer()
            VStack {
                Button(action: {
                    isVideoPlayerVisible = false
                }) {
                    Image(systemName: "xmark")
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 0.04*screenWidth)
                        .foregroundColor(.white)
                        .padding(0.02*screenWidth)
                        .background {
                            Circle()
                                .foregroundColor(Color(.darkGray))
                        }
                }
                //Push up
                Spacer()
            }
        }
    }
    
    /*
     
        Creates the image/video player for the selected recipe.
     
     */
    func imageVideoPlayer() -> some View {
        ZStack {
            if let image = recipeImage {
                Image(uiImage: image)
                    .resizable()
                    .aspectRatio(contentMode: .fill)
                    .frame(width: screenWidth, height: 0.5*screenHeight)
                    .ignoresSafeArea(.all)
                    .cornerRadius(10)
            }

            if isVideoPlayerVisible {
                RecipeVideo(videoId: videoUrl)
                    .frame(width: screenWidth, height: 0.5*screenHeight)
                    .edgesIgnoringSafeArea(.all)
                    .aspectRatio(contentMode: .fill)
                    .ignoresSafeArea(.all)
                    .cornerRadius(10)
                
                //overlay to block touch gestures
                VStack {
                    Spacer()
                    HStack {
                        Spacer()
                        Rectangle()
                            .foregroundColor(.gray.opacity(0.001))
                            .frame(width: 0.75*screenWidth, height: 0.05*screenHeight)
                    }
                }
            }
        }
    }
    
    /*
     
        Creates the row of like, favourite, and comment buttons with their counts
     
     */
    func recipeIconsBar() -> some View {
        HStack {
            Spacer()
            
            //Like button and count
            HStack(spacing: 0.01*screenWidth) {
                LikeButton(recipeId, $isLiked, $timesLiked)
                Text("\(timesLiked)")
                    .foregroundColor(colorScheme == .dark ? .white : .black)
                    .minimumScaleFactor(0.8)
                    .lineLimit(1)
            }
            
            //Comment button and count
            HStack(spacing: 0.01*screenWidth) {
                CommentButton(recipeId, $numberOfComments)
                Text("\(numberOfComments)")
                    .foregroundColor(colorScheme == .dark ? .white : .black)
                    .minimumScaleFactor(0.8)
                    .lineLimit(1)
            }
            
            //Favourite button and count
            HStack(spacing: 0.01*screenWidth) {
                FavouriteButton(recipeId, $isFavourited)
            }
        }
        .modifier(CustomTextLabelStyle(0.05*screenWidth, false))
    }
    
    /*
     
        Creates a row of information inside the nutrition info and iingredients disclosure groups.
     
     */
    func createDisclosureItem(_ name: String, _ quantity: Double, _ unit: String) -> some View {
        VStack {
            HStack {
                Text(name)
                Spacer()
                Text("\(quantity) " + unit)
            }
            .font(.system(size: 0.04*screenWidth, weight: .medium))
            .foregroundColor(colorScheme == .dark ? .white : .black)
            .blur(radius: backendUtilities.isUserPaidAccount ? 0 : 1.0)
            
            Divider()
                .background(Color.white)
        }
    }
    
    /*

        Creates a disclosure item content background rectangle.

    */
    func disclosureBackgroundRectangle() -> some View {
        RoundedRectangle(cornerRadius: 20)
            .foregroundColor(colorScheme == .dark ? Color(.darkGray) : .gray)
            .shadow(radius: 10)
    }
    
    /*
     
        Creates the ingredients disclosure group on the recipe view.
     
     */
    func ingredientsDisclosure() -> some View {
        DisclosureGroup("Ingredients") {
            VStack {
                //If ingredients 4 or less, print normally
                if ingredients.count <= 4 {
                    ForEach(0..<ingredients.count, id: \.self) { index in
                        createIngredientDisclosureItem(index)
                    }
                } else {
                    ForEach(0..<4, id: \.self) { index in
                        createIngredientDisclosureItem(index)
                    }
                    
                    VStack {
                        ForEach(4..<ingredients.count, id: \.self) { index in
                            createIngredientDisclosureItem(index)
                        }
                    }
                    .padding(!backendUtilities.isUserPaidAccount ? 0.04*screenWidth : 0)
                    .overlay {
                        //Show overlay over remaining ingredients if the user is an unpaid account.
                        if !backendUtilities.isUserPaidAccount {
                            ZStack {
                                Color.black
                                    .opacity(0.9)
                                Color.white
                                    .opacity(0.8)
                                Color.black
                                    .opacity(0.8)
                                Text("Upgrade your account to see more ingredients.")
                                    .foregroundColor(.white)
                                    .modifier(CustomTextLabelStyle(0.04*screenWidth, false))
                                    .shadow(color: .black, radius: 1)
                                    .padding(.horizontal)
                            }
                            .cornerRadius(10)
                        }
                    }
                }
            }
            .padding()
            .background(
                disclosureBackgroundRectangle()
            )
        }
    }
    
    /*
     
        Creates an item in the ingredients disclosure group.
     
     */
    func createIngredientDisclosureItem(_ index: Int) -> some View {
        Group {
            let ingredientName = ingredients[index][0] as? String ?? ""
            let ingredientQty = ingredients[index][1] as? Double ?? 0
            let ingredientQtyUnit = ingredients[index][2] as? String ?? ""

            createDisclosureItem(ingredientName, ingredientQty, ingredientQtyUnit)
        }
    }
    
    /*
     
        Creates the description disclosure group on the recipe view.
     
     */
    func descriptionDisclosure() -> some View {
        DisclosureGroup("Description") {
            ZStack(alignment: .top) {
                VStack {
                    HStack {
                        Text(description)
                            .font(.system(size: 0.04 * screenWidth, weight: .medium))
                            .foregroundColor(colorScheme == .dark ? .white : .black)
                            .fixedSize(horizontal: false, vertical: true)
                            .blur(radius: backendUtilities.isUserPaidAccount ? 0 : 2)
                        Spacer()
                    }
                }
                .padding()
                .background(
                    disclosureBackgroundRectangle()
                )
                .overlay {
                    //Show overlay over remaining ingredients if the user is an unpaid account.
                    if !backendUtilities.isUserPaidAccount {
                        ZStack {
                            Color.black
                                .opacity(0.9)
                            Color.white
                                .opacity(0.8)
                            Color.black
                                .opacity(0.7)
                            Text("Upgrade your account to see decription for this recipe.")
                                .foregroundColor(.white)
                                .modifier(CustomTextLabelStyle(0.04*screenWidth, false))
                                .shadow(color: .black, radius: 1)
                                .padding(.horizontal)
                        }
                        .cornerRadius(10)
                        .padding(0.02*screenWidth)
                    }
                }
            }
        }
    }
    
    /*
     
        Creates the nutrition disclosure group on the recipe view.
     
     */
    func nutritionDisclosure() -> some View {
        DisclosureGroup("Nutrition") {
            VStack {
                ForEach(nutrientKeyNamesOrdered.indices, id: \.self) { index in
                    let nutrientKey = nutrientKeyNamesOrdered[index]
                    
                    if let quantity = nutritionInformation[nutrientKey]?[0] as? Double, let unit = nutritionInformation[nutrientKey]?[1] as? String {
                        createDisclosureItem(nutrientTitlesOrdered[index], quantity, unit)
                    }
                }
            }
            .padding()
            .background(
                disclosureBackgroundRectangle()
            )
            .overlay {
                //Show overlay over remaining ingredients if the user is an unpaid account.
                if !backendUtilities.isUserPaidAccount {
                    ZStack {
                        Color.black
                            .opacity(0.5)
                        Color.white
                            .opacity(0.8)
                        Color.black
                            .opacity(0.5)
                        
                        Text("Upgrade your account to see nutrition information for this recipe.")
                            .foregroundColor(.white)
                            .modifier(CustomTextLabelStyle(0.04*screenWidth, false))
                            .shadow(color: .black, radius: 1)
                            .padding(.horizontal)
                    }
                    .cornerRadius(10)
                    .padding(0.02*screenWidth)
                }
            }
        }
    }
    
    /*
     
        Called on view appear. Initializes all the fields from the database for the recipe being displayed on the view.
     
     */
    func initializeRecipeData() {
        let query = PFQuery(className: "Recipes")
        query.whereKey("objectId", equalTo: recipeId)

        //Execute the query
        query.getFirstObjectInBackground { (object, error) in
            if let error = error {
                print("Error fetching recipe: \(error.localizedDescription)\n")
                
            } else if let recipe = object {
                recipeName = recipe["name"] as? String ?? ""
                description = recipe["description"] as? String ?? ""
                background = recipe["background"] as? String ?? ""

                videoUrl = recipe["videoUrl"] as? String ?? ""
                
                timesLiked = recipe["timesLiked"] as? Int ?? 0
                numberOfComments = recipe["numberOfComments"] as? Int ?? 0
                
                isFavourited = checkIfRecipeFavourited()
                isLiked = checkIfRecipeLiked()
                
                initializeImage(recipe)
                initializeIngredients(recipe)
                initializeNutritionInfo(recipe)
                
                isLoading = false
            }
        }
    }
    
    /*
     
        Initializes the recipe's image.
     
     */
    func initializeImage(_ recipe: PFObject) {
        backendUtilities.getRecipeImage(recipe) { image in
            if let image = image {
                recipeImage = image
            } else {
                print("Unsuccessfully fetched image data.\n")
            }
        }
    }
    
    /*
     
        Initializes the recipe's ingredients array.
     
     */
    func initializeIngredients(_ recipe: PFObject) {
        if let ingredientsData = recipe["ingredients"] as? [[Any]] {
            for item in ingredientsData {

                let name = item[0] as? String ?? ""
                let quantity = item[1] as? Double ?? 0.0
                let unit = item[2] as? String ?? ""

                //Append ingredient data to local ingredients array
                ingredients.append([name, quantity, unit])
            }
        }
    }
    
    /*
     
        Initializes the recipe's nutritional information.
     
     */
    func initializeNutritionInfo(_ recipe: PFObject) {
        if let recipeNutritionInfo = recipe["nutritionInformation"] as? [String: [Any]] {
            for (nutrient, nutrientValues) in recipeNutritionInfo {

                let quantity = nutrientValues[0] as? Double ?? 0.0
                let unit = nutrientValues[1] as? String ?? ""

                nutritionInformation[nutrient] = [quantity, unit]
            }
        }
    }
    
    /*
     
        Checks if this recipe's id is in the user's favourited recipes list.
     
     */
    func checkIfRecipeFavourited() -> Bool {
        return backendUtilities.getUserFavouritedRecipes().contains(recipeId)
    }
    
    /*
     
        Checks if this recipe's id is in the user's liked recipes list.
     
     */
    func checkIfRecipeLiked() -> Bool {
        return backendUtilities.getUserLikedRecipes().contains(recipeId)
    }
}
