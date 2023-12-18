import SwiftUI

struct HomePage: View {
    
    @Environment(\.colorScheme) private var colorScheme
    
    @EnvironmentObject private var homePageData: HomePageObservable
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0

    @State var tabSelected: String = "Explore"

    /*
     
        View body
     
     */
    var body: some View {
        ZStack {
            Color("background").ignoresSafeArea()
            
            switch homePageData.isCategorySelected {
                case true:
                    CategorySelectedView() 
                case false:
                    homeTabView()
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
                .foregroundColor(.black.opacity(0.5))
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
            homePageData.tabSelectedExplore = tabName
        }) {
            VStack(spacing: 0.01*screenHeight) {
                Text(tabName)
                    .foregroundColor(homePageData.tabSelectedExplore == tabName ? .orange : Color("text"))
                
                Rectangle()
                    .frame(width: 0.25*screenWidth, height: 0.0009*screenHeight)
                    .foregroundColor(.orange)
                    .opacity(homePageData.tabSelectedExplore == tabName ? 1.0 : 0.0)
            }
            
        }
    }
    
    /*
     
        Creates the explore/favourites tab view on the home page.
     
     */
    func homeTabView() -> some View {
        VStack() {
            //Top tab bar. Buttons to change state of tabSelected to dynamically update the body content.
            HStack() {
                Spacer()
                createTabButton("Explore")
                innerTabSpace()
                createTabButton("Favourites")
                Spacer()
            }
            .background(Color("tabs"))
            .contentShape(Rectangle())
            
            //Dynamically generate the body of the view with the view corresponding to current tab selected.
            switch homePageData.tabSelectedExplore {
               case "Explore":
                    HomePageExplore()

               case "Favourites":
                    HomePageFavourites()

               default:
                   EmptyView()
            }
        }
    }
}
