import SwiftUI

struct RecipeTileHash: Hashable {
    let name: String
    let imageName: UIImage?
    let recipeId: String
    let mealType: String
    let ingredients: [[Any]]

    init(_ name: String, _ imageName: UIImage?, _ recipeId: String, _ mealType: String, _ ingredients: [[Any]]) {
        self.name = name
        self.imageName = imageName
        self.recipeId = recipeId
        self.mealType = mealType
        self.ingredients = ingredients
    }

    // Implement the Hashable protocol
    func hash(into hasher: inout Hasher) {
       // Combine the hash values of the properties you want to use for hashing
       hasher.combine(name)
       hasher.combine(imageName)
       hasher.combine(recipeId)
       hasher.combine(mealType)
    }

    // Implement the Equatable protocol for == comparison
    static func == (lhs: RecipeTileHash, rhs: RecipeTileHash) -> Bool {
       return lhs.name == rhs.name && lhs.imageName == rhs.imageName && lhs.recipeId == rhs.recipeId && lhs.mealType == rhs.mealType
    }
}
