import SwiftUI

struct AccountCreationBiometrics: View {
    
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    @EnvironmentObject var userData: UserAccount

    @State var screenWidth = 0.0
    @State var screenHeight = 0.0
    
    @State var sex = "--"
    
    @State var birthdate = Date()
    
    @State var weight = ""
    @State var weightUnit = "lb"
    
    @State private var height = 0.0
    @State private var heightFeet = ""
    @State private var heightInches = ""
    @State private var heightFractionOfInch = "--"
    @State private var heightCm = ""
    @State private var heightUnit = "in"
    
    @State var wristCircumference = ""
    @State var wristCircumferenceUnit = "in"
    
    @State var ethnicity = "--"
    
    //Control the next button dependening on fields being filled out
    var isNextButtonEnabled: Bool {
        let calendar = Calendar.current
        let currentDateComponents = calendar.dateComponents([.year, .month, .day], from: Date())
        let birthdateComponents = calendar.dateComponents([.year, .month, .day], from: birthdate)

        let commonData = !(sex == "--")
                && (birthdateComponents != currentDateComponents)
                && !weight.isEmpty
                && !wristCircumference.isEmpty
                && !(ethnicity == "--")

        if heightUnit == "in" {
            return commonData
                && !heightFeet.isEmpty
                && !heightInches.isEmpty
        } else {
            return commonData && !heightCm.isEmpty
        }
    }
    
    //Control alerts
    @State private var isAlertPresented = false
    @State private var alertTitle = ""
    @State private var alertMessage = ""
    
    //Allow access to reusable views
    @State var builder = ViewBuilders(0.0, 0.0)

