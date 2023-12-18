import SwiftUI

struct AccountCreationPrimary: View {
    
    @StateObject var userData = UserAccount()
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0
    
    @State var firstName: String = ""
    @State var lastName: String = ""
    @State var emailAddress: String = ""
    @State var password: String = ""
    @State var confirmPassword: String = ""
    
    @State var termsAndConditionsAccepted = false

    var logoAssetName: String = "broccoli"
    
    @State private var isNextAlertPresented = false
    @State private var isEmailAlertPresented = false
    @State private var isPasswordAlertPresented = false
    @State private var isTermsAlertPresented = false
    
    @State private var alertTitle = ""
    @State private var alertMessage = ""
    
    @FocusState private var isEmailFocused: Bool
    @FocusState private var isConfirmPasswordFocused: Bool
    
    var isNextButtonEnabled: Bool {
        return !firstName.isEmpty
            && !lastName.isEmpty
            && !emailAddress.isEmpty
            && !password.isEmpty
            && !confirmPassword.isEmpty
            && password == confirmPassword
            && termsAndConditionsAccepted
    }
    
    @State var builder = ViewBuilders(0.0, 0.0)
    
    var body: some View {
        ZStack(alignment: .top) {
            Color("background").ignoresSafeArea()
            
            VStack {
                Spacer()
                builder.imageHeader()
                Spacer()

                inputFieldArea()
                Spacer()
                termsAndConditionsStatement()
                nextButton()
            }
            .padding(.horizontal)
            
        }
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
            builder = ViewBuilders(screenWidth, screenHeight)
        }
    }
    
    /*
     
     
     
     */
    func inputFieldArea() -> some View {
        Group {
            builder.createTextFieldWithLabel("First Name", "Enter First Name", $firstName)
            builder.createTextFieldWithLabel("Last Name", "Enter Last Name", $lastName)
            builder.createTextFieldWithLabel("Email Address", "Enter Email Address", $emailAddress)
                .focused($isEmailFocused)
                .onChange(of: isEmailFocused) { isFocused in
                    if self.emailAddress != "" { verifyEmail() }
                }
                .alert(isPresented: $isEmailAlertPresented) {
                    Alert(title: Text(self.alertTitle), message: Text(self.alertMessage), dismissButton: .default(Text("OK")))
                }
            
            builder.createPasswordFieldWithLabel("Password", "Enter Password", $password)
            builder.createPasswordFieldWithLabel("Confirm Password", "Confirm Password", $confirmPassword)
                .focused($isConfirmPasswordFocused)
                .onChange(of: isConfirmPasswordFocused) { isFocused in
                    if self.confirmPassword != "" { verifyPassword() }
                }
                .alert(isPresented: $isPasswordAlertPresented) {
                    Alert(title: Text(self.alertTitle), message: Text(self.alertMessage), dismissButton: .default(Text("OK")))
                }
        }
    }
    
    /*
     
     
     
     */
    func termsAndConditionsStatement() -> some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(spacing: 0) {
                Toggle(isOn: $termsAndConditionsAccepted) {}
                    .toggleStyle(iOSCheckboxToggleStyle())
                    .foregroundColor(Color("text"))
                        
                Text("  I acknowledge and agree to the ")
                
                Button(action: {
                    
                }) {
                    Text("Terms of Use")
                        .underline()
                        .foregroundColor(.blue)
                }
            }
            
            HStack(spacing: 0) {
                Text("  & ")
                
                Button(action: {

                }) {
                    Text("Privacy Policy")
                        .underline()
                        .foregroundColor(.blue)
                }
            }
            .padding(.leading, 0.04*screenWidth)
        }
        .modifier(CustomTextLabelStyle(0.04*screenWidth))
    }
    
    func nextButton() -> some View {
        builder.createNextButton({ AccountCreationBiometrics().environmentObject(userData) })
            .disabled(!isNextButtonEnabled)
            .simultaneousGesture(TapGesture().onEnded {
                if !isNextButtonEnabled {
                    handleDisabledNextClick()
                } else {
                    updateUserData()
                }
            })
            .alert(isPresented: $isNextAlertPresented) {
                Alert(title: Text(alertTitle), message: Text(alertMessage), dismissButton: .default(Text("OK")))
            }
    }
    
    /*
     
        Called upon selecting the next button. Updates the user model to continue account creation.
     
     */
    func updateUserData() {
        userData.firstName = self.firstName
        userData.lastName = self.lastName
        userData.emailAddress = self.emailAddress
        userData.password = self.password
    }
        
    /*
     
        Called to handle the logic for clicking the disabled next button. Sets alert data and displays it.
     
     */
    func handleDisabledNextClick() {
        self.alertTitle = "Error"
        self.alertMessage = "Missing data. Please fill out all fields and agree to the terms of use & privacy policy."
        isNextAlertPresented.toggle()
    }
    
    /*
        Called to verify a valid email address has been entered. Sets alert data and displays it when invalid.
     */
    func verifyEmail() {
        let emailValidationRegex = "^[\\p{L}0-9!#$%&'*+\\/=?^_`{|}~-][\\p{L}0-9.!#$%&'*+\\/=?^_`{|}~-]{0,63}@[\\p{L}0-9-]+\\.[a-zA-Z]{2,7}$"
        let emailValidationPredicate = NSPredicate(format: "SELF MATCHES %@", emailValidationRegex)

        //if not valid, set alerts and reset value
        if !(emailValidationPredicate.evaluate(with: self.emailAddress)) {
            self.emailAddress = ""
            self.alertTitle = "Error"
            self.alertMessage = "Invalid email address format. Please enter again."
            self.isEmailAlertPresented.toggle()
        }
    }
    
    /*
        Called to verify password input. Sets alert data and displays it when invalid.
     */
    func verifyPassword() {
        //if passwords dont match
        if self.password != self.confirmPassword {
            self.confirmPassword = ""
            self.alertTitle = "Error"
            self.alertMessage = "Passwords don't match! Please confirm your password again."
            self.isPasswordAlertPresented.toggle()
        }
    }
}




