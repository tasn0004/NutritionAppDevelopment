import SwiftUI
import Parse

struct CategorySelectedView: View {
    
    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.presentationMode) var presentationMode

    @EnvironmentObject private var backendUtilities: BackendUtilities
    @EnvironmentObject private var homePageData: HomePageObservable
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0
    
    @State private var isLoading = true
    
    @State private var recipeListObjects: [RecipeHash] = []
 
    /*
     
        View body
     
     */
    var body: some View {
        ZStack {
            Color("background").ignoresSafeArea()
            LoadingSpinner($isLoading)
            
            VStack {
                imageHeader()

                //Recipes
                ScrollView {
                    LazyVStack(spacing: 0.05*screenWidth) {
                        ForEach(recipeListObjects, id: \.self) { recipe in
                            createRecipeButton(recipe)
                        }
                    }
                }
                .clipped()
                
                Spacer()
            }
        }
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
            initializeRecipes()
        }
    }
    
    /*
     
        Creates the header displaying the category name and image.
     
     */
    func imageHeader() -> some View {
        // Page header
        if let image = homePageData.selectedCategoryObject.imageName {
            return AnyView(
                // Image
                ZStack(alignment: .top) {
                    Image(uiImage: image)
                        .resizable()
                        .frame(height: 0.25*screenHeight)
                        .scaledToFit()
                        .ignoresSafeArea(.all)
                    
                    // Dark overlay
                    Rectangle()
                        .foregroundColor(Color(.darkGray))
                        .frame(height: 0.25*screenHeight)
                        .ignoresSafeArea(.all)
                        .opacity(0.35)
                    
                    // Category name
                    VStack {
                        Spacer()
                        Text(homePageData.selectedCategoryObject.name)
                            .foregroundColor(.white)
                            .modifier(CustomTextLabelStyle(0.09*screenWidth, true))
                            .shadow(radius: 1)
                        Spacer()
                    }
                    
                    backButton()
                        .padding(.top, 0.035*screenHeight)
                }
                .frame(height: 0.18*screenHeight)
                .shadow(radius: 6)
                .padding(.bottom)
            )
        } else {
            return AnyView(EmptyView())
        }
    }
    
    /*
     
        Creates the back button. When selected, modifies environment varibales for home navigation, and pops the current view off the stack to go back.
     
     */
    func backButton() -> some View {
        HStack {
            Button(action: {
                homePageData.selectedCategoryObject = RecipeCategoryHash("", nil)
                homePageData.isCategorySelected = false
                
                presentationMode.wrappedValue.dismiss()
            }) {
                Image(systemName: "chevron.left")
                    .resizable()
                    .frame(width: 0.04*screenWidth, height: 0.03*screenHeight)
                    .foregroundColor(.blue)
            }
            .frame(width: 0.04*screenWidth, height: 0.03*screenHeight)
            .padding(.leading, 0.05*screenWidth)
            .padding(.top)
            Spacer()
        }
    }
    
    /*
     
        Creates a recipe button with the data supplied by recipe.
     
     */
    func createRecipeButton(_ recipe: RecipeHash) -> some View {
        Button(action: {
            homePageData.selectedRecipeId = recipe.recipeId
            homePageData.isRecipeSelected = true
            homePageData.tabSelectedExplore = "Explore"
        }) {
            ZStack {
                //Image
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
                
                //Recipe name
                Text(recipe.name)
                    .shadow(color: .black, radius: 1)
                    .modifier(CustomTextLabelStyle(0.08*screenWidth, true))
                    .foregroundColor(.white)
                    .shadow(color: .black, radius: 1)
            }
            .frame(width: 0.90*screenWidth, height: 0.17*screenHeight)
            .contentShape(RoundedRectangle(cornerRadius: 15))
            .cornerRadius(15)
            .shadow(radius: 6)
        }
        .contentShape(Rectangle()) 
    }
    
    
    /*
     
        Creates and returns a new RecipeHash object based on an ingredient object passed in.
     
     */
    func createNewRecipeHash(_ recipe: PFObject, _ image: UIImage) -> RecipeHash {
        let name = recipe["name"] as? String ?? ""
        let recipeId = recipe.objectId as? String ?? ""

        return RecipeHash(name, image, recipeId, "", [])
    }
    
    /*
     
        Initializes recipeListObjects with recipes tagged under the current selected category.
     
     */
    func initializeRecipes() {
        isLoading = true
        let query = PFQuery(className: "Recipes")
        query.whereKey("tags", equalTo: homePageData.selectedCategoryObject.name)
        
        var tempRecipesArray: [RecipeHash] = []
        let dispatchGroup = DispatchGroup()

        query.findObjectsInBackground { (recipeObjects: [PFObject]?, error: Error?) in
            if let error = error {
                print("Error fetching recipes: \(error.localizedDescription)\n")
            } else if let recipeObjects = recipeObjects {
                for recipe in recipeObjects {
                    dispatchGroup.enter()

                    backendUtilities.getRecipeImage(recipe) { image in
                        defer { dispatchGroup.leave() }

                        if let image = image {
                            let newRecipe = createNewRecipeHash(recipe, image)
                            tempRecipesArray.append(newRecipe)
                        } else {
                            print("Unsuccessfully fetched image data.\n")
                        }
                    }
                }

                dispatchGroup.notify(queue: .main) {
                    isLoading = false
                    // If all recipes have been looped through, sort and initialize recipeListObjects
                    tempRecipesArray.sort { $0.name.localizedCaseInsensitiveCompare($1.name) == .orderedAscending }
                    recipeListObjects = tempRecipesArray

                }
            }
        }
    }
}
