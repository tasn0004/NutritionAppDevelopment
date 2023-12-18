import SwiftUI

struct SevenDayDateSelector: View {
    
    @Environment(\.colorScheme) var colorScheme

    @EnvironmentObject private var backendUtilities: BackendUtilities
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0
    
    @State var currentDate = Date()
    @State var currentDateIndex = 0
    
    @Binding var selectedDate: Date
    @Binding var selectedDay: String
    
    @State var displayedDate = ""

    @State var daysOfWeekValues: [String] = []

    init(_ selectedDate: Binding<Date>, _ selectedDay: Binding<String>) {
        self._selectedDate = selectedDate
        self._selectedDay = selectedDay
    }
    
    /*
     
        View body
     
     */
    var body: some View {
        VStack {
            //Selector
            Picker("", selection: $selectedDay) {
                ForEach(daysOfWeekValues, id: \.self) { day in
                    Text(day)
                        .tag(day)
                }
            }
            .pickerStyle(SegmentedPickerStyle())
            .background(colorScheme == .dark ? Color(.darkGray) : Color(.lightGray))
            .cornerRadius(10)

            //Date text under the selector
            Text(displayedDate)
                .foregroundColor(colorScheme == .dark ? .white : .black)
                .modifier(CustomTextLabelStyle(0.04*screenWidth, true))
        }
        .padding(.horizontal)
        .onAppear() {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
            initializeDayOfWeekValues()
            setToCurrentDay($selectedDay)
            displayedDate = calculateSelectedDate()
        }
        .onChange(of: selectedDay) { newDate in
            displayedDate = calculateSelectedDate()
        }
    }
    
    /*
     
        Calculates the date from the selected day on the date selector.
     
     */
    func calculateSelectedDate() -> String {
        let dateFormatter = DateFormatter()
        
        //get day of current day
        dateFormatter.dateFormat = "E"
        let currentDayString = dateFormatter.string(from: currentDate)
        
        //if day selected is today, return the formatted currentDaySTring
        if currentDayString == selectedDay {
            selectedDate = currentDate
            dateFormatter.dateFormat = "EEEE, MMMM d"
            return  dateFormatter.string(from: selectedDate)
            
        } else {
            var indexDifference: Int
            
            //calculate difference in indexes and store resulting date in selected date
            if let selectedDayIndex = daysOfWeekValues.firstIndex(of: selectedDay), let currentDayIndex = daysOfWeekValues.firstIndex(of: currentDayString) {
                indexDifference = selectedDayIndex - currentDayIndex
                
                //subtract the difference in days from current date
                let calendar = Calendar.current
                var dateComponents = DateComponents()
                dateComponents.day = indexDifference
                
                //find the new date and set as value for selectedDate
                if let newDate = calendar.date(byAdding: dateComponents, to: currentDate) {
                    selectedDate = newDate
                }
            }
            //return the formatted date
            dateFormatter.dateFormat = "EEEE, MMMM d"
            return  dateFormatter.string(from: selectedDate)
        }
    }
    
    /*
     
        Initializes the day of week values based off the user preferred start day of week.
     
     */
    func initializeDayOfWeekValues() {
        switch backendUtilities.getUserPreferredStartDay() {
            case "Mon":
                daysOfWeekValues = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]
            case "Tue":
                daysOfWeekValues = ["Tue", "Wed", "Thu", "Fri", "Sat", "Sun", "Mon"]
            case "Wed":
                daysOfWeekValues = ["Wed", "Thu", "Fri", "Sat", "Sun", "Mon", "Tue"]
            case "Thu":
                daysOfWeekValues = ["Thu", "Fri", "Sat", "Sun", "Mon", "Tue", "Wed"]
            case "Fri":
                daysOfWeekValues = ["Fri", "Sat", "Sun", "Mon", "Tue", "Wed", "Thu"]
            case "Sat":
                daysOfWeekValues = ["Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri"]
            case "Sun":
                daysOfWeekValues = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"]
            default:
                daysOfWeekValues = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"]
        }
    }
    
    /*
     
        Sets the selected date on the selector to the current day.
     
     */
    func setToCurrentDay(_ daySelected: Binding<String>) {
        //get day from current date
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "E"
        let currentDay = dateFormatter.string(from: Date())
       
        //sets daySelected initialy to the current day
        switch currentDay {
            case "Mon":
               daySelected.wrappedValue = "Mon"
            case "Tue":
               daySelected.wrappedValue = "Tue"
            case "Wed":
               daySelected.wrappedValue = "Wed"
            case "Thu":
               daySelected.wrappedValue = "Thu"
            case "Fri":
               daySelected.wrappedValue = "Fri"
            case "Sat":
               daySelected.wrappedValue = "Sat"
            case "Sun":
               daySelected.wrappedValue = "Sun"
            default:
               daySelected.wrappedValue = ""
        }
   }
}
