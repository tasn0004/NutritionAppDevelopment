import SwiftUI

struct RecipeCategoryHash: Hashable {
    let name: String
    let imageName: UIImage?
    
    init(_ name: String, _ imageName: UIImage?) {
        self.name = name
        self.imageName = imageName
    }
}
