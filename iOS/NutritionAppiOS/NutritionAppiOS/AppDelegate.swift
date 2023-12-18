import Foundation
import UIKit
import Parse

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        
        let configuration = ParseClientConfiguration {
            $0.applicationId = "e6Ll6TU4kwmISih6oezEZOsmnm32eGOhNKu7Z7VK"
            $0.clientKey = "s440pyEcg7W9EOxVtMlFDzdARPWYD9Jw5MShBiQd"
            $0.server = "https://parseapi.back4app.com"
        }
        Parse.initialize(with: configuration)
        
        return true
    }
}
