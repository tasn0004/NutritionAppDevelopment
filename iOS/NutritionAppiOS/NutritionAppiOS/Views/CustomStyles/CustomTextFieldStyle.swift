import SwiftUI

struct CustomTextFieldStyle: TextFieldStyle {

    var screenWidth: Double
    var screenHeight: Double
    
    init(_ screenWidth: Double, _ screenHeight: Double) {
        self.screenWidth = screenWidth
        self.screenHeight = screenHeight
    }
    
    func _body(configuration: TextField<Self._Label>) -> some View {
        
        configuration
            .font(.system(size: 0.05*screenWidth, weight: .light, design: .rounded))
            .frame(width: 0.83*screenWidth, height: 0.01*screenHeight)
            .foregroundColor(.black)
            .padding()
            .background(ComponentColours.dataFieldsSecondary)
            .cornerRadius(10)
            .disableAutocorrection(true)
            .autocapitalization(.none)
    }
}

struct CustomTextFieldStyleBiometrics: TextFieldStyle {

    var screenWidth: Double
    var screenHeight: Double
    
    init(_ screenWidth: Double, _ screenHeight: Double) {
        self.screenWidth = screenWidth
        self.screenHeight = screenHeight
    }
    
    func _body(configuration: TextField<Self._Label>) -> some View {
        
        configuration
            .font(.system(size: 0.05*screenWidth, weight: .light, design: .rounded))
            .frame(width: 0.10*screenWidth, height: 0.01*screenHeight)
            .foregroundColor(.black)
            .padding()
            .background(ComponentColours.dataFieldsSecondary)
            .cornerRadius(10)
            .disableAutocorrection(true)
            .autocapitalization(.none)
    }
}

struct CustomTextFieldStyleMyAccount: TextFieldStyle {

    var screenWidth: Double
    var screenHeight: Double
    
    init(_ screenWidth: Double, _ screenHeight: Double) {
        self.screenWidth = screenWidth
        self.screenHeight = screenHeight
    }
    
    func _body(configuration: TextField<Self._Label>) -> some View {
        
        configuration
            .font(.system(size: 0.04*screenWidth, weight: .light, design: .rounded))
            .frame(width: 0.11*screenWidth, height: 0.01*screenHeight)
            .foregroundColor(.black)
            .padding()
            .background(ComponentColours.dataFieldsSecondary)
            .cornerRadius(10)
            .disableAutocorrection(true)
            .autocapitalization(.none)
    }
}
