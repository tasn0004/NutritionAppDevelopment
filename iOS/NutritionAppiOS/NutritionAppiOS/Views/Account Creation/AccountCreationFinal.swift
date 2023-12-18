import SwiftUI
import Parse

struct AccountCreationFinal: View {

    //Access the colour scheme from the environment
    @Environment(\.colorScheme) var colorScheme
    
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0
    
    //State to manage the loading indicator for sign-up.
    @State private var isLoadingSignup: Bool = false
    
    //Control alerts
    @State private var isAlertPresented = false
    @State private var alertTitle = ""
    @State private var alertMessage = ""
    
    //hold selected values from pickers
    @State var weightManagementGoal = "--"
    @State var preferredStartDayOfWeek = "--"
    
    //Control navigation to the main app view
    @State var showLogin = false
    
    @State private var selectedDate = Date()
    
    //Control the all values being selected
    var isFinishButtonEnabled: Bool {
        return !(weightManagementGoal == "--") && !(preferredStartDayOfWeek == "--")
    }
    
    //Allow access to reusable views
    @State var builder = ViewBuilders(0.0, 0.0)
    
    //Reference the User data model
    @EnvironmentObject var userData: UserAccount
    
    init() {
        
    }
    
    var body: some View {
        ZStack {
            Color("background").ignoresSafeArea()

            /*
                Main Vstack
             */
            VStack(spacing: 0) {
                /*
                    Page header label
                 */
                VStack() {
                    Text("Almost there!")
                        .padding(.vertical, 0.05*screenHeight)
                }
                .modifier(CustomTextLabelStyle(0.05*screenWidth))
                
                Spacer()
                
                /*
                    Weight management goal
                 */
                VStack(alignment: .leading, spacing: 0.01*screenHeight) {
                    HStack {
                        Text("Select your weight management goal")
                            .modifier(CustomTextLabelStyle(0.05*screenWidth))
                            
                       Spacer()
                    }
                    
                    Picker("", selection: $weightManagementGoal) {
                        ForEach(builder.weightManagementGoalValues, id: \.self) { goal in
                            Text(goal)
                        }
                    }
                    .pickerStyle(SegmentedPickerStyle())
                    .background(colorScheme == .dark ? Color(.darkGray) : Color(.lightGray))
                    .cornerRadius(8)
                }
                
                Spacer()
                
                /*
                    Start of week preference
                 */
                VStack(alignment: .leading, spacing: 0.01*screenHeight) {
                    HStack {
                        Text("What day of the week should your meal plan start?")
                            .modifier(CustomTextLabelStyle(0.05*screenWidth))
                            
                       Spacer()
                    }
                    
                    /*
                        Date selector
                     */
                    SevenDayDateSelector($selectedDate, $preferredStartDayOfWeek)
                }

                //push all components to the top of the screen
                Spacer()
                Spacer()
                
                finishButton()
                
                //Navigate to main app view
                NavigationLink(destination: Login().navigationBarBackButtonHidden(true), isActive: $showLogin) { EmptyView() }


            }
            .padding(.horizontal)
        }
        .onAppear {
            UISegmentedControl.appearance().selectedSegmentTintColor = .orange
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
            builder = ViewBuilders(screenWidth, screenHeight)
        }
    }
    
    func finishButton() -> some View {
        Button(action: {
            createUser()
        }) {
            Text("Finish")
                .foregroundColor(.black)
        }
        .disabled(!isFinishButtonEnabled)
        .onTapGesture {
            if !isFinishButtonEnabled {
                handleDisabledFinishClick()
            }
        }
        .alert(isPresented: $isAlertPresented) {
            Alert(title: Text(self.alertTitle), message: Text(self.alertMessage), dismissButton: .default(Text("OK")))
        }
        .frame(width: 0.50*screenWidth, height: 0.10*screenHeight)
        .background(ComponentColours.submitButton)
        .border(Color.black, width: 0.5)
        .cornerRadius(8)
        .padding(0.1*screenWidth)
    }
    
    /*
        Called to handle the logic for clicking the disabled next button. Sets alert data and displays it.
     */
    func handleDisabledFinishClick() {
        self.alertTitle = "Error"
        self.alertMessage = "Missing data. Please select an option for each question."
        isAlertPresented.toggle()
    }
    
    func createUser() {
        
        userData.weightManagementGoal = self.weightManagementGoal
        userData.preferredStartDayOfWeek = self.preferredStartDayOfWeek
        
        userData.signUp()
        
        //if successfully created user
        showLogin = true
    }
}
