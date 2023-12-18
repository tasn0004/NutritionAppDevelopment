import SwiftUI

struct RecipeSearchView: View {
    
    @Environment(\.colorScheme) private var colorScheme
    @Environment(\.presentationMode) var presentationMode

    @EnvironmentObject private var homePageData: HomePageObservable
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0

    /*
     
        View body
     
     */
    var body: some View {
        //Recipes
        ScrollView {
            LazyVStack(spacing: 0.05*screenWidth) {
                ForEach(homePageData.searchResults, id: \.self) { recipe in
                    createRecipeButton(recipe)
                }
            }
        }
        .clipped()
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
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
                    .shadow(color: .black, radius: 1)
                    .foregroundColor(.white)
            }
            .frame(width: 0.90*screenWidth, height: 0.17*screenHeight)
            .contentShape(RoundedRectangle(cornerRadius: 15))
            .cornerRadius(15)
            .shadow(radius: 6)
        }
        .contentShape(Rectangle()) 
    }
}
