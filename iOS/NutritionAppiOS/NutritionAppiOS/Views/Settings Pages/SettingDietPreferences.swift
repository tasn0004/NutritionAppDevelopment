import SwiftUI
import Parse

struct SettingDietPreferences: View {
    
    @Environment(\.colorScheme) var colorScheme
    
    @EnvironmentObject private var backendUtilities: BackendUtilities
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0
    
    @State private var selectedDietPreferences: [Int] = []
    @State private var dietPreferenceValues: [String] = []
    
    @State private var isAlertPresented = false
    @State var selectedOption: Int? = nil
    
    @State var shouldReload = false
    
    @State var builder = ViewBuilders(0.0, 0.0)
    
    var isNextButtonEnabled: Bool {
        return !selectedDietPreferences.isEmpty
    }
    
    var body: some View {
        ZStack(alignment: .top) {
            Color("background").ignoresSafeArea()

            VStack {
                Text("Edit your diet preferences here")
                    .modifier(CustomTextLabelStyle(0.05*screenWidth))
                    .padding(.vertical, 0.05*screenHeight)

                Text("Select all that apply")
                    .foregroundColor(.orange)
                    .modifier(CustomTextLabelStyle(0.045*screenWidth))
                    .padding(.vertical)

                
                //Diet preferecne options
                ScrollView {
                    VStack {
                        builder.createPreferenceOptionList(dietPreferenceValues, $selectedDietPreferences, screenWidth)
                    }
                    .modifier(CustomTextLabelStyle(0.04*screenWidth))
                    .padding(.horizontal)
                    .padding(.vertical, 0.02*screenWidth)
                }
                .frame(width: 0.85*screenWidth, height: 0.50*screenHeight)
                .clipped()
                .background {
                    RoundedRectangle(cornerRadius: 10)
                        .foregroundColor(Color(.darkGray))
                }
            }
            .padding(.horizontal)
        }
        .navigationBarItems(trailing: saveButton())
        .onChange(of: shouldReload) { _ in
            if shouldReload {
                initializeDietPreferenceValues()
                shouldReload = false
            }
        }
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
            initializeDietPreferenceValues()
        }
    }
    
    /*
     
        Creates the save button updates user's diet preferecnes with the selcted values from the list.
     
    */
    func saveButton() -> some View {
        var tempArray: [String] = []
        
        for index in selectedDietPreferences {
            tempArray.append(dietPreferenceValues[index])
        }
        
        return
            Button("Save", action: {
                backendUtilities.updateUserDietPreferences(tempArray) { success in
                    if success {
                        print("Successfully saved \(tempArray) to user's diet preferences.\n")
                        shouldReload = true
                    } else {
                        print("Unsuccessfully saved \(tempArray) to user's diet preferences.\n")
                    }
                }
            })
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
                initializeSelectedDietPreferenceValues()
            }
        }
    }
    
    func initializeSelectedDietPreferenceValues() {
        for preference in backendUtilities.getUserDietPreferences() {
            let dietPreference = preference as? String ?? ""
            
            if let index = dietPreferenceValues.firstIndex(of: dietPreference) {
                selectedDietPreferences.append(index)
            }
        }
    }
}
