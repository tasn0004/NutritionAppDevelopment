import SwiftUI


struct iOSCheckboxToggleStyle: ToggleStyle {
    
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0

    func makeBody(configuration: Configuration) -> some View {
        Button(action: {
            configuration.isOn.toggle()

        }, label: {
            HStack {
                Image(systemName: configuration.isOn ? "checkmark.square" : "square")
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: 0.05*screenWidth)
                
                configuration.label
            }
        })
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
        }
    }
}

struct iOSCheckboxToggleStyleGroceryList: ToggleStyle {
    
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0

    func makeBody(configuration: Configuration) -> some View {
        Button(action: {
            configuration.isOn.toggle()
        }, label: {
            HStack {
                Image(systemName: configuration.isOn ? "checkmark.square" : "square")
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: 0.06*screenWidth)
                
                configuration.label
                    .foregroundColor(configuration.isOn ? .gray : .black)
            }
        })
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
        }
    }
}
