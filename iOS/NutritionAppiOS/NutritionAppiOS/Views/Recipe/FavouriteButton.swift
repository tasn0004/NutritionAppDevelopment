import SwiftUI
import Parse

struct FavouriteButton: View {
    
    @Environment(\.colorScheme) private var colorScheme
    
    @EnvironmentObject private var backendUtilities: BackendUtilities
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State private var screenWidth = 0.0
    @State private var screenHeight = 0.0
    
    var recipeId: String
    @Binding var isFavourited: Bool
    
    init(_ recipeId: String, _ isFavourited: Binding<Bool>) {
        self.recipeId = recipeId
        self._isFavourited = isFavourited
    }
    
    /*
     
        View body
     
     */
    var body: some View {
        Button(action: {
            //Call the appropriate function depending on current isFavourited state.
            if isFavourited {
                removeFromFavouritedRecipes()
            } else {
                addToFavouritedRecipes()
            }
        }) {
            Image(systemName: isFavourited ? "bookmark.fill" : "bookmark")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 0.04*screenWidth)
                .foregroundColor(.orange)
        }
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
        }
    }

    /*
     
        Adds this recipe's id to the user's favourited recipes list.
     
     */
    func addToFavouritedRecipes() {
        backendUtilities.updateUserFavouritedRecipes(recipeId, isFavourited) { success in
            if success {
                print("Successfully added \(recipeId) to user's favourited recipes list.\n")
                modifyTimesFavourited(1)
                isFavourited = true
            } else {
                print("Unsuccessfully added \(recipeId) to user's favourited recipes list.\n")
            }
        }
    }
    
    /*
     
        Removes this recipe's id from the user's favourited recipes list.
     
     */
    func removeFromFavouritedRecipes() {
        backendUtilities.updateUserFavouritedRecipes(recipeId, isFavourited) { success in
            if success {
                print("Successfully removed \(recipeId) from user's favourited recipes list.\n")
                modifyTimesFavourited(-1)
                isFavourited = false
            } else {
                print("Unsuccessfully removed \(recipeId) from user's favourited recipes list.\n")
            }
        }
    }
    
    /*
     
        Increments or decrements the recipe's times favourited count by 1.
     
     */
    func modifyTimesFavourited(_ incrementFactor: Int) {
        let query = PFQuery(className: "Recipes")
        query.getObjectInBackground(withId: recipeId) { (recipeObject, error) in
            if let error = error {
                print("Error fetching recipe id: \(recipeId) \(error.localizedDescription)\n")
                
            } else if let recipeObject = recipeObject {
                
                if incrementFactor == 1 {
                    recipeObject.incrementKey("timesFavourited", byAmount: 1)
                } else {
                    recipeObject.incrementKey("timesFavourited", byAmount: -1)
                }
                
                recipeObject.saveInBackground { (success, error) in
                    if success {
                        print("Successfully updated the times favourited value for recipe \(recipeId).\n")
                    } else if let error = error {
                        print("Unsuccessfully updated the times favourited value for recipe \(recipeId). \(error.localizedDescription)\n")
                    }
                }
            }
        }
    }
}
