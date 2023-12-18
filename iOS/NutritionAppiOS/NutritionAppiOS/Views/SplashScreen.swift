import SwiftUI
import Parse

struct SplashScreen: View {
    
    @Environment(\.colorScheme) var colorScheme
    
    @EnvironmentObject var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0

    @State var isActive: Bool = false
    @State private var opacity = 0.5
    @State private var size = 0.8
    
    /*
     
        View body
     
     */
    var body: some View {
        GeometryReader { geometry in
            NavigationView {
                ZStack {
                    //If a user is signed in, navigate to the home page, else navigate to the login page
                    if isActive {
                        if PFUser.current() == nil {
                            login()
                        } else {
                            mainAppView()
                        }
                        
                    } else {
                        //Splash screen content
                        ZStack {
                            Color("background").ignoresSafeArea()
                            
                            VStack {
                                VStack {
                                    Image(systemName: "fork.knife.circle.fill")
                                        .font(.system(size: 150))
                                        .foregroundColor(.orange)
                                    
                                    Text("Nutrition App")
                                        .font(Font.custom("Baskerville-Bold", size: 30))
                                        .foregroundColor(colorScheme == .dark ? .white : .black)
                                        .opacity(0.80)
                                }
                                .scaleEffect(size)
                                .opacity(opacity)
                                .onAppear {
                                    withAnimation(.easeIn(duration: 1.2)) {
                                        self.size = 0.9
                                        self.opacity = 1.0
                                    }
                                }
                            }
                        }
                        .onAppear {
                            DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
                                self.isActive = true
                            }
                        }
                    }
                }
            }
            .navigationBarTitle("")
            .navigationBarHidden(true)
            .navigationBarBackButtonHidden(true)
            .navigationViewStyle(StackNavigationViewStyle())
            .onAppear {
                setEnvironmentScreenDimensions(geometry.size.width, geometry.size.height)
                screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
            }
        }
    }
    
    /*
     
        Creates a naviagtion link to the login page.
     
     */
    func login() -> some View {
        NavigationLink("", destination: Login()
                                            .navigationBarTitle("")
                                            .navigationBarHidden(true)
                                            .navigationBarBackButtonHidden(true)
                                            .navigationViewStyle(StackNavigationViewStyle()),
                        isActive: $isActive)
    }
    
    /*
     
        Creates a naviagtion link to the apps entry point.
     
     */
    func mainAppView() -> some View {
        NavigationLink("", destination: MainAppView()
                                            .navigationBarTitle("")
                                            .navigationBarHidden(true)
                                            .navigationBarBackButtonHidden(true)
                                            .navigationViewStyle(StackNavigationViewStyle()),
                            isActive: $isActive)
    }
    
    /*
     
        Set the screen dimensions in the environment.
     
     */
    func setEnvironmentScreenDimensions(_ width: Double, _ height: Double) {
        screenDimensions.screenWidth = width
        screenDimensions.screenHeight = height
    }
}

struct SplashScreen_Previews: PreviewProvider {
    static var previews: some View {
        SplashScreen()
    }
}
