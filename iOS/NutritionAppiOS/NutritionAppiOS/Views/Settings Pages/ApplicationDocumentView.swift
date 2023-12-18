import SwiftUI
import Parse

struct ApplicationDocumentView: View {

    @Environment(\.colorScheme) private var colorScheme
    
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State var screenWidth = 0.0
    @State var screenHeight = 0.0
    
    @State private var isLoading = true
    
    @State private var documentText = ""
    
    private var pageTitle: String
    private var documentName: String
    
    init(_ pageTitle: String, _ documentName: String) {
        self.pageTitle = pageTitle
        self.documentName = documentName
    }

    /*
     
        View body
        
     */
    var body: some View {
        ZStack {
            Color("background").ignoresSafeArea()
            
            isLoading ? AnyView(LoadingSpinner($isLoading)) : AnyView(textArea())
        }
        .navigationBarTitle(pageTitle, displayMode: .inline)
        .onAppear {
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
            initializeText()
        }
    }
    
    /*
     
        Creates the text area for the documentText.
        
     */
    func textArea() -> some View {
        ScrollView {
            LazyVStack(alignment: .leading) {
                Text(documentText)
                    .modifier(CustomTextLabelStyle(0.04*screenWidth, false))
                    .foregroundColor(colorScheme == .dark ? .white : .black)
                    .lineSpacing(8)

                Spacer()
            }
        }
        .padding()
        .clipped()
    }
    
    /*
     
        Initialize the document text by fetching from the ApplicationDocuments table.
        
     */
    func initializeText() {
        let query = PFQuery(className:"ApplicationDocuments")
        query.whereKey("name", matchesRegex: documentName, modifiers: "i")
        
        query.getFirstObjectInBackground() { (object, error) in
            if let document = object {
                documentText = document["documentText"] as? String ?? ""
                isLoading = false
                return
                
            } else if let error = error {
                print("Error fetching \(documentName.lowercased()) document text: \(error.localizedDescription)\n")
            }
        }
        print("Could not find \(documentName.lowercased()) during fetch.\n")
    }
}

