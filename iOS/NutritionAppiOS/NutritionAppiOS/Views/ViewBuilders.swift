import SwiftUI
import Parse

struct ViewBuilders: View {
    
    @Environment(\.colorScheme) var colorScheme
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    let ageValues = 1...100
    let sexValues = ["Male", "Female"]
    let ethnicityValues = ["--", "Caucasian", "African", "Arab", "Latin", "East Asian", "South Asian"]
    let inchesAndCm = ["in", "cm"]
    let fractionsOfInchValues = ["--", "3/4", "1/2", "1/4", "1/16"]
    let weightUnitValues = ["lb", "kg"]
    
    let activityOptions = [
        (name: "Very low", factor: 1.0, description: "No exercise, and completely sedentary"),
        (name: "Normal", factor: 1.2, description: "No exercise, but not sedentary"),
        (name: "Slightly", factor: 1.3, description: "1-2 hrs/week of exercise or equivalent"),
        (name: "Moderately", factor: 1.5, description: "3-4 hrs/week of exercise or equivalent"),
        (name: "Very", factor: 1.7, description: "5-6 hrs/week of exercise or equivalent"),
        (name: "Extremely", factor: 1.9, description: "7-8+ hrs/week of exercise or vigorous activity")
    ]
    
    @State var dietPreferenceValues: [String] = []
    @State var healthConcernValues: [String] = []
    
    let weightManagementGoalValues = ["Lose", "Maintain", "Gain"]
    
    @State var trackedSelectedDay = ""
    
    var screenWidth: Double = 0.0
    var screenHeight: Double = 0.0
    
    var labelSpacing: Double = 0.0
    
    init(_ screenWidth: Double, _ screenHeight: Double) {
        self.screenWidth = screenWidth
        self.screenHeight = screenHeight
        labelSpacing = 0.01*screenHeight
    }
    
    var body: some View {
        EmptyView()
    }

    /*
        Creates a dual value unit selector with values passed in from an array of 2 units. Stores the value in the varibale passed as storageVariable.
     */
    func createUnitSelector(_ values: [String], _ storageVariable: Binding<String>) -> some View {
        Picker("", selection: storageVariable) {
            ForEach(values, id: \.self) { unit in
                Text(unit)
            }
        }
        .pickerStyle(SegmentedPickerStyle())
        .background(colorScheme == .dark ? Color(.darkGray) : Color(.lightGray))
        .frame(width: 0.20*screenWidth)
        .cornerRadius(8)
    }
    

    
    /*
        Creates a label with 2 text fields and a drop down picker for fractions, when using imperial units for height.
     */
    func createHeightFieldsInches(_ values: [String], _ heightFeetValue: String, _ heightInchesValue: String, _ heightFeet: Binding<String>, _ heightInches: Binding<String>, _ heightFractionOfInch: Binding<String>) -> some View {
        VStack(spacing: labelSpacing) {
            HStack {
                Text("Height")
                Spacer()
            }
            
            HStack(spacing: 0.015*screenWidth) {
                TextField(heightFeetValue, text: heightFeet)
                    .keyboardType(.numberPad)
                
                TextField(heightInchesValue, text: heightInches)
                    .keyboardType(.numberPad)
                
                ZStack {
                    Rectangle()
                        .foregroundColor(ComponentColours.dataFieldsSecondary)
                        .frame(width: 0.15*screenWidth, height: 0.05*screenHeight)
                        .cornerRadius(8)
                    
                    Picker("", selection: heightFractionOfInch) {
                        ForEach(fractionsOfInchValues, id: \.self) { fraction in
                            Text(fraction).tag(fraction)
                        }
                    }
                    .fixedSize()
                    .scaledToFit()
                    .labelsHidden()
                    .scaledToFit()
                }
                Spacer()
            }
        }
    }
    
    /*
        Creates a text field with a label and stores the value into the variable passed in as storageVariable
     */
    func createTextField(_ labelText: String, _ textfieldText: String, _ storageVariable: Binding<String>, _ screenHeight: Double) -> some View {
        VStack(alignment: .leading, spacing: 0.01*screenHeight) {
            HStack() {
                Text(labelText)
                Spacer()
            }
            
            TextField("", text: storageVariable, prompt: Text(textfieldText).foregroundColor(.gray.opacity(0.7)))
        }
    }
    
