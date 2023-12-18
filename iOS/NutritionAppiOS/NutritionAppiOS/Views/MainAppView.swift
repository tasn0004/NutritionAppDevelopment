import SwiftUI

struct MainAppView: View {
    
    @Environment(\.colorScheme) private var colorScheme

    @EnvironmentObject private var homePageData: HomePageObservable
    @EnvironmentObject private var backendUtilities: BackendUtilities
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0

    /*
     
        View body
     
     */
    var body: some View {
        ZStack(alignment: .top) {
            Color("background").ignoresSafeArea()
            
            //Switch between showing the app or the recipe view of a selected recipe.
            switch homePageData.isRecipeSelected {
                case true:
                    RecipeView(homePageData.selectedRecipeId).navigationBarBackButtonHidden(true)
                
                case false:
                    VStack {
                        
                        TabView(selection: $homePageData.tabViewSelection) {
                            HomePage()
                                .tag(0)
                                .tabItem {
                                    Image(systemName: "house")
                                    Text("Home")
                                }
                            
                            WeeklyHub()
                                .tag(1)
                                .tabItem {
                                    Image(systemName: "list.bullet")
                                    Text("Weekly Hub")
                                }
                            
                            Settings()
                                .tag(2)
                                .tabItem {
                                    Image(systemName: "person")
                                    Text("Settings")
                                }
                                
                        }
                        .edgesIgnoringSafeArea(.top)
                        .accentColor(.orange)
                    }
            }
        }
        .onAppear {
            startupOperation()
        }
        .navigationBarTitle("")
        .navigationBarHidden(true)
        .navigationBarBackButtonHidden(true)
    }
    
    /*
     
        Styles the veiw onAppear
     
     */
    func startupOperation() {
        screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
        backendUtilities.initializeCurrentUser()
        backendUtilities.initializeIsUserPaidAccount()

        UITabBar.appearance().barTintColor = .systemBackground
        UITabBar.appearance().shadowImage = UIImage()
        UITabBar.appearance().backgroundImage = UIImage()
        UITabBar.appearance().unselectedItemTintColor = UIColor(Color("text"))
        UITabBar.appearance().isTranslucent = true
        UITabBar.appearance().backgroundColor = UIColor(Color("tabs"))
    }
}







