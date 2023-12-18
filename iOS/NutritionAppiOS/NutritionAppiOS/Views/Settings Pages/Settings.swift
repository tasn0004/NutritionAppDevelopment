import SwiftUI
import Parse

struct Settings: View {
    
    @AppStorage("isDarkModeEnabled") var isDarkModeEnabled: Bool = false
    @Environment(\.colorScheme) private var colorScheme
    
    @EnvironmentObject private var backendUtilities: BackendUtilities
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State private var screenWidth = 0.0
    @State private var screenHeight = 0.0
    
    @State private var navigateToLogin = false
    
    /*
     
        View body
     
     */
    var body: some View {
        ZStack(alignment: .top) {
            Color("background").ignoresSafeArea()

            VStack {
                Spacer()
                HStack {
                    Text(backendUtilities.getUserFullName())
                        .modifier(CustomTextLabelStyle(0.045*screenWidth, true))
                    Spacer()
                }
                
                Divider()
                Spacer()
                
                settingsList()
                Spacer()
            }
            .padding(.horizontal)

            NavigationLink("", destination: Login()
                                                .navigationBarTitle("")
                                                .navigationBarHidden(true)
                                                .navigationBarBackButtonHidden(true),
                                isActive: $navigateToLogin)
        }
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
        }
    }
    
    func colourSchemeSelector(_ imageName: String, _ textStringTop: String, _ textStringBottom: String) -> some View {
        Group {
            HStack {
                Image(systemName: imageName)
                    .resizable()
                    .scaledToFit()
                    .foregroundColor(.orange)
                    .frame(width: 0.06*screenWidth, height: 0.03*screenHeight)

                VStack(alignment: .leading) {
                    Text(textStringTop)
                        .foregroundColor(Color("text"))
                        .modifier(CustomTextLabelStyle(0.038*screenWidth))

                    Text(textStringBottom)
                        .foregroundColor(.gray)
                        .modifier(CustomTextLabelStyle(0.034*screenWidth))
                }
                Spacer()
                
                HStack {
                    Toggle("", isOn: $isDarkModeEnabled)
                        .labelsHidden()
                    
                    Image(systemName: "moon.stars")
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 0.05 * screenWidth)
                        .foregroundColor(Color("text"))
                }
            }
        }
    }
    
    func settingsList() -> some View {
        VStack(spacing: 0.03*screenHeight) {
            createSettingOption("person.crop.circle", "My Account", "Edit your profile information", { SettingsMyAccount() })
            createSettingOption("wallet.pass", "Payment", "Manage your subscription", { EmptyView() })
            createSettingOption("cursor.rays", "Promotions", "Promo code that applies to your payment", { EmptyView() })
            createSettingOption("list.dash.header.rectangle", "Diet Preferences", "Edit your diet preferences", { SettingDietPreferences() })
            createSettingOption("heart.text.square", "Health Concerns", "Edit your health concerns", { SettingHealthConcern() })
            createSettingOption("sun.min", "Theme", "Adjust your theme", { EmptyView() })
            createSettingOption("info.circle", "About", "About us", { ApplicationDocumentView("About", "About") })
            createSettingOption("lock.shield", "Privacy", "Read our privacy policy", { ApplicationDocumentView("Privacy Policy", "Privacy") })
            createSettingOption("rectangle.portrait.and.arrow.right", "Log Out", "Log out of your profile", { Login() })
            
        }
    }
    
    /*
     
        Creates navigation links to the views passed in as destination. Views for links are settingOptionViews. When log out is selected, log out user in environment and navigate to login.
     
     */
    func createSettingOption<Destination: View>(_ imageName: String,_ textStringTop: String,_ textStringBottom: String, @ViewBuilder _ destination: @escaping () -> Destination) -> some View {
            //If log out selected, log user out and navigat to login page
            if textStringTop == "Log Out" {
                return AnyView(
                    Button(action: {
                        PFUser.logOut()
                        backendUtilities.resetCurrentUser()
                        navigateToLogin = true
                        
                    }) {
                        settingOptionView(imageName, textStringTop, textStringBottom)
                    }
                )
            } else if textStringTop == "Theme" {
                return AnyView(colourSchemeSelector(imageName, textStringTop, textStringBottom))
                
            } else {
                return AnyView(
                    NavigationLink(destination: destination().navigationBarBackButtonHidden(false)) {
                        settingOptionView(imageName, textStringTop, textStringBottom)
                    }
                )
            }
        }
    
    /*
     
        Creates the view for a settings option.
     
     */
    func settingOptionView(_ imageName: String, _ textStringTop: String, _ textStringBottom: String) -> some View {
        HStack(spacing: 0.03*screenWidth) {
            Image(systemName: imageName)
                .resizable()
                .scaledToFit()
                .foregroundColor(.orange)
                .frame(width: 0.06*screenWidth, height: 0.03*screenHeight)

            VStack(alignment: .leading) {
                Text(textStringTop)
                    .foregroundColor(Color("text"))
                    .modifier(CustomTextLabelStyle(0.038*screenWidth))

                Text(textStringBottom)
                    .foregroundColor(.gray)
                    .modifier(CustomTextLabelStyle(0.034*screenWidth))
            }
            Spacer()
        }
    }
}