    /*
        Creates a text field with a label and stores the value into the variable passed in as storageVariable. Added onEditingChanged parameter to allow calling functions after a text field has been edited and lost focus
     */
    func createTextField(_ labelText: String, _ textfieldText: String, _ storageVariable: Binding<String>, _ screenHeight: Double, _ onEditingChanged: @escaping (Bool) -> Void) -> some View {
        VStack(alignment: .leading, spacing: 0.01*screenHeight) {
            HStack() {
                Text(labelText)
                Spacer()
            }
            
            TextField(textfieldText, text: storageVariable)
        }
    }
    
    /*
        Creates the next button used on account creation pages to navigate forward 
     */
    func createNextButton<Destination: View>(@ViewBuilder _ destination: @escaping () -> Destination) -> some View {
        NavigationLink(
            destination: {
                destination()
            },
            label: {
                Spacer()
                Image(systemName: "chevron.right")
                  .resizable()
                  .scaleEffect(0.35, anchor: .center)
                  .foregroundColor(.black)
                  .background(ComponentColours.submitButton)
                  .frame(width: 0.20*screenWidth, height: 0.15*screenHeight)
                  .clipShape(Circle())
                  .shadow(radius: 6)
            }
        )
    }
    
    /*
        Creates a preference option for diet prefs/health concerns
     */
    func createPreferenceOption(_ preferenceItem: String, _ isSelected: Binding<Bool>) -> some View {
        VStack(alignment: .leading) {
            // Label
            HStack {
                // Toggle
                Toggle(isOn: isSelected) {
                }
                .toggleStyle(iOSCheckboxToggleStyle())

                Text(preferenceItem)
                Spacer()
            }
        }
        .padding(.bottom)
    }
    
    /*
        Creates a list of selectable options with square toggles. Pass in the data to display and the array to store selections. When an array value "None" is selected, unselects all options and
        resets the selected array with the index for "None". Used for the diet preferences/health concerns lists.
     */
    func createPreferenceOptionList(_ preferenceValues: [String], _ selectedValuesBinding: Binding<[Int]>, _ screenWidth: Double) -> some View {
        VStack {
            ForEach(0..<preferenceValues.count, id: \.self) { index in
                let isSelected = Binding(
                    get: {
                        selectedValuesBinding.wrappedValue.contains(index)
                    },
                    set: { newValue in
                        var updatedSelectedValues = selectedValuesBinding.wrappedValue
                        if preferenceValues[index] == "None" {
                            if newValue {
                                updatedSelectedValues = [index]
                            }
                        } else {
                            if newValue {
                                // Deselect "None" and select the chosen option
                                if updatedSelectedValues.contains(0) {
                                    updatedSelectedValues.removeAll { $0 == 0 }
                                }
                                updatedSelectedValues.append(index)
                            } else {
                                // Deselect the chosen option
                                updatedSelectedValues.removeAll { $0 == index }
                            }
                        }
                        selectedValuesBinding.wrappedValue = updatedSelectedValues
                    }
                )
                createPreferenceOption(preferenceValues[index], isSelected)
            }
        }
        .modifier(CustomTextLabelStyle(0.045*screenWidth))
    }
    
    
    
    
    
    
    
