import Parse
import SwiftUI

struct LikeButton: View {
    
    @Environment(\.colorScheme) private var colorScheme
    
    @EnvironmentObject private var backendUtilities: BackendUtilities
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State private var screenWidth = 0.0
    @State private var screenHeight = 0.0
    
    var recipeId: String
    @Binding var isLiked: Bool
    @Binding var timesLiked: Int
    
    init(_ recipeId: String, _ isLiked: Binding<Bool>, _ timesLiked: Binding<Int>) {
        self.recipeId = recipeId
        self._isLiked = isLiked
        self._timesLiked = timesLiked
    }

    /*
     
        View body
     
     */
    var body: some View {
        Button(action: {
            //Call the appropriate function depending on current isLiked state.
            if isLiked {
                removeFromLikedRecipes()
            } else {
                addToLikedRecipes()
            }
        }) {
            Image(systemName: isLiked ? "heart.fill" : "heart")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 0.062*screenWidth)
                .foregroundColor(.red)
        }
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
        }
    }
    
    /*
     
        Adds this recipe's id to the user's liked recipes list.
     
     */
    func addToLikedRecipes() {
        backendUtilities.updateUserLikedRecipes(recipeId, isLiked) { success in
            if success {
                print("Successfully added \(recipeId) to user's liked recipes list.\n")
                modifyTimesLiked(1)
                isLiked = true
            } else {
                print("Unsuccessfully added \(recipeId) to user's liked recipes list.\n")
            }
        }
    }
    
    /*
     
        Removes this recipe's id from the user's liked recipes list.
     
     */
    func removeFromLikedRecipes() {
        backendUtilities.updateUserLikedRecipes(recipeId, isLiked) { success in
            if success {
                print("Successfully removed \(recipeId) from user's liked recipes list.\n")
                modifyTimesLiked(-1)
                isLiked = false
            } else {
                print("Unsuccessfully removed \(recipeId) from user's liked recipes list.\n")
            }
        }
    }

    /*
     
        Increments or decrements the recipe's times liked count by 1.
     
     */
    func modifyTimesLiked(_ incrementFactor: Int) {
        let query = PFQuery(className: "Recipes")
        query.getObjectInBackground(withId: recipeId) { (recipeObject, error) in
            if let error = error {
                print("Error fetching recipe id: \(recipeId) \(error.localizedDescription)\n")
                
            } else if let recipeObject = recipeObject {
                
                if incrementFactor == 1 {
                    recipeObject.incrementKey("timesLiked", byAmount: 1)
                    timesLiked += 1
                } else {
                    recipeObject.incrementKey("timesLiked", byAmount: -1)
                    timesLiked -= 1
                }
                
                recipeObject.saveInBackground { (success, error) in
                    if success {
                        print("Successfully updated the times liked value for recipe \(recipeId).\n")
                    } else if let error = error {
                        print("Unsuccessfully updated the times liked value for recipe \(recipeId). \(error.localizedDescription)\n")
                    }
                }
            }
        }
    }
}
