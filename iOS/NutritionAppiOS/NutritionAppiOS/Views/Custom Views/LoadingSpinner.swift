import SwiftUI

struct LoadingSpinner: View {
    
    @Binding var isLoading: Bool
    
    init(_ isLoading: Binding<Bool>) {
        self._isLoading = isLoading
    }
    
    var body: some View {
        if isLoading {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    .scaleEffect(2.0)
        } else {
            EmptyView()
        }
    }
}
