import SwiftUI
import Parse

struct HomePageFavourites: View {
    
    @Environment(\.colorScheme) private var colorScheme
    
    @EnvironmentObject private var backendUtilities: BackendUtilities
    @EnvironmentObject private var homePageData: HomePageObservable
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0

    @State private var shouldRefresh = false
    @State private var isLoading = true
    
    @State private var favouritedListObjects: [RecipeHash] = []
  
    /*
     
        View body
     
     */
    var body: some View {
        ZStack {
            Color("background").ignoresSafeArea()
            LoadingSpinner($isLoading)
            
            //Favourited recipes list
            ScrollView {
                LazyVStack(spacing: 0.05*screenWidth) {
                    ForEach(favouritedListObjects, id: \.self) { recipe in
                        createRecipeButton(recipe)
                    }
                }
                .padding(.top)
            }
            .clipped()
            
            Spacer()
        }
        .onChange(of: shouldRefresh) { newValue in
            //reinitialize favourited recipes when a recipe has been unfavourited
            initializeFavouritedRecipes()
            shouldRefresh = false
        }
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
            initializeFavouritedRecipes()
        }
    }

    /*
     
        Creates the recipe buttons in the list of favourited recipes.
     
     */
    func createRecipeButton(_ recipe: RecipeHash) -> some View {
        Button(action: {
            //Set navigation environment variables to show the recipe page for the selected recipe.
            homePageData.selectedRecipeId = recipe.recipeId
            homePageData.isRecipeSelected = true
            homePageData.tabSelectedExplore = "Favourites"
        }) {
            ZStack {
                //Image
                ZStack {
                    if let image = recipe.imageName {
                        Image(uiImage: image)
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                            .clipped()
                            .shadow(radius: 10)
                    }

                    //Dark overlay
                    Rectangle()
                        .foregroundColor(Color(.darkGray))
                        .opacity(0.35)
                }
                .frame(width: 0.90*screenWidth, height: 0.17*screenHeight)
                .cornerRadius(15)
                .mask(
                   LinearGradient(
                       gradient: Gradient(colors: [.black, .black, .black, .clear]),
                       startPoint: .top,
                       endPoint: .bottom)
                )
                
                //Bottom row
                HStack() {
                    Spacer()
                    //Recipe name
                    Text(recipe.name)
                        .foregroundColor(.white)
                        .modifier(CustomTextLabelStyle(0.04*screenWidth, true))
                        .padding(.trailing, 0.17*screenWidth)
                        .minimumScaleFactor(0.3)
                        .lineLimit(2)
                    Spacer()

                    Spacer()
                    //Favourited button
                    favouritedButton(recipe)
                        .padding(.leading, 0.17*screenWidth)
                    Spacer()
                }
                .padding(.horizontal)
                .padding(.top, 0.11*screenHeight)
            }
            .shadow(radius: 6)
        }
        .contentShape(Rectangle())
    }

    /*
     
        Creates the favourited button. When selected, item is unfavourited and the list is updated.
     
     */
    func favouritedButton(_ recipe: RecipeHash) -> some View {
        Button(action: {
            onFavouritedSelect(recipe.recipeId)
        }) {
            Image(systemName: "bookmark.fill")
                .foregroundColor(.orange)
        }
        .frame(width: 0.08*screenWidth, height: 0.07*screenHeight)
    }
    
    /*
     
        Updates the current user's favourited recipe array with recipes in the favouritedListObjects array.
     
     */
    func onFavouritedSelect(_ recipeId: String) {
        backendUtilities.updateUserFavouritedRecipes(recipeId, true) { success in
            if success {
                print("Successfully removed \(recipeId) from user's favourited recipes list.\n")
                favouritedListObjects.removeAll { $0.recipeId == recipeId }
                shouldRefresh = true
            } else {
                print("Unsuccessfully removed \(recipeId) from user's favourited recipes list.\n")
            }
        }
    }
    
    /*
     
        Creates and returns a new GroceryItem object based on an ingredient object passed in.
     
     */
    func createNewRecipeHash(_ recipe: PFObject, _ image: UIImage) -> RecipeHash {
        let name = recipe["name"] as? String ?? ""
        let recipeId = recipe.objectId as? String ?? ""

        return RecipeHash(name, image, recipeId, "", [])
    }
    
    /*
     
        Initializes the favouritedListObjects with recipe objects representing the users favourited recipes.
     
     */
    func initializeFavouritedRecipes() {
        let favouritedRecipeIds = backendUtilities.getUserFavouritedRecipes()
        var tempRecipesArray: [RecipeHash] = []
        let dispatchGroup = DispatchGroup()
        isLoading = true

        let query = PFQuery(className: "Recipes")
        query.whereKey("objectId", containedIn: favouritedRecipeIds)
        query.findObjectsInBackground { (recipeObjects, error) in
            if let error = error {
                print("Error fetching favorite recipes: \(error.localizedDescription)\n")
                
            } else if let recipeObjects = recipeObjects {
                for recipe in recipeObjects {
                    dispatchGroup.enter()
                    
                    backendUtilities.getRecipeImage(recipe) { image in
                        defer { dispatchGroup.leave() }
                        
                        if let image = image {
                            let newRecipe = createNewRecipeHash(recipe, image)
                            tempRecipesArray.append(newRecipe)
                            
                            // If all recipes have been looped through, sort and initialize favouritedListObjects
                            if tempRecipesArray.count == recipeObjects.count {
                                tempRecipesArray.sort { $0.name.localizedCaseInsensitiveCompare($1.name) == .orderedAscending }
                                favouritedListObjects = tempRecipesArray
                            }
                        } else {
                            print("Unsuccessfully fetched image data.\n")
                        }
                    }
                }
            }
        }
        dispatchGroup.notify(queue: .main) { isLoading = false }
    }
}


