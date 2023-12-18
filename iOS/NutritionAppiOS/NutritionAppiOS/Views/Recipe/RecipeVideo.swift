import SwiftUI
import WebKit

struct RecipeVideo: UIViewRepresentable {

    let videoId: String

    func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView()
        webView.isUserInteractionEnabled = false
        return WKWebView()
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {
        guard let youtubeURL = URL(string: "https://www.youtube.com/embed/\(videoId)") else { return }

        uiView.scrollView.isScrollEnabled = false
        uiView.load(URLRequest(url: youtubeURL))
    }
}
