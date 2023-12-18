import SwiftUI

struct AccountCreationActivityLevel: View {
    
    @Environment(\.colorScheme) var colorScheme
    
    @EnvironmentObject var userData: UserAccount
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0
    
    @State var activityLevel = 0.0

    @State var selectedOption: Int? = nil
    
    @State private var isAlertPresented = false
    @State private var alertTitle = ""
    @State private var alertMessage = ""

    var isNextButtonEnabled: Bool {
        return selectedOption != nil
    }
    
    @State var builder = ViewBuilders(0.0, 0.0)
    
    /*
     
     
     
     */
    var body: some View {
        ZStack(alignment: .top) {
            Color("background").ignoresSafeArea()

            VStack {
                title()
                activityLevelList()
                
                    .modifier(CustomTextLabelStyle(0.045 * screenWidth))
                Spacer()
                
                nextButton()
                
            }
            .padding(.horizontal)
        }
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
            builder = ViewBuilders(screenWidth, screenHeight)
        }
    }
    
    func title() -> some View {
        Text("Tell us about your activity level")
            .modifier(CustomTextLabelStyle(0.05*screenWidth))
            .padding(.vertical, 0.05*screenHeight)
    }
    
    func activityLevelList() -> some View {
        VStack(alignment: .leading, spacing: 0.01 * screenHeight) {
            ForEach(0..<builder.activityOptions.count, id: \.self) { index in
                createActivityLevelOption(
                    builder.activityOptions[index].name,
                    builder.activityOptions[index].factor,
                    builder.activityOptions[index].description,
                    isSelected: selectedOption == index,
                    toggle: {
                        if selectedOption == index {
                            selectedOption = nil
                        } else {
                            selectedOption = index
                        }
                    }
                )
            }
        }
        .padding()
        .background(.white)
        .foregroundColor(.black)
        .cornerRadius(10)
        .background {
            RoundedRectangle(cornerRadius: 10)
                .stroke(.black, lineWidth: 0.005*screenWidth)
                .shadow(radius: 6)
        }
    }
    
    func nextButton() -> some View {
        builder.createNextButton({ AccountCreationDietPreferences().environmentObject(userData) })
            .disabled(!isNextButtonEnabled)
            .simultaneousGesture(TapGesture().onEnded{
                if !isNextButtonEnabled {
                    handleDisabledNextClick()
                } else {
                    updateUserData()
                }
            })
            .alert(isPresented: $isAlertPresented) {
                Alert(title: Text(self.alertTitle), message: Text(self.alertMessage), dismissButton: .default(Text("OK")))
            }
    }
    
    /*
     
        
     
     */
    func handleDisabledNextClick() {
        self.alertTitle = "Error"
        self.alertMessage = "Please select an activity level."
        isAlertPresented.toggle()
    }
    
    /*
     
        
     
     */
    func updateUserData() {
        userData.activityLevel = self.activityLevel
    }
    
    /*
     
        
     
     */
    func createActivityLevelOption(_ levelName: String, _ levelFactor: Double, _ levelDescription: String, isSelected: Bool, toggle: @escaping () -> Void) -> some View {
        VStack(alignment: .leading, spacing: 0.01 * screenHeight) {
            // Label
            HStack {
                Text(levelName)
                Spacer()
            }

            // Toggle + description
            HStack {
                Toggle(isOn: Binding(
                    get: { isSelected },
                    set: { _ in toggle() }
                )) {
                }
                .toggleStyle(iOSCheckboxToggleStyle())
                .foregroundColor(.black)
                .onChange(of: isSelected) { newValue in
                    if newValue {
                        self.activityLevel = levelFactor
                    } else {
                        self.activityLevel = 0.0
                    }
                }

                Text(levelDescription)
            }
        }
        .padding(.bottom)
    }
}



