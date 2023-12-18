import SwiftUI
import Parse

struct SettingHealthConcern: View {
    
    @Environment(\.colorScheme) var colorScheme
    
    @EnvironmentObject private var backendUtilities: BackendUtilities
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0
    
    @State private var selectedhealthConcerns: [Int] = []
    @State private var healthConcernValues: [String] = []
    
    @State private var isAlertPresented = false
    @State var selectedOption: Int? = nil
    
    @State var shouldReload = false
    
    @State var builder = ViewBuilders(0.0, 0.0)
    
    var isNextButtonEnabled: Bool {
        return !selectedhealthConcerns.isEmpty
    }
    
    var body: some View {
        ZStack(alignment: .top) {
            Color("background").ignoresSafeArea()

            VStack {
                Text("Edit your health concerns here")
                    .modifier(CustomTextLabelStyle(0.05*screenWidth))
                    .padding(.vertical, 0.05*screenHeight)

                Text("Select all that apply")
                    .foregroundColor(.orange)
                    .modifier(CustomTextLabelStyle(0.045*screenWidth))
                    .padding(.vertical)

                //Diet preferecne options
                ScrollView {
                    VStack {
                        builder.createPreferenceOptionList(healthConcernValues, $selectedhealthConcerns, screenWidth)
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
                initializeHealthConcernValues()
                shouldReload = false
            }
        }
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
            builder = ViewBuilders(screenWidth, screenHeight)
            initializeHealthConcernValues()
        }
    }
    
    /*
     
        Creates the save button updates user's health concerns with the selcted values from the list.
     
    */
    func saveButton() -> some View {
        var tempArray: [String] = []
        
        for index in selectedhealthConcerns {
            tempArray.append(healthConcernValues[index])
        }
        
        return
            Button("Save", action: {
                backendUtilities.updateUserHealthConcerns(tempArray) { success in
                    if success {
                        print("Successfully saved \(tempArray) to user's health concerns.\n")
                        shouldReload = true
                    } else {
                        print("Unsuccessfully saved \(tempArray) to user's health concerns.\n")
                    }
                }
            })
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
                initializeSelectedHealthConcernValues()
            }
        }
    }
    
    func initializeSelectedHealthConcernValues() {
        for concern in backendUtilities.getUserHealthConcerns() {
            let healthConcern = concern as? String ?? ""
            
            if let index = healthConcernValues.firstIndex(of: healthConcern) {
                selectedhealthConcerns.append(index)
            }
        }
    }
}
