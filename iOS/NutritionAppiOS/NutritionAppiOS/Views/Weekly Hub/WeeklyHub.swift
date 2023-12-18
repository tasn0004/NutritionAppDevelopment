import SwiftUI

struct WeeklyHub: View {

    @Environment(\.colorScheme) private var colorScheme
    
    @EnvironmentObject private var backendUtilities: BackendUtilities
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0
    
    @State var tabSelected: String = "Meal Plan"

    /*
     
        View body
     
     */
    var body: some View {
        ZStack(alignment: .top) {
            Color("background").ignoresSafeArea()

            VStack(spacing: 0) {
                //Top tab bar. Buttons to change state of tabSelected to dynamically update the body content.
                HStack {
                    Spacer()
                    createTabButton("Meal Plan")
                    innerTabSpace()
                    createTabButton("Targets")
                    innerTabSpace()
                    createTabButton("Grocery List")
                    Spacer()
                }
                .background(Color("tabs"))
                .contentShape(Rectangle())
                .padding(.bottom)
                
                //Dynamically generate the body of the view with the view corresponding to current tab selected.
                switch tabSelected {
                    case "Meal Plan":
                        WeeklyMealPlan()
                    case "Targets":
                        WeeklyTargets()
                    case "Grocery List":
                        GroceryList()
                    default:
                        EmptyView()
                }
            }
        }
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
        }
    }

    /*
     
        Creates the space and line between tab buttons.
     
     */
    func innerTabSpace() -> some View {
        Group {
            Spacer()
            Rectangle()
                .frame(width: 0.001*screenWidth, height: 0.03*screenHeight)
                .foregroundColor(.black)
                .opacity(0.5)
                .cornerRadius(20)
                .shadow(radius: 10)
            Spacer()
        }
    }
    
    /*
     
        Creates a tab button for the top navigation bar.
     
     */
    func createTabButton(_ tabName: String) -> some View {
        Button(action: {
            tabSelected = tabName
        }) {
            VStack(spacing: 0.01*screenHeight) {
                Text(tabName)
                    .foregroundColor(tabSelected == tabName ? .orange : Color("text"))
                
                Rectangle()
                    .frame(width: 0.25*screenWidth, height: 0.0009*screenHeight)
                    .foregroundColor(.orange)
                    .opacity(tabSelected == tabName ? 1.0 : 0.0)
            }
        }
        .disabled(!backendUtilities.isUserPaidAccount)
    }
}
