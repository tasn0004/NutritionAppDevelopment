import SwiftUI

struct MealPlanAddMealButton: View {

    @EnvironmentObject var mealPlanData: MealPlanObservable
    @EnvironmentObject var homePageData: HomePageObservable
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0
    
    let mealTypeValue: String
    
    @Binding private var selectedDate: Date
    
    let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "MMMM d, yyyy"
        return formatter
    }()

    init(_ mealTypeValue: String, _ selectedDate: Binding<Date>) {
        self.mealTypeValue = mealTypeValue
        self._selectedDate = selectedDate
    }
    
    /*
     
        View body
     
     */
    var body: some View {
        Button(action: {
            //set context for meal plan addition
            let formattedDate = dateFormatter.string(from: selectedDate)
            mealPlanData.dateSelected = formattedDate
            mealPlanData.mealTypeSelected = mealTypeValue
            
            //Set home page data for proper control of views
            homePageData.resetFields()
            
            homePageData.navigateToHome()
        }) {
            ZStack {
                Rectangle()
                    .foregroundColor(.orange)
                    .frame(height: 0.08*screenHeight)
                    .shadow(radius: 10)

                HStack {
                    Image(systemName: "plus.circle")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 0.06*screenWidth)

                    Text("Add to Meal Plan")
                        .font(.system(size: 0.05*screenWidth, weight: .bold))
                        .shadow(color: .black, radius: 1)
                }
                .foregroundColor(.white)
            }
        }
        .cornerRadius(15)
        .shadow(radius: 6)
        .contentShape(Rectangle())
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
        }
    }
}
