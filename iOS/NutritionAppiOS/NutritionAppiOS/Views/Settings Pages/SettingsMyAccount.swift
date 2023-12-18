import SwiftUI
import Parse

struct SettingsMyAccount: View {
    
    @Environment(\.colorScheme) var colorScheme
    @AppStorage("isDarkModeEnabled") var isDarkModeEnabled: Bool = false
    
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0
    
    @State var sex = ""
    
    @State var weight = ""
    @State var weightLb = ""
    @State var weightKg = ""
    @State var weightUnit = "lb"
    
    @State private var height = ""
    @State private var heightCm = ""
    @State private var heightFeet = ""
    @State private var heightInches = ""
    @State private var heightFractionOfInch = "--"
    @State private var heightUnit = "in"
    
    @State var wristCircumference = ""
    @State var wristCircumferenceIn = ""
    @State var wristCircumferenceCm = ""
    @State var wristCircumferenceUnit = "in"
    
    @State var firstName: String = ""
    @State var lastName: String = ""
    @State var emailAddress: String = ""
    @State var password: String = ""
    @State var birthdate: Date = Date()
    
    //Control alerts
    @State private var isAlertPresented = false
    @State private var alertTitle = ""
    @State private var alertMessage = ""
    
    @State private var isEmailAlertPresented = false
    @FocusState private var isEmailFocused: Bool
    
    @State var currentUser = PFUser.current()

    @State var builder = ViewBuilders(0.0, 0.0)

    var body: some View {
        ZStack {
            Color("background").ignoresSafeArea()
            
            VStack {
                /*
                    Text header
                 */
                HStack {
                    Text("My Account")
                        .modifier(CustomTextLabelStyle(0.1*screenWidth))
                    Spacer()
                }
                .padding(.horizontal)
                
                Form {
                    /*
                        personal info section
                     */
                    Section(header: Text("Personal Information").bold()) {
                        TextField(firstName, text: $firstName)
                        TextField(lastName, text: $lastName)
                        TextField(emailAddress, text: $emailAddress)
                            .focused($isEmailFocused)
                            .onChange(of: isEmailFocused) { isFocused in
                                if self.emailAddress != "" { verifyEmail() }
                            }
                            .alert(isPresented: $isEmailAlertPresented) {
                                Alert(title: Text(self.alertTitle), message: Text(self.alertMessage), dismissButton: .default(Text("OK")))
                            }
                        
                        TextField("", text: $password, prompt: Text("Edit password").foregroundColor(.gray.opacity(0.8)))
                        DatePicker("Edit birth date", selection: $birthdate, displayedComponents: .date)
                    }
                    .accentColor(.black)
                    .autocapitalization(.none)
                    .listRowBackground(Color("tabs"))
                    
                    /*
                        Biometrics section
                     */
                    Section(header:Text("Biometric Information").bold()) {
                       /*
                           Height
                        */
                        HStack {
                           if heightUnit == "in" {
                               builder.createHeightFieldsInches(builder.fractionsOfInchValues, heightFeet, heightInches, $heightFeet, $heightInches, $heightFractionOfInch)
                                   .keyboardType(.numberPad)
                               Spacer()
                           }
                           else {
                               builder.createSmallTextFieldWithLabel("Height", self.heightCm, $heightCm)
                                   .keyboardType(.numberPad)
                           }
                            builder.createUnitSelector(builder.inchesAndCm, $heightUnit)
                                .padding(.top, 0.035*screenHeight)
                        }
                        
                        
                        /*
                           Weight
                        */
                        HStack {
                            builder.createSmallTextFieldWithLabel("Weight", self.weightUnit == "lb" ? weightLb : weightKg, self.weightUnit == "lb" ? $weightLb : $weightKg)
                                .keyboardType(.numberPad)
                           
                            builder.createUnitSelector(builder.weightUnitValues, $weightUnit)
                               .padding(.top, 0.035*screenHeight)
                        }

                        /*
                           Wrist circumference
                        */
                        HStack {
                            builder.createSmallTextFieldWithLabel("Wrist Circumference", self.wristCircumferenceUnit == "in" ? wristCircumferenceIn : wristCircumferenceCm, self.wristCircumferenceUnit == "in" ? $wristCircumferenceIn : $wristCircumferenceCm)
                                .keyboardType(.numberPad)
                           
                            builder.createUnitSelector(builder.inchesAndCm, $wristCircumferenceUnit)
                               .padding(.top, 0.035*screenHeight)
                        }
                    }
                    .autocapitalization(.none)
                    .listRowBackground(Color("tabs"))
                }
                .scrollContentBackground(.hidden)
                .navigationBarItems(trailing: saveButton())

            }//Main VStack
        }//Zstack
        .onAppear {
            startupOperation()
            // Fetch the current user when the view appears
            if let currentUser = PFUser.current() {
                self.currentUser = currentUser
                initializeFieldsFromUser()
            }
        }
    }
    
