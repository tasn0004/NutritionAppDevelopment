import SwiftUI

struct CommentItem {
    let id: String
    let userId: String
    let recipeId: String
    let commentText: String
    let userName: String
    let timestamp: String
    let parentId: String
    
    init(_ id: String, _ userId: String, _ recipeId: String, _ commentText: String, _ userName: String,  _ timestamp: String, _ parentId: String) {
        self.id = id
        self.userId = userId
        self.recipeId = recipeId
        self.commentText = commentText
        self.userName = userName
        self.timestamp = timestamp
        self.parentId = parentId
    }
}

