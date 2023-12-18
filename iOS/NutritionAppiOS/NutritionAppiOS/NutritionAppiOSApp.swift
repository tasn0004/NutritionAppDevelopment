import SwiftUI

@main
struct NutritionAppiOSApp: App {
    
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    
    @ObservedObject var mealPlanData = MealPlanObservable()
    @ObservedObject var homePageData = HomePageObservable()
    @StateObject var screenDimensions = ScreenDimensions()
    @StateObject var backendUtilities = BackendUtilities()

    @AppStorage("isDarkModeEnabled") var isDarkModeEnabled: Bool = true

    var body: some Scene {
        WindowGroup {
            SplashScreen()
                .environmentObject(mealPlanData)
                .environmentObject(homePageData)
                .environmentObject(screenDimensions)
                .environmentObject(backendUtilities)
                .preferredColorScheme(isDarkModeEnabled ? .dark : .light)
        }
    }
}