    var body: some View {
        ZStack(alignment: .top) {
            Color("background").ignoresSafeArea()

            VStack(spacing: 0.02*screenHeight) {
                Spacer()
                title()
                Spacer()
                sexSelector()
                birthdateSelector()
                heightInputAndSelector()
                weightInputAndSelector()
                wristCircumferenceInputAndSelector()
                ethnicitySelector()
                Spacer()
                nextButton()
            }
            .padding(.horizontal)
        }
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
            builder = ViewBuilders(screenWidth, screenHeight)
            UISegmentedControl.appearance().selectedSegmentTintColor = .orange
            UISegmentedControl.appearance().setTitleTextAttributes([.foregroundColor: UIColor.white], for: .normal)
        }
    }
    
    /*
     
     
     
     */
    func title() -> some View {
        Text("Tell us about yourself")
            .modifier(CustomTextLabelStyle(0.05*screenWidth))
    }
    
    /*
     
     
     
     */
    func sexSelector() -> some View {
        VStack(alignment: .leading, spacing: builder.labelSpacing) {
            HStack {
                Text("Biological Sex")
                    .modifier(CustomTextLabelStyle(0.045*screenWidth, true))
                Spacer()
            }

            Picker("", selection: $sex) {
                ForEach(builder.sexValues, id: \.self) { sexValue in
                    Text(sexValue)
                }
            }
            .pickerStyle(SegmentedPickerStyle())
            .background(Color("pickersSelectors"))
            .frame(width: 0.45*screenWidth, height: 0.04*screenHeight)
            .border(.black, width: 0.5)
            .cornerRadius(10)
            .shadow(radius: 6)
        }
    }
    
    /*
     
     
     
     */
    func birthdateSelector() -> some View {
        VStack(alignment: .leading, spacing: builder.labelSpacing) {
            HStack {
                Text("Birthdate")
                    .modifier(CustomTextLabelStyle(0.045*screenWidth, true))
                Spacer()
            }
            
            DatePicker("", selection: $birthdate, displayedComponents: [.date])
                .labelsHidden()
                .frame(width: 0.4*screenWidth, height: 0.05*screenHeight)
                .colorScheme(.dark)
                .brightness(-0.03)
                .background {
                    RoundedRectangle(cornerRadius: 10)
                        .border(.black, width: 0.5)
                        .cornerRadius(10)
                        .foregroundColor(Color("pickersSelectors"))
                        .frame(width: 0.45*screenWidth, height: 0.05*screenHeight)
                        .shadow(radius: 6)
                }
                .padding(.leading, 0.028*screenWidth)
        }
    }
    
    /*
     
     
     
     */
    func heightInputAndSelector() -> some View {
        HStack {
            if heightUnit == "in" {
                builder.createHeightFieldsInches(builder.fractionsOfInchValues, $heightFeet, $heightInches, $heightFractionOfInch)
                    .keyboardType(.numberPad)
            } else {
                builder.createSmallTextFieldWithLabel("Height", heightUnit, $heightCm)
                    .keyboardType(.numberPad)
            }
            
            builder.createUnitSelector(builder.inchesAndCm, $heightUnit)
                .padding(.top, 0.045*screenHeight)
        }
    }
    
    /*
     
     
     
     */
    func weightInputAndSelector() -> some View {
        HStack {
            builder.createSmallTextFieldWithLabel("Weight", weightUnit, $weight)
                .keyboardType(.numberPad)
            
            builder.createUnitSelector(builder.weightUnitValues, $weightUnit)
                .padding(.top, 0.045*screenHeight)
        }
    }
    
    /*
     
     
     
     */
    func wristCircumferenceInputAndSelector() -> some View {
        HStack {
            builder.createSmallTextFieldWithLabel("Wrist Circumference", wristCircumferenceUnit, $wristCircumference)
                .keyboardType(.numberPad)
            
            builder.createUnitSelector(builder.inchesAndCm, $wristCircumferenceUnit)
                .padding(.top, 0.045*screenHeight)
        }
    }
    
    /*
     
     
     
     */
    func ethnicitySelector() -> some View {
        VStack(alignment: .leading, spacing: builder.labelSpacing) {
            HStack {
                Text("Biological Ethnicity")
                    .modifier(CustomTextLabelStyle(0.045*screenWidth, true))
                Spacer()
            }

            Picker("", selection: $ethnicity) {
                ForEach(builder.ethnicityValues, id: \.self) { ethnicity in
                    Text(ethnicity).tag(ethnicity)
                }
            }
            .frame(width: 0.4*screenWidth, height: 0.05*screenHeight)
            .accentColor(.white)
            .background {
                RoundedRectangle(cornerRadius: 10)
                    .border(.black, width: 0.5)
                    .cornerRadius(10)
                    .foregroundColor(Color("pickersSelectors"))
                    .frame(width: 0.45*screenWidth, height: 0.05*screenHeight)
                    .shadow(radius: 6)
            }
            .padding(.leading, 0.028*screenWidth)
        }
    }
    
    /*



    */
    func nextButton() -> some View {
        builder.createNextButton({ AccountCreationActivityLevel().environmentObject(userData) })
            .disabled(!isNextButtonEnabled)
            .simultaneousGesture(TapGesture().onEnded{
                if !isNextButtonEnabled {
                    handleDisabledNextClick()
                } else {
                    updateUserData()
                }
            })
            .alert(isPresented: $isAlertPresented) {
                Alert(title: Text(alertTitle), message: Text(alertMessage), dismissButton: .default(Text("OK")))
            }
    }
        
    /*
        Called to handle the logic for clicking the disabled next button. Sets alert data and displays it.
     */
    func handleDisabledNextClick() {
        self.alertTitle = "Error"
        self.alertMessage = "Missing data. Please fill out all fields."
        isAlertPresented.toggle()
    }
    
    /*
        Called upon selecting the next button. Updates the user model to continue account creation.
     */
    func updateUserData() {
        userData.sex = sex
        userData.birthdate = birthdate
        userData.height = convertHeight()
        userData.heightUnit = heightUnit
        userData.weight = (weight as NSString).doubleValue
        userData.weightUnit = weightUnit
        userData.wristCircumference = (wristCircumference as NSString).doubleValue
        userData.wristCircumferenceUnit = wristCircumferenceUnit
        userData.ethnicity = ethnicity
    }

    /*
        Converts the string value of height into a double for storing in the database. If unit is inches, calculate total number of inches.
     */
    func convertHeight() -> Double {
        if self.heightUnit == "cm" {
            return (self.heightCm as NSString).doubleValue
        } else {
            return ((self.heightFeet as NSString).doubleValue*12) + (self.heightInches as NSString).doubleValue + convertFractionsOfInch()
        }
    }
    
    /*
        Converts the string value of height inch fraction into a double for storing in the database.
     */
    func convertFractionsOfInch() -> Double {
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
    
}
