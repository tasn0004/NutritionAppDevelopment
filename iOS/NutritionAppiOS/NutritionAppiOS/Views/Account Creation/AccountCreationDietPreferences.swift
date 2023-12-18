import SwiftUI
import Parse

struct AccountCreationDietPreferences: View {
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0
    
    //hold selected values from list
    @State private var selectedDietPreferences: [Int] = []
    
    //Control alerts
    @State private var isAlertPresented = false
    @State private var alertTitle = ""
    @State private var alertMessage = ""
    
    //Inform if an activity level is selected
    @State var selectedOption: Int? = nil
    
    @State private var dietPreferenceValues: [String] = []
    
    @State private var isLoading = true
    
    //Control the next button dependening on fields being filled out
    var isNextButtonEnabled: Bool {
        return !selectedDietPreferences.isEmpty
    }
    
    //Allow access to reusable views
    @State var builder = ViewBuilders(0.0, 0.0)
    
    //Initialize the User data model
    @EnvironmentObject var userData: UserAccount
    
    /*
     
        View body
     
     */
    var body: some View {
        ZStack(alignment: .top) {
            Color("background").ignoresSafeArea()

            VStack {
                Text("Tell us about your diet preferences")
                    .modifier(CustomTextLabelStyle(0.05*screenWidth))
                    .padding(.vertical, 0.05*screenHeight)

                Text("Select all that apply")
                    .modifier(CustomTextLabelStyle(0.04*screenWidth))
            

                
                //List area
                ScrollView {
                    VStack {
                        builder.createPreferenceOptionList(dietPreferenceValues, $selectedDietPreferences, screenWidth)
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
            initializeDietPreferenceValues()
        }
    }
    
    func nextButton() -> some View {
        builder.createNextButton({ AccountCreationHealthConcerns().environmentObject(userData)})
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
        self.alertMessage = "Please select from the list of diet preferences. If you have none, select 'None'"
        isAlertPresented = true
    }
    
    func updateUserData() {
        var selectedIndex: Int
        
        for i in 0...selectedDietPreferences.count - 1 {
            selectedIndex = selectedDietPreferences[i]
            userData.dietPreferences.append(dietPreferenceValues[selectedIndex])
        }
    }
    
    func initializeDietPreferenceValues() {
        let query = PFQuery(className: "NutritionProfiles")
        
        query.whereKey("type", equalTo: "dietPreference")
        query.findObjectsInBackground { (objects, error) in
            if let error = error {
                print("Error fetching recipe: \(error.localizedDescription)\n")
                
            } else if let dietPreferences = objects {
                dietPreferenceValues = []
                dietPreferenceValues.append("None")
                
                for preference in dietPreferences {
                    dietPreferenceValues.append(preference["name"] as? String ?? "")
                }
                
                isLoading = false
            }
        }
    }
}

