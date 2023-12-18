import SwiftUI

// Custom ViewModifier to style the Text
struct CustomTextLabelStyle: ViewModifier {
    private var fontSize: Double
    private var isBold: Bool
    
    init(_ fontSize: Double) {
        self.fontSize = fontSize
        isBold = false
    }
    
    init(_ fontSize: Double, _ isBold: Bool) {
        self.fontSize = fontSize
        self.isBold = isBold
    }
    
    func body(content: Content) -> some View {
        content
            .font(.system(size: fontSize, 
                          weight: isBold ? .bold : .medium,
                          design: .rounded)
            )
    }
}