    /*
     
        Styles the veiw onAppear
     
     */
    func startupOperation() {
        screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
        builder = ViewBuilders(screenWidth, screenHeight)
        UISegmentedControl.appearance().selectedSegmentTintColor = .orange
        UINavigationBar.appearance().largeTitleTextAttributes = [.foregroundColor: UIColor.init(.white)]
    }
    
    /*
       Creates the save button and calls updateCurrentUser() as an action
    */
    func saveButton() -> some View {
        Button("Save", action: {
            updateCurrentUser()
        })
        .alert(isPresented: $isAlertPresented) {
            Alert(title: Text(self.alertTitle), message: Text(self.alertMessage), dismissButton: .default(Text("OK")))
        }
    }
    
    /*
        Converts the string value of height inch fraction into a double for storing in the database.
     */
    func convertFractionsOfInchToDouble() -> Double {
        switch self.heightFractionOfInch {
        case "1/16":
            return 0.0625
            
        case "1/4":
            return 0.25
            
        case "1/2":
            return 0.5
            
        case "3/4":
            return 0.75
            
        default:
            return 0.0
        }
    }
    
    func convertHeight() -> Double {
        if self.heightUnit == "cm" {
            return (self.heightCm as NSString).doubleValue
        } else {
            return ((self.heightFeet as NSString).doubleValue*12) + (self.heightInches as NSString).doubleValue + convertFractionsOfInchToDouble()
        }
    }
    
    func convertDataBeforeSaving() {
        //save final height depending on unit
        if self.heightUnit == "cm" {
            self.height = String(self.heightCm)
        } else {
            self.height = String(convertHeight())
        }
        
        //save final weight depending on unit
        if self.weightUnit == "lb" {
            self.weight = String(self.weightLb)
        } else {
            self.weight = String(self.weightKg)
        }
        
        //save final wrist circumference depending on unit
        if self.wristCircumferenceUnit == "cm" {
            self.wristCircumference = String(self.wristCircumferenceCm)
        } else {
            self.wristCircumference = String(self.wristCircumferenceIn)
        }
    }
    
    /*
       Updates the state of the current user with the field values on the page and saves to the database
    */
    func updateCurrentUser() {
        if let currentUser = PFUser.current() {
            
            currentUser["username"] = self.emailAddress
            currentUser["email"] = self.emailAddress
            
            currentUser["firstName"] = self.firstName
            currentUser["lastName"] = self.lastName
            currentUser["birthdate"] = self.birthdate
            currentUser["height"] = (self.height as NSString).doubleValue
            currentUser["heightUnit"] = self.heightUnit
            currentUser["weight"] = (self.weight as NSString).doubleValue
            currentUser["weightUnit"] = self.weightUnit
            currentUser["wristCircumference"] = (self.wristCircumference as NSString).doubleValue
            currentUser["wristCircumferenceUnit"] = self.wristCircumferenceUnit

            currentUser.saveInBackground { (success, error) in
                if let error = error {
                    print("Unsuccessful user update \(error.localizedDescription)\n")
                    self.alertTitle = "Error"
                    self.alertMessage = "Editing account unsuccessful."
                    self.isAlertPresented.toggle()
                } else if success {
                    print("Successful user update\n")
                    self.alertTitle = "Success!"
                    self.alertMessage = "You successfully updated your account."
                    self.isAlertPresented.toggle()
                }
            }
            
            //reinitialize values with new values to refresh pages field values
            initializeFieldsFromUser()
        }
    }
    
