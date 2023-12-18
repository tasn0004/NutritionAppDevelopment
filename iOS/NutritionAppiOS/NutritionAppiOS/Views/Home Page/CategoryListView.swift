import SwiftUI
import Parse

struct CategoryListView: View {
    
    @Environment(\.colorScheme) private var colorScheme

    @EnvironmentObject private var backendUtilities: BackendUtilities
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    @EnvironmentObject private var homePageData: HomePageObservable
    @EnvironmentObject var mealPlanData: MealPlanObservable
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0
    
    var isMealTypeSelected: Bool {
        mealPlanData.mealTypeSelected.isEmpty
    }
    
    @State private var isLoading = true
    
    @State var categoryObjects: [RecipeCategoryHash] = []
    
    /*
     
        View body
     
     */
    var body: some View {
        ZStack {
            LoadingSpinner($isLoading)
            
            ScrollView {
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible(), spacing: 0.03*screenHeight)]) {
                    ForEach(categoryObjects, id: \.self) { category in
                        createCategoryButton(category)
                            .padding(.bottom, 0.015*screenHeight)
                    }
                }
                .padding()
            }
            .clipped()
            .onAppear {
                screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
                homePageData.searchResults = []
                initializeCategories()
            }
        }
    }
    
    /*
     
        Creates a button for a category
     
     */
    func createCategoryButton(_ category: RecipeCategoryHash) -> some View {
        Button {
            //set the selected category data in the environment
            homePageData.selectedCategoryObject = category
            homePageData.isCategorySelected = true

        } label: {
            ZStack {
                //Image
                if let image = category.imageName {
                    Image(uiImage: image)
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .clipped()
                }
                //Dark overlay
                Rectangle()
                    .foregroundColor(Color(.darkGray))
                    .opacity(0.35)
                
                //Recipe name
                Text(category.name)
                    .modifier(CustomTextLabelStyle(0.06*screenWidth, true))
                    .padding()
                    .shadow(color: .black, radius: 1)
                    .foregroundColor(.white)
                    .minimumScaleFactor(0.5)
                    .lineLimit(1)
            }
            .frame(width: 0.43*screenWidth, height: 0.18*screenHeight)
            .cornerRadius(15)
            .shadow(radius: 6)
        }
        .contentShape(Rectangle())
    }
    
    /*
     
        Initializses categoryObjects with all category data from database.s
        
     */
    func initializeCategories() {
        isLoading = true
        let query = PFQuery(className: "Category")
        var tempCategoriesArray: [RecipeCategoryHash] = []

        let dispatchGroup = DispatchGroup()

        query.findObjectsInBackground { (categories, error) in
            if let error = error {
                print("Error fetching categories: \(error.localizedDescription)\n")
                
            } else if let categories = categories {
                for category in categories {
                    dispatchGroup.enter()

                    backendUtilities.getCategoryImage(category) { image in
                        if let image = image {
                            let category = RecipeCategoryHash(category["name"] as? String ?? "", image)
                            tempCategoriesArray.append(category)
                        } else {
                            print("Unsuccessfully fetched image data.\n")
                        }
                        defer { dispatchGroup.leave() }
                    }
                }

                dispatchGroup.notify(queue: .main) {
                    isLoading = false
                    //If all categories have been looped through, sort and initialize categoryObjects
                    tempCategoriesArray.sort { $0.name.localizedCaseInsensitiveCompare($1.name) == .orderedAscending }
                    categoryObjects = tempCategoriesArray
                }
            }
        }
    }

}
