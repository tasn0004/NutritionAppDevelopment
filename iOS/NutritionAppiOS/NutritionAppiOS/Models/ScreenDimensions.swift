import Foundation

class ScreenDimensions: ObservableObject {
    
    @Published var _screenWidth = 0.0
    @Published var _screenHeight = 0.0
    
    var screenWidth: Double {
        get {
            return _screenWidth
        }
        set {
            _screenWidth = newValue
        }
    }
    
    var screenHeight: Double {
        get {
            return _screenHeight
        }
        set {
            _screenHeight = newValue
        }
    }
    
    /*
     
        Set the value of the parameters passed in as the values of _screenWidth & _screenHeight
     
     */
    func setScreenDimensions(_ screenWidth: inout Double, _ screenHeight: inout Double) {
        screenWidth = _screenWidth
        screenHeight = _screenHeight
    }
}