    /*
       Initializses the field values with the current users field values to reflect the state of the current user.
    */
    func initializeFieldsFromUser() {
        if currentUser != nil {
            if let emailAddress = currentUser?.email {
                self.emailAddress = emailAddress
            }

            if let firstName = currentUser?["firstName"] as? String {
                self.firstName = firstName
            }
            
            if let lastName = currentUser?["lastName"] as? String {
                self.lastName = lastName
            }
            
            if let birthdate = currentUser?["birthdate"] as? Date {
                self.birthdate = birthdate
            }
            
            if let sex = currentUser?["sex"] as? String {
                self.sex = sex
            }
            
            if let height = currentUser?["height"] as? Double {
                if self.heightUnit == "in" {
                    //convert to cm and store
                    self.heightCm = String(format: "%.1f", height * 2.54)
                    //pass the total inches
                    convertAndSetHeightInches(height)
                } else {
                    //set as cm
                    self.heightCm = String(format: "%.1f", height)
                    //pass converted cm to inches value
                    convertAndSetHeightInches(height * 0.393701)
                }
            }
            
            if let heightUnit = currentUser?["heightUnit"] as? String {
                self.heightUnit = heightUnit
            }
            
            if let weight = currentUser?["weight"] as? Double {
                if self.weightUnit == "lb" {
                    self.weightLb = String(format: "%.1f", weight)
                    //convertlb to kg
                    self.weightKg = String(format: "%.1f", weight / 2.2)
                } else {
                    //convert kg to lb
                    self.weightLb = String(format: "%.1f", weight * 2.2)
                    self.weightKg = String(format: "%.1f", weight)
                }
            }
            
            if let weightUnit = currentUser?["weightUnit"] as? String {
                self.weightUnit = weightUnit
            }
            
            if let wristCircumference = currentUser?["wristCircumference"] as? Double {
                if self.wristCircumferenceUnit == "in" {
                    //convert to cm and store
                    self.wristCircumferenceCm = String(format: "%.1f", wristCircumference * 2.54)
                    //pass the total inches
                    self.wristCircumferenceIn = String(format: "%.1f", wristCircumference)
                } else {
                    //set as cm
                    self.wristCircumferenceCm = String(format: "%.1f", wristCircumference)
                    //pass converted cm to inches value
                    self.wristCircumferenceIn = String(format: "%.1f", wristCircumference * 0.393701)
                }
            }
            
            if let wristCircumferenceUnit = currentUser?["wristCircumferenceUnit"] as? String {
                self.wristCircumferenceUnit = wristCircumferenceUnit
            }
        }
    }
    
    func convertAndSetHeightInches(_ totalInches: Double) {
        let feet = Int(totalInches / 12)
        let remainingInches = totalInches.truncatingRemainder(dividingBy: 12)
        let wholeInches = Int(remainingInches)
        let fractionOfInch = remainingInches - Double(wholeInches)
        
        self.heightFeet = String(feet)
        self.heightInches = String(Int(remainingInches))
        self.heightFractionOfInch = convertFractionOfInchToString(fractionOfInch)
    }
    
    func convertFractionOfInchToString(_ total: Double) -> String {
        if total > 0 && total <= 0.0625 {
            return "1/16"
            
        } else if total > 0.0625 && total <= 0.25 {
            return "1/4"
            
        } else if total > 0.25 && total <= 0.5 {
            return "1/2"
            
        } else if total > 0.5 && total <= 0.75 {
            return "3/4"
            
        } else {
            return "--"
        }
    }
    
    /*
        Called to verify a valid email address has been entered. Sets alert data and displays it when invalid.
     */
    func verifyEmail() {
        let emailValidationRegex = "^[\\p{L}0-9!#$%&'*+\\/=?^_`{|}~-][\\p{L}0-9.!#$%&'*+\\/=?^_`{|}~-]{0,63}@[\\p{L}0-9-]+\\.[a-zA-Z]{2,7}$"
        let emailValidationPredicate = NSPredicate(format: "SELF MATCHES %@", emailValidationRegex)

        //if not valid, set alerts and reset value
        if !(emailValidationPredicate.evaluate(with: self.emailAddress)) {
            if let emailAddress = currentUser?.email {
                self.emailAddress = emailAddress
            }
            self.alertTitle = "Error"
            self.alertMessage = "Invalid email address format. Please enter again."
            self.isEmailAlertPresented.toggle()
        }
    }
}

struct SettingsMyAccount_Previews: PreviewProvider {
    static var previews: some View {
        SettingsMyAccount()
    }
}
