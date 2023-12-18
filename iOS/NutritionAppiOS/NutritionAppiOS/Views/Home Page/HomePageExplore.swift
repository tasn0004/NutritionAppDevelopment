import SwiftUI
import Parse

struct HomePageExplore: View {
    
    @Environment(\.colorScheme) private var colorScheme

    @EnvironmentObject private var backendUtilities: BackendUtilities
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    @EnvironmentObject private var homePageData: HomePageObservable
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0
    
    @State private var isLoading = false
    
    @FocusState private var isSearchBarFocused: Bool

    /*
     
        View body
     
     */
    var body: some View {
        ZStack {
            Color("background").ignoresSafeArea()

            VStack(spacing: 0) {
                searchBar()
                
                //Show view depending on state of search term
                if homePageData.searchTerm.isEmpty {
                    CategoryListView()
                } else {
                    RecipeSearchView()
                }
            }
        }
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
        }
    }
    
    /*
     
        Creates the search bar view.
     
     */
    func searchBar() -> some View {
        ZStack {
            TextField("", text: $homePageData.searchTerm)
                .focused($isSearchBarFocused)
                .padding()
                .background(.white)
                .frame(width: 0.9*screenWidth, height: 0.06*screenHeight)
                .cornerRadius(10)
                .onSubmit {
                    homePageData.searchResults = []
                    searchForRecipesByName()
                    searchForRecipesByIngredientName()
                    if !homePageData.searchResults.isEmpty {
                        print("Recipes returned from searching for \(homePageData.searchTerm): \(homePageData.searchResults)\n")
                    }
                }
                .background {
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(.black, lineWidth: 0.005*screenWidth)
                        .shadow(radius: 6)
                }
                .disableAutocorrection(true)
                .autocapitalization(.none)
            
            HStack {
                Image(systemName: "magnifyingglass")
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: 0.05*screenWidth)
                    
                Text("Search for recipes")
                Spacer()
            }
            .opacity(!homePageData.searchTerm.isEmpty ? 0.0 : 0.2)
            .padding()
            .onTapGesture {
                isSearchBarFocused = true
            }
        }
        .foregroundColor(.black)
        .modifier(CustomTextLabelStyle(0.05*screenWidth, false))
        .padding()
        .shadow(radius: 6)
    }
    
    /*
     
        Creates and returns a new recipe hash object with the data passed in.
     
     */
    func createNewRecipeHash(_ recipe: PFObject, _ image: UIImage) -> RecipeHash {
        let name = recipe["name"] as? String ?? ""
        let recipeId = recipe.objectId as? String ?? ""

        return RecipeHash(name, image, recipeId, "", [])
    }
    
    /*
     
        Searches the Recipes table for any recipe with the name containing search term. Results are created as objects and added to
     
     */
    func searchForRecipesByName() {
        var tempRecipesArray: [RecipeHash] = []
        let dispatchGroup = DispatchGroup()
        isLoading = true
        
        let query = PFQuery(className:"Recipes")

        query.whereKey("name", matchesRegex: homePageData.searchTerm, modifiers: "i")
        query.findObjectsInBackground { (recipeObjects, error) in
            if let error = error {
                print("Error fetching recipes on searching by name: \(error.localizedDescription)\n")
                
            } else if let recipeObjects = recipeObjects {
                //No results from search, return
                if recipeObjects.isEmpty {
                    print("No recipes found when searching by recipe name\n")
                    return
                }
                //Loop through results and add to searchResults array.
                for recipe in recipeObjects {
                    dispatchGroup.enter()
                    
                    backendUtilities.getRecipeImage(recipe) { image in
                        defer { dispatchGroup.leave() }
                        
                        if let image = image {
                            let newRecipe = createNewRecipeHash(recipe, image)
                            tempRecipesArray.append(newRecipe)
                            
                            //If all recipes have been looped through, initialize searchResults by appending the temp array of recipes.
                            if tempRecipesArray.count == recipeObjects.count {
                                homePageData.searchResults = tempRecipesArray
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

    /*
     
        Searches the Recipes table for any recipe with an ingredient with a name containing search term.
     
     */
    func searchForRecipesByIngredientName() {
        let dispatchGroup = DispatchGroup()
        var resultFound = false
        isLoading = true
        
        //Get all recipes in recipes table
        let query = PFQuery(className:"Recipes")
        query.findObjectsInBackground { (recipeObjects: [PFObject]?, error: Error?) in
            if let error = error {
                print("Error fetching recipes on searching by ingredient names: \(error.localizedDescription)\n")
                
            } else if let recipeObjects = recipeObjects {
                //Loop and check ingredients for each recipe for the searchTerm in an ingredient name.
                for recipe in recipeObjects {
                    //If current recipe doe snot contain an ingredient name containing search term, continue loop
                    if !doesIngredientsContainSearchTerm(recipe["ingredients"] as? [[Any]] ?? []) {
                        continue
                    }
                    resultFound = true
                    
                    //Recipe contains an ingredient with a name containing the search term. Create the object and add to results.
                    backendUtilities.getRecipeImage(recipe) { image in
                        if let image = image {
                            let newRecipe = createNewRecipeHash(recipe, image)
                            homePageData.searchResults.append(newRecipe)
                            
                        } else {
                            print("Unsuccessfully fetched image data.\n")
                        }
                    }
                }
            }
            if !resultFound {
                print("No recipes found when searching by ingredient names.\n")
            }
        }
    }
    
    /*
     
        Checks if an ingredient in an ingredients array contains the search term.
     
     */
    func doesIngredientsContainSearchTerm(_ ingredients: [[Any]]) -> Bool {
        for ingredient in ingredients {
            if (ingredient[0] as? String ?? "").lowercased()
                .contains(homePageData.searchTerm.lowercased()) {
                return true
            }
        }
        return false
    }
}
