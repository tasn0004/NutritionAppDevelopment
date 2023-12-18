import SwiftUI
import Parse

struct ForgotPasswordPopover: View {
    
    @Environment(\.colorScheme) var colorScheme

    @ObservedObject private var screenDimensions = ScreenDimensions()
    
    //Screen dimensions
    var screenWidth: Double
    var screenHeight: Double

    @State var emailAddress: String = ""

    @State private var showAlert: Bool = false
    @State private var alertTitle: String = ""
    @State private var alertMessage: String = ""
    
    @State private var isValidEmail = false
    
    @FocusState private var isEmailFocused: Bool
    
    @State var builder = ViewBuilders(0.0, 0.0)
    
    init(_ screenWidth: Double, _ screenHeight: Double) {
        self.screenWidth = screenWidth
        self.screenHeight = screenHeight
    }

    /*
     
        View body
     
     */
    var body: some View {
        VStack {
            //Swipe to close rectangle marker
            RoundedRectangle(cornerRadius: 10)
                .frame(width: 0.10*screenWidth, height: 0.01*screenHeight)
                .foregroundColor(.gray)
                .opacity(0.5)
                .padding(.vertical)

            builder.createTextFieldWithLabel("Email address: ", emailAddress, $emailAddress)
                .keyboardType(.emailAddress)
            
            Spacer()
            
            //Submit button
            Button(action: {
                verifyEmail()
                
                if isValidEmail {
                    do {
                        try PFUser.requestPasswordReset(forEmail: emailAddress)
                        alertTitle = "Success!"
                        alertMessage = "Password reset instructions sent to email address \(emailAddress)"
                        showAlert = true
                        emailAddress = ""
                    } catch {
                        print("Error: \(error.localizedDescription)")
                    }
                }
            }) {
                Text("Submit")
                    .foregroundColor(.black)
            }
            .frame(width: 0.50*screenWidth, height: 0.10*screenHeight)
            .background(ComponentColours.submitButton)
            .cornerRadius(8)
            .shadow(radius: 6)
        }
        .onAppear {
            builder = ViewBuilders(screenWidth, screenHeight)
        }
        .padding(.horizontal)
        .alert(isPresented: $showAlert) {
            Alert(title: Text(alertTitle), message: Text(alertMessage), dismissButton: .default(Text("OK"),
                  action: {
                        showAlert = false
                  }
              ))
        }
    }

    /*
     
        Verifies the email
     
     */
    func verifyEmail() {
        let emailValidationRegex = "^[\\p{L}0-9!#$%&'*+\\/=?^_`{|}~-][\\p{L}0-9.!#$%&'*+\\/=?^_`{|}~-]{0,63}@[\\p{L}0-9-]+\\.[a-zA-Z]{2,7}$"
        let emailValidationPredicate = NSPredicate(format: "SELF MATCHES %@", emailValidationRegex)

        //if not valid, set alerts and reset value
        if !(emailValidationPredicate.evaluate(with: emailAddress)) && emailAddress.isEmpty {
            emailAddress = ""
            alertTitle = "Error"
            alertMessage = "Invalid email address format. Please enter again."
            showAlert = true
        } else {
            isValidEmail = true
        }
    }
}
