import SwiftUI
import Parse

class UserMealPlan {
    
    @EnvironmentObject private var backendUtilities: BackendUtilities
    
    var mealPlanDictionary: [String: [String: [String]]] = [:]
    
    private var userPreferredStartDay: String?
   
    //Format for dictionary keys
    let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        return formatter
    }()

    /*
     
        Builds a meal plan by finding the date of the last instance of the users preferred start day, and adding 7 consecutive days of empty meal type collections as values to the date keys.
     
     */
    init(_ userPreferredStartDay: String) {
        self.userPreferredStartDay = userPreferredStartDay
        
        var currentDate = calculateFirstDayOfWeek()
        var formattedDate = ""
        
        //Set date components to increment day by 1
        dateFormatter.dateFormat = "MMMM d, yyyy"
        let calendar = Calendar.current
        var dateComponents = DateComponents()
        dateComponents.day = 1
        
        //loop through all 7 days of meal plans starting from user preferred start day
        for _ in 0..<7 {
            formattedDate = dateFormatter.string(from: currentDate)
            
            mealPlanDictionary[formattedDate] = [
                                    "breakfast": [],
                                    "lunch": [],
                                    "dinner": [],
                                    "snacks": [],
                                    "desserts": []
                                ]

            //add 1 day to the current date to get the next day for key
            if let newDate = calendar.date(byAdding: dateComponents, to: currentDate) {
                currentDate = newDate
            }
        }
    }

    /*
     
        Returns the date of the first day of week determined by the users preferred start day.
     
     */
    func calculateFirstDayOfWeek() -> Date {
        var currentDate = Date()
        dateFormatter.dateFormat = "E"
        
        //get day of current date
        var currentDayString = dateFormatter.string(from: currentDate)

        //if the current day is already the first day of the week, return current date
        if userPreferredStartDay == currentDayString {
            return currentDate
        }
        
        //Set date components to decrement day by 1
        let calendar = Calendar.current
        var dateComponents = DateComponents()
        dateComponents.day = -1
        
        //loop backwards until we are at the date of the last userPreferredStartDay
        while(true) {
            //find the new date and set as value for selectedDate
            if let newDate = calendar.date(byAdding: dateComponents, to: currentDate) {
                currentDate = newDate
                
                currentDayString = dateFormatter.string(from: currentDate)
                
                if userPreferredStartDay == currentDayString {
                    return currentDate
                }
            }
        }
    }
}

