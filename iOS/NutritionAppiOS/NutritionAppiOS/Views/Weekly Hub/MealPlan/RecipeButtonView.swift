import SwiftUI
import Parse

struct RecipeButtonView: View {
    
    @EnvironmentObject private var backendUtilities: BackendUtilities
    @EnvironmentObject private var homePageData: HomePageObservable
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0
    
    let recipe: RecipeHash

    @Binding private var selectedDate: Date
    
    @State private var isPressed: Bool = false

    @GestureState private var translation: CGSize = .zero
    @State private var offset: CGFloat = 0
    @State private var dragGestureTranslation: CGSize = .zero
    @State private var isDragging = false
    
    @State private var swipeCrossedThreshold = false
    
    @Binding private var shouldRefresh: Bool
    
    //Format for dictionary keys
    let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMMM d, yyyy"
        return formatter
    }()

    init(_ recipe: RecipeHash, _ shouldRefresh: Binding<Bool>, _ selectedDate: Binding<Date>) {
        self.recipe = recipe
        self._shouldRefresh = shouldRefresh
        self._selectedDate = selectedDate
    }

    /*
     
        View body
     
     */
    var body: some View {
        ZStack {
            deleteRectangle()
            
            Button(action: {
                homePageData.selectedRecipeId = recipe.recipeId
                homePageData.isRecipeSelected = true
            }) {
                ZStack {
                    // Image
                    if let image = recipe.imageName {
                        Image(uiImage: image)
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                            .frame(height: 0.08*screenHeight)
                            .shadow(radius: 10)
                    }
                    
                    // Dark overlay
                    Rectangle()
                        .foregroundColor(Color(.darkGray))
                        .opacity(0.35)
                    
                    // Recipe name
                    Text(recipe.name)
                        .font(.system(size: 0.08*screenWidth, weight: .bold))
                        .foregroundColor(.white)
                        .shadow(color: .black, radius: 1)
                }
                .offset(x: offset + translation.width)
                .highPriorityGesture(
                    DragGesture()
                        .updating($translation) { value, state, _ in
                            // Only allow leftward swipe
                            if value.translation.width < 0 {
                                state = CGSize(width: max(value.translation.width, -screenWidth / 2), height: 0)
                            }
                        }
                        .onEnded { gesture in
                            if -gesture.translation.width > screenWidth / 2 {
                                onDelete()
                            }
                            
                            // Reset the offset here to animate back to the starting position
                            withAnimation {
                                self.offset = 0
                            }
                        }
                )
            }
            .cornerRadius(15)
            .shadow(radius: 6)
            .contentShape(Rectangle())
            .onTapGesture {
                print("Navigating to recipe with id: " + recipe.recipeId + "\n")
                homePageData.selectedRecipeId = recipe.recipeId
                homePageData.isRecipeSelected = true
            }
        }// outer zstack
        .mask(
           LinearGradient(
               gradient: Gradient(colors: [.black, .black, .black, .clear]),
               startPoint: .top,
               endPoint: .bottom)
        )
        .contentShape(Rectangle())
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
        }
    }
    
    func deleteRectangle() -> some View {
        if translation.width < 0 {
            return
                AnyView(Group {
                    Rectangle()
                        .foregroundColor(.red)
                        .cornerRadius(15)
                    
                    HStack{
                        Spacer()
                        Image(systemName: "trash")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 0.06*screenWidth)
                            .foregroundColor(.white)
                    }
                    .padding(.trailing)
            })
        } else {
            return AnyView(EmptyView())
        }
    }

    
    /*
     
        Deletes a recipe from the database and updates the user's meal plan and grocery list dictionary. Called when the swipe left gesture on a recipe tile completes.
     
     */
    func onDelete() {
        //Get meal type array for recipe being deleted.
        let formattedDate = dateFormatter.string(from: selectedDate)
        var mealPlanDictionary = backendUtilities.getUserMealPlan()
        var mealPlanDay = backendUtilities.getUserMealPlanDay(formattedDate)
        var mealTypeArray = mealPlanDay[recipe.mealType] ?? []
        
        //Delete the recipeId from the array if it exists
        if let index = mealTypeArray.firstIndex(of: recipe.recipeId) {
            mealTypeArray.remove(at: index)
        } else {
            print("Recipe Id: " + recipe.recipeId + " not found in user's meal plan " + recipe.mealType + " array\n")
        }
        
        //Update the mealTypeArray in the current meal plan and set as new meal plan dictionary.
        mealPlanDay[recipe.mealType] = mealTypeArray
        mealPlanDictionary[formattedDate] = mealPlanDay
        
        backendUtilities.updateUserMealPlan(mealPlanDictionary) { success in
            if success {
                print("Successfully updated meal plan after deleting " + recipe.name + " with recipeId: " + recipe.recipeId + " from user's " + recipe.mealType + "\n")
                deleteFromGroceryList()
                shouldRefresh = true
            } else {
                print("Unsuccessfully updated meal plan after deleting " + recipe.name + " with recipeId: " + recipe.recipeId + " from user's " + recipe.mealType + "\n")
            }
        }
    }
    
    /*
     
        Deletes the ingredients of a deleted recipe from the user's grocery list dictionary.
     
     */
    func deleteFromGroceryList() {
        //get grocery list for the date selected
        let formattedDate = dateFormatter.string(from: selectedDate)
        var userGroceryList = backendUtilities.getUserGroceryList()
        var groceryListDay = backendUtilities.getUserGroceryListDay(formattedDate)

        //hold qty value of an ingredients total qty in the recipe being deleted
        var ingredientQuantity: Double
        var ingredientName: String
        var existingQuantity: Double
        var newQuantity: Double
        
        for ingredient in recipe.ingredients {
            ingredientName = ingredient[0] as? String ?? ""
            
            //find index of ingredient in grocery list
            if let existingIngredientIndex = groceryListDay.firstIndex(where: { $0["ingredientName"] as? String == ingredientName }) {
                
                existingQuantity = groceryListDay[existingIngredientIndex]["quantity"] as? Double ?? 0.0
                ingredientQuantity = ingredient[1] as? Double ?? 0.0
                newQuantity = existingQuantity - ingredientQuantity

                // Check if the new quantity will be 0, if yes then delete item from grocery list for date key or update the quantity value for the existing ingredient
                if newQuantity <= 0 {
                    groceryListDay.remove(at: existingIngredientIndex)
                } else {
                    groceryListDay[existingIngredientIndex]["quantity"] = newQuantity
                }
            }
        }

        //Update the userGroceryList dictionary with the modified array
        userGroceryList[formattedDate] = groceryListDay
        
        backendUtilities.updateUserGroceryList(userGroceryList) { success in
            if success {
                print("Successfully updated grocery list after deleting " + recipe.name + " with recipeId: " + recipe.recipeId + " from user's " + recipe.mealType + "\n")
            } else {
                print("Unsuccessfully updated grocery list after deleting " + recipe.name + " with recipeId: " + recipe.recipeId + " from user's " + recipe.mealType + "\n")
            }
        }
    }
}