    /*
        Creates a label with 2 text fields and a drop down picker for fractions, when using imperial units for height.
     */
    func createHeightFieldsInches(_ values: [String], _ heightFeet: Binding<String>, _ heightInches: Binding<String>, _ heightFractionOfInch: Binding<String>) -> some View {
        VStack(alignment: .leading, spacing: labelSpacing) {
            HStack {
                Text("Height")
                    .modifier(CustomTextLabelStyle(0.045*screenWidth, true))
                Spacer()
            }
            
            HStack {
                createSmallTextFieldNoLabel("ft'", heightFeet)
                    .keyboardType(.numberPad)
                
                createSmallTextFieldNoLabel("in'", heightInches)
                    .keyboardType(.numberPad)
                

                Picker("", selection: heightFractionOfInch) {
                    ForEach(fractionsOfInchValues, id: \.self) { fraction in
                        Text(fraction).tag(fraction)
                    }
                }
                .background(.white)
                .cornerRadius(10)
                .accentColor(.black)
                .overlay {
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(.black, lineWidth: 0.0025*screenWidth)
                        .shadow(radius: 6)
                }
                
                Spacer()
            }
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    /*
     
        Creates a text field with a label and stores the value into the variable passed in as storageVariable
     
     */
    func createTextFieldWithLabel(_ labelText: String, _ placeholderText: String, _ bindingString: Binding<String>) -> some View {
        VStack(spacing: labelSpacing) {
            HStack {
                Text(labelText)
                    .modifier(CustomTextLabelStyle(0.045*screenWidth, true))
                Spacer()
            }
            
            TextField("", text: bindingString, prompt: Text(placeholderText).foregroundColor(.gray.opacity(0.7)))
                .modifier(CustomTextLabelStyle(0.045*screenWidth))
                .padding(0.015*screenWidth)
                .background(.white)
                .foregroundColor(.black)
                .cornerRadius(10)
                .background {
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(.black, lineWidth: 0.005*screenWidth)
                        .shadow(radius: 6)
                }
                .disableAutocorrection(true)
                .autocapitalization(.none)
        }
    }
    
    /*
     
        Creates a text field with a label and stores the value into the variable passed in as storageVariable
     
     */
    func createSmallTextFieldWithLabel(_ labelText: String, _ placeholderText: String, _ bindingString: Binding<String>) -> some View {
        VStack(alignment: .leading, spacing: labelSpacing) {
            HStack {
                Text(labelText)
                    .modifier(CustomTextLabelStyle(0.045*screenWidth, true))
                Spacer()
            }
            
            TextField("", text: bindingString, prompt: Text(placeholderText).foregroundColor(.gray.opacity(0.7)))
                .frame(width: 0.12*screenWidth, height: 0.03*screenHeight)
                .modifier(CustomTextLabelStyle(0.045*screenWidth))
                .padding(0.015*screenWidth)
                .background(.white)
                .foregroundColor(.black)
                .cornerRadius(10)
                .background {
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(.black, lineWidth: 0.005*screenWidth)
                        .shadow(radius: 6)
                }
                .disableAutocorrection(true)
                .autocapitalization(.none)
        }
    }
    
    /*
     
        Creates a text field with a label and stores the value into the variable passed in as storageVariable
     
     */
    func createSmallTextFieldNoLabel(_ placeholderText: String, _ bindingString: Binding<String>) -> some View {
        TextField("", text: bindingString, prompt: Text(placeholderText).foregroundColor(.gray.opacity(0.7)))
            .frame(width: 0.12*screenWidth, height: 0.03*screenHeight)
            .modifier(CustomTextLabelStyle(0.045*screenWidth))
            .padding(0.015*screenWidth)
            .background(.white)
            .foregroundColor(.black)
            .cornerRadius(10)
            .background {
                RoundedRectangle(cornerRadius: 10)
                    .stroke(.black, lineWidth: 0.005*screenWidth)
                    .shadow(radius: 6)
            }
            .disableAutocorrection(true)
            .autocapitalization(.none)
    }
    
    /*
     
        Creates a text field with a label and stores the value into the variable passed in as storageVariable
     
     */
    func createPasswordFieldWithLabel(_ labelText: String, _ placeholderText: String, _ bindingString: Binding<String>) -> some View {
        VStack(spacing: labelSpacing) {
            HStack {
                Text(labelText)
                    .modifier(CustomTextLabelStyle(0.045*screenWidth, true))
                Spacer()
            }
            
            SecureField("", text: bindingString, prompt: Text(placeholderText).foregroundColor(.gray.opacity(0.7)))
                .font(.system(size: 0.05*screenWidth, weight: .light, design: .rounded))
                .padding(0.015*screenWidth)
                .background(.white)
                .foregroundColor(.black)
                .cornerRadius(10)
                .background {
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(.black, lineWidth: 0.005*screenWidth)
                        .shadow(radius: 6)
                }
                .disableAutocorrection(true)
                .autocapitalization(.none)
        }
    }
    
    func imageHeader() -> some View {
        Image("NutricooksLogo")
            .resizable()
            .aspectRatio(contentMode: .fit)
            .clipShape(Circle())
            .frame(width: 0.30*screenWidth)
            .shadow(radius: 6)
    }
}

