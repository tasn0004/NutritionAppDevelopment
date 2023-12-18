import SwiftUI
import Parse

struct GroceryList: View {
    
    @Environment(\.colorScheme) var colorScheme
    
    @EnvironmentObject private var backendUtilities: BackendUtilities
    @EnvironmentObject private var mealPlanData: MealPlanObservable
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0

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

            TabView {
                GroceryListContent("daily")
                    .tabItem {
                        Text("Daily")
                    }
                
                GroceryListContent("weekly")
                    .tabItem {
                        Text("Weekly")
                    }
            }
            .tabViewStyle(.page)

        }
        .navigationBarTitle("")
        .navigationBarHidden(true)
        .navigationBarBackButtonHidden(true)
        .onAppear {
            startupOperation()
        }
    }
    
    /*
     
        Checks if grocery list needs resetting, and adds component styling.
     
     */
    func startupOperation() {
        screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
        UISegmentedControl.appearance().selectedSegmentTintColor = .orange
        
        //Reset grocery list when new week has started
        if shouldResetGroceryList() {
            backendUtilities.resetGroceryListData()
        }
    }
    
    /*
     
        Determines if the current date is present as a key in the grocery list. If not, this indicates that the week is over and we need to reset the grocery list for the new week, starting from the date of user preferred start day.
     
     */
    func shouldResetGroceryList() -> Bool {
        let formattedCurrentDate = dateFormatter.string(from: Date())
        let userGroceryList = backendUtilities.getUserGroceryList()
        let keyExists = userGroceryList.contains { $0.key == formattedCurrentDate }

        return keyExists ? false : true
    }
}
