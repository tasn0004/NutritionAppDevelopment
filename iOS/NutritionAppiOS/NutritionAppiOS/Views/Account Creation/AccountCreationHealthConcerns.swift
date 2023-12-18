import SwiftUI
import Parse

struct AccountCreationHealthConcerns: View {
    @EnvironmentObject var userData: UserAccount
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0
    
    //Control alerts
    @State private var isAlertPresented = false
    @State private var alertTitle = ""
    @State private var alertMessage = ""
    
    //Inform if an activity level is selected
    @State var selectedOption: Int? = nil
    
    @State private var healthConcernValues: [String] = []
    @State private var selectedhealthConcerns: [Int] = []
    
    @State private var isLoading = true
    
    //Control the next button dependening on fields being filled out
    var isNextButtonEnabled: Bool {
        return !selectedhealthConcerns.isEmpty
    }
    
    //Allow access to reusable views
    @State var builder = ViewBuilders(0.0, 0.0)

    var body: some View {
        ZStack(alignment: .top) {
            Color("background").ignoresSafeArea()
            
            VStack {
                Text("Tell us about your health concerns")
                    .modifier(CustomTextLabelStyle(0.05*screenWidth))
                    .padding(.vertical, 0.05*screenHeight)

                Text("Select all that apply")
                    .modifier(CustomTextLabelStyle(0.04*screenWidth))
                
                //List area
                ScrollView {
                    VStack {
                        builder.createPreferenceOptionList(healthConcernValues, $selectedhealthConcerns, screenWidth)
                    }
                    .modifier(CustomTextLabelStyle(0.04*screenWidth))
                }
                .frame(width: 0.85*screenWidth, height: 0.50*screenHeight)
                .padding()
                .background(.white)
                .foregroundColor(.black)
                .cornerRadius(10)
                .background {
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(.black, lineWidth: 0.005*screenWidth)
                        .shadow(radius: 6)
                }
                
                Spacer()
                nextButton()
            }
            .padding(.horizontal)
        }
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
            builder = ViewBuilders(screenWidth, screenHeight)
            initializeHealthConcernValues()
        }
    }
    
    func nextButton() -> some View {
        builder.createNextButton({ AccountCreationFinal().environmentObject(userData) })
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
        Called to handle the logic for clicking the disabled next button. Sets alert data and displays it.
     */
    func handleDisabledNextClick() {
        self.alertTitle = "Error"
        self.alertMessage = "Please select from the list of health concerns. If you have none, select 'None'"
        isAlertPresented.toggle()
    }
    
    func updateUserData() {
        var selectedIndex: Int
        
        for i in 0...selectedhealthConcerns.count - 1 {
            selectedIndex = selectedhealthConcerns[i]
            userData.healthConcerns.append(healthConcernValues[selectedIndex])
        }
    }
    
    func initializeHealthConcernValues() {
        let query = PFQuery(className: "NutritionProfiles")
        
        query.whereKey("type", equalTo: "healthConcern")
        query.findObjectsInBackground { (objects, error) in
            if let error = error {
                print("Error fetching recipe: \(error.localizedDescription)\n")
                
            } else if let healthConcerns = objects {
                healthConcernValues = []
                healthConcernValues.append("None")
                
                for concern in healthConcerns {
                    healthConcernValues.append(concern["name"] as? String ?? "")
                }
                
                isLoading = false
            }
        }
    }
}
