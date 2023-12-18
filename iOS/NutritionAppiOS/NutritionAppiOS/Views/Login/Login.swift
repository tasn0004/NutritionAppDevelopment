import SwiftUI
import Parse

struct Login: View {

    @Environment(\.colorScheme) var colorScheme

    @EnvironmentObject private var homePageData: HomePageObservable
    @EnvironmentObject private var backendUtilities: BackendUtilities
    @ObservedObject private var screenDimensions = ScreenDimensions()
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0
    
    @State var showMainAppView = false
    @State var emailAddress: String = ""
    @State var password: String = ""

    @State private var showAlert: Bool = false
    @State private var alertTitle: String = ""
    @State private var alertMessage: String = ""
    
    @State private var isLoginDisabled = false
    
    @State private var showForgotPasswordPopover = false
    
    @FocusState private var isEmailFocused: Bool
    
    @State var builder = ViewBuilders(0.0, 0.0)
    
    /*
     
        Body
     
     */
    var body: some View {
        ZStack(alignment: .top) {
            GeometryReader { geometry in
                Color("background").ignoresSafeArea()
                    .onAppear {
                        screenDimensions.screenHeight = geometry.size.height
                        screenDimensions.screenWidth = geometry.size.width
                        
                        screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
                        builder = ViewBuilders(screenWidth, screenHeight)
                    }
            }

            VStack {
                builder.imageHeader()
                    .padding(.vertical)
                title()
                    .padding(.vertical)
                
                Spacer()
                dataInputArea()
                
                Spacer()
                Group {
                    forgotPassword()
                    loginButton()
                    signUp()
                }
                .modifier(CustomTextLabelStyle(0.045*screenWidth))
                Spacer()
            }
            .padding(.horizontal)
            
            NavigationLink(
                destination: MainAppView(),
                isActive: $showMainAppView) { EmptyView() }
        }
        .popover(isPresented: $showForgotPasswordPopover) {
            ForgotPasswordPopover(screenWidth, screenHeight)
        }
        .alert(isPresented: $showAlert) {
            Alert(
                title: Text(alertTitle),
                message: Text(alertMessage),
                dismissButton: .default(Text("OK")) {
                    //If alert is success, navigate to app on successful login after selecting ok.
                    if self.alertTitle == "Login Successful" {
                        navigateToApp()
                    }
                }
            )
        }
    }
    
    /*
     
        Creates the page title
     
     */
    func title() -> some View {
        Text("Welcome to Nutricooks")
            .foregroundColor(Color("text"))
            .font(.system(size: 0.06*screenWidth, weight: .bold, design: .rounded))
    }
    
    /*
     
        Creates the data input area.
     
     */
    func dataInputArea() -> some View {
        Group {
            builder.createTextFieldWithLabel("Email Address", "Enter your email address", $emailAddress)
                .keyboardType(.emailAddress)
            builder.createPasswordFieldWithLabel("Password", "Enter your password", $password)

        }
    }
    
    /*
     
        Creates the forgot password link.
     
     */
    func forgotPassword() -> some View {
        Button(action: {
            showForgotPasswordPopover = true
       }) {
           Text("Forgot password?")
               .shadow(color: .black, radius: 0.05)
               .padding(.top, 0.05*screenHeight)
       }
    }
    
    /*
     
        Verifies the email format
     
     */
    func verifyEmail() {
        let emailValidationRegex = "^[\\p{L}0-9!#$%&'*+\\/=?^_`{|}~-][\\p{L}0-9.!#$%&'*+\\/=?^_`{|}~-]{0,63}@[\\p{L}0-9-]+\\.[a-zA-Z]{2,7}$"
        let emailValidationPredicate = NSPredicate(format: "SELF MATCHES %@", emailValidationRegex)

        //if not valid, set alerts and reset value
        if !(emailValidationPredicate.evaluate(with: emailAddress)) {
            emailAddress = ""
            alertTitle = "Error"
            alertMessage = "Invalid email address format. Please enter again."
            showAlert = true
        }
    }

    /*
     
        Creates the login button. When selected, calls signin().
     
     */
    func loginButton() -> some View {
        Button(action: {
            onLoginSelect()
        }) {
            Text("Log In")
                .foregroundColor(.black)
        }
        .frame(width: 0.50*screenWidth, height: 0.08*screenHeight)
        .background(ComponentColours.submitButton)
        .cornerRadius(8)
        .padding(.vertical, 0.05*screenHeight)
        .disabled(isLoginDisabled)
        .shadow(radius: 6)
    }
    
    /*
     
        Creates the forgot password link.
     
     */
    func signUp() -> some View {
        NavigationLink(
            destination: AccountCreationPrimary().environmentObject(screenDimensions),
            label: {
                Text("Sign up")
                    .foregroundColor(.blue)
                    .shadow(color: .black, radius: 0.05)
            }
        )
    }
    
    func onLoginSelect() {
        if isDataValid() {
            isLoginDisabled = true
            signin()
        } else {
            alertTitle = "Error"
            
            if emailAddress.isEmpty && password.isEmpty {
                alertMessage = "Must enter an email address and password."
            } else if emailAddress.isEmpty {
                alertMessage = "Must enter an email address."
            } else if password.isEmpty {
                alertMessage = "Must enter a password."
            }
            
            showAlert = true
        }
    }
    
    func isDataValid() -> Bool { return !password.isEmpty && !emailAddress.isEmpty }
    
    /*
     
        Changes showMainAppView to true upon successful log in in order to trigger the navigation into the app. Sets the tabViewSelection on MainAppview to show home tab by default.
     
     */
    func navigateToApp() {
        homePageData.resetFields()
        showMainAppView = true
    }
    
    
    /*
     
        Calls the parse log in function with info from user.
     
     */
    func signin() {
        PFUser.logInWithUsername(inBackground: emailAddress, password: password) { user, error in
            if user != nil {
                // Update alert details for successful login.
                alertTitle = "Login Successful"
                alertMessage = ""
                showAlert = true
            } else if let error = error {
                // Update alert details with the error description.
                alertTitle = "Error"
                alertMessage = error.localizedDescription
                showAlert = true
            }
        }
    }
}

struct Login_Previews: PreviewProvider {
    static var previews: some View {
        Login()
    }
}
