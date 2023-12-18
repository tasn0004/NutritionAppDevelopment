import SwiftUI
import Parse

struct CommentButton: View {
    
    @Environment(\.colorScheme) private var colorScheme
    
    //Environment objects
    @EnvironmentObject private var backendUtilities: BackendUtilities
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State private var screenWidth = 0.0
    @State private var screenHeight = 0.0
    
    @State private var showingComments = false
    
    private var recipeId: String
    @Binding private var numComments: Int
    
    init(_ recipeId: String, _ numComments: Binding<Int>) {
        self.recipeId = recipeId
        self._numComments = numComments
    }
    
    /*

        View body

    */
    var body: some View {
        Button(action: {
            showingComments = true
        }) {
            Image(systemName: "bubble.middle.bottom")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 0.062*screenWidth)
                .foregroundColor(colorScheme == .dark ? .white : .black)
        }
        .popover(isPresented: $showingComments) {
            CommentsPopover(recipeId, $numComments)
                
        }
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
        }
    }
    
 }


