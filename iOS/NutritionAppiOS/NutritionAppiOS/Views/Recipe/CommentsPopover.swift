import SwiftUI
import Parse

struct CommentsPopover: View {
    
    @State private var commentText = ""
    @State private var parentComments: [CommentItem] = []
    @State private var childComments: [CommentItem] = []
    
    @State private var shouldRefresh = false
    
    @FocusState private var isTextEditorFocused: Bool
    
    //Environment objects
    @EnvironmentObject private var backendUtilities: BackendUtilities
    @EnvironmentObject private var screenDimensions: ScreenDimensions
    
    //Screen dimensions
    @State private var screenWidth = 0.0
    @State private var screenHeight = 0.0
    
    @State private var parentId = ""
    @State private var editingCommentId = ""
    @State private var expandedComments: [String] = []
    
    private var recipeId: String
    @Binding private var numComments: Int
    
    init(_ recipeId: String, _ numComments: Binding<Int>) {
        self.recipeId = recipeId
        self._numComments = numComments
    }
    
    var body: some View {
        VStack {
            //Swipe to close rectangle marker
            RoundedRectangle(cornerRadius: 10)
                .frame(width: 0.10*screenWidth, height: 0.01*screenHeight)
                .foregroundColor(.gray)
                .opacity(0.5)
                .padding(.vertical)
            
            //Comments
            ScrollView {
                LazyVStack {
                    ForEach(parentComments.indices, id: \.self) { index in
                        commentItemView(parentComments[index])
                    }
                }
            }
            .padding(.horizontal)
            .clipped()
            Spacer()
            
            newCommentArea()
        }
        .onChange(of: shouldRefresh) { _ in
            if shouldRefresh {
                initializeComments()
                shouldRefresh = false
            }
        }
        .onAppear {
            UITextView.appearance().backgroundColor = .clear
            screenDimensions.setScreenDimensions(&screenWidth, &screenHeight)
            initializeComments()
        }
        .onDisappear {
            commentText = ""
        }
    }
    
    /*
     
        Creates the view for a single comment item including, user's name, timestamp, button row, and child comments if present.
     
     */
    func commentItemView(_ comment: CommentItem) -> some View {
        Group {
            //Commenters name and row of icons
            commentTopRow(comment)
            
            commentTextAndTimestamp(comment)

            //Child comment area
            if commentHasReplies(comment.id) {
                childCommentArea(comment)
            }
            
            //Line under comment
            Divider()
                .foregroundColor(.gray)
        }
    }
    
    /*
     
        Creates the top row of a comment view. Contains the commenters name and the row of icon options.
     
     */
    func commentTopRow(_ comment: CommentItem) -> some View {
        HStack {
            //Commenter's name
            Text("\(comment.userName)")
                .foregroundColor(ComponentColours.submitButton)
            
            Spacer()
            //Row of option icons
            commentItemOptions(comment)
        }
        .modifier(CustomTextLabelStyle(0.03*screenWidth, false))
    }
    
    /*
     
        Creates the view for the comment text and timestamp
     
     */
    func commentTextAndTimestamp(_ comment: CommentItem) -> some View {
        HStack {
            //Comment text
            Text(comment.commentText)
                .font(.body)
            Spacer()
            
            //Timestamp
            Text("\(comment.timestamp)")
                .font(.caption)
                .padding(.trailing)
        }
        .modifier(CustomTextLabelStyle(0.03*screenWidth, false))
    }
    
    /*
     
        Creates the child comment area underneath a parent comment that has replies.
     
     */
    func childCommentArea(_ parentComment: CommentItem) -> some View {
        HStack {
            VStack {
                Divider()
                    .background(.gray)
                
                //Child comments
                ForEach(childComments.indices, id: \.self) { index in
                    if childComments[index].parentId == parentComment.id && isCommentExpanded(parentComment.id) {
                        
                        //Chile commenter's name
                        commentTopRow(childComments[index])

                        //Child comment text
                        commentTextAndTimestamp(childComments[index])
                        
                        Divider()
                            .background(.gray)
                    }
                }
                .padding(.leading, 0.05*screenWidth)
                
                //View/hide replies button
                viewHideReplyButton(parentComment)
            }
        }
        .modifier(CustomTextLabelStyle(0.03*screenWidth, false))
    }
    
    /*
     
        Creates the view/hide replies button underneath a parent comment with child comments.
     
     */
    func viewHideReplyButton(_ parentComment: CommentItem) -> some View {
        HStack {
            Spacer()
            
            Button {
                if isCommentExpanded(parentComment.id) {
                    expandedComments.removeAll { $0 == parentComment.id }
                } else {
                    expandedComments.append(parentComment.id)
                }
                
            } label: {
                Text(!isCommentExpanded(parentComment.id) ? "View replies" : "Hide replies")
                    .foregroundColor(.gray)
                    .padding()
                    .background(
                        RoundedRectangle(cornerRadius: 10)
                            .frame(width: 0.50*screenWidth, height: 0.02*screenHeight)
                            .foregroundColor(.black)
                            .opacity(0.8)
                    )
            }
            Spacer()
        }
    }
    
    /*
     
        
     
     */
    func isCommentExpanded(_ commentId: String) -> Bool {
        return expandedComments.contains(commentId)
    }
    
    /*
     
        Checks the parentId value of all child comments to determine if a comment is a reply to the parent comment with commentId.
     
     */
    func commentHasReplies(_ commentId: String) -> Bool {
        for comment in childComments {
            if comment.parentId == commentId { return true }
        }
        
        return false
    }
    
    /*
     
        Creates the reply, edit and delete buttons on a comment item.
     
     */
    func commentItemOptions(_ comment: CommentItem) -> some View {
        HStack(spacing: 0.02*screenWidth) {
            if comment.userId == backendUtilities.currentUser.objectId {
                editCommentButton(comment)
                deleteCommentButton(comment)
            } else {
                replyToCommentButton(comment)
            }
        }
    }
    
    /*
     
        Creates the add to meal plan button inside the hamburger menu.
     
     */
    func editCommentButton(_ comment: CommentItem) -> some View {
        Button(action: {
            commentText = comment.commentText
            editingCommentId = comment.id
            isTextEditorFocused = true
        }) {
            Image(systemName: "pencil")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 0.03*screenWidth)
        }
    }
    
    /*
     
        Creates the add to meal plan button inside the hamburger menu.
     
     */
    func deleteCommentButton(_ comment: CommentItem) -> some View {
        Button(action: {
            onDelete(comment)
        }) {
            Image(systemName: "trash")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 0.03*screenWidth)
        }
    }
    
    /*
     
        Creates the reply to comment button
     
     */
    func replyToCommentButton(_ parentComment: CommentItem) -> some View {
        Button(action: {
            //Set the parent id of the reply comment to be parentComment's id if parentComment is a true parent comment. Else parent comment is a child comment itself and take its parentId.
            if parentComment.parentId.isEmpty {
                parentId = parentComment.id
            } else {
                parentId = parentComment.parentId
            }
            
            isTextEditorFocused = true
        }) {
            Image(systemName: "arrowshape.turn.up.left")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 0.03*screenWidth)
        }
    }
    
    /*

        Creates the submit button and new comment textfield.

    */
    func newCommentArea() -> some View {
        HStack {
            ZStack {
                TextEditor(text: $commentText)
                    .focused($isTextEditorFocused)
                    .padding(0.02*screenWidth)
                    .background {
                        RoundedRectangle(cornerRadius: 10)
                            .stroke(.gray, lineWidth: 0.005*screenWidth)
                    }
                
                Text("Leave a comment...")
                    .foregroundColor(.gray)
                    .modifier(CustomTextLabelStyle(0.04*screenWidth, false))
                    .padding()
                    .padding(.trailing, 0.25*screenWidth)
                    .opacity(!isTextEditorFocused ? 0.5 : 0.0)
                    .onTapGesture {
                        isTextEditorFocused = true
                    }
            }

            // Submit button
            Button("Post") {
                if !commentText.isEmpty {
                    onSubmit()
                    
                }
            }
            .disabled(commentText.isEmpty)
            .padding(0.03*screenWidth)
            .foregroundColor(ComponentColours.submitButton)
            .background {
                RoundedRectangle(cornerRadius: 10)
                    .stroke(.gray, lineWidth: 0.005*screenWidth)
            }
        }
        .frame(height: 0.05*screenHeight)
        .padding()
    }
    
    /*
     
        Removes a user added comment from the Comments table.
     
     */
    func onDelete(_ comment: CommentItem) {
        let query = PFQuery(className: "Comments")
        query.whereKey("objectId", equalTo: comment.id)

        query.getFirstObjectInBackground { (commentObject, error) in
            if let error = error {
                print("Error fetching comment \(error.localizedDescription)\n")
                
            } else if let comment = commentObject {
                comment.deleteInBackground { success, deleteError in
                    if success {
                        modifyCommentCount(-1)
                        shouldRefresh = true
                        print("Successfully deleted comment,\n")
                        
                    } else if let deleteError = deleteError {
                        print("Unsuccessfully deleted comment \(deleteError.localizedDescription)\n")
                    }
                }
            }
        }
    }
    
    /*
     
        Adds a new comment to the Comments table upon selecting submit and text being valid.
     
     */
    func onSubmit() {
        let newComment = PFObject(className:"Comments")
        
        if !editingCommentId.isEmpty{
            newComment.objectId = editingCommentId
        }
        
        newComment["userId"] = backendUtilities.currentUser.objectId
        newComment["recipeId"] = recipeId
        newComment["commentText"] = commentText
        newComment["userName"] = backendUtilities.getUserFullName()
        newComment["date"] = Date()
        newComment["parentId"] = parentId

        newComment.saveInBackground { success, error in
            if success {
                print("Successfully saved new comment to database.\n")
                modifyCommentCount(1)
                shouldRefresh = true
                
            } else if let error = error {
                print("Unsuccessfully saved new comment to database: \(error.localizedDescription)")
            }
        }
        
        commentText = ""
        parentId = ""
        editingCommentId = ""
    }
    
    /*
     
        Returns a sorted array of comment items by time
     
     */
    func sortCommentsByTime(_ commentArray: [CommentItem]) -> [CommentItem] {
        let sortedArray = commentArray.sorted { (comment1, comment2) -> Bool in
            return comment1.timestamp < comment2.timestamp
        }

        return sortedArray
    }
    
    /*
     
        Creates a CommentItem object from the comment data passed in.
     
     */
    func createCommentItem(_ comment: PFObject) -> CommentItem {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "MMM d, yyyy"
        
        let id = comment.objectId as? String ?? ""
        let userId = comment["userId"] as? String ?? ""
        let recipeId = comment["recipeId"] as? String ?? ""
        let commentText = comment["commentText"] as? String ?? ""
        let userName = comment["userName"] as? String ?? ""
        let timestamp = dateFormatter.string(from: comment["date"] as? Date ?? Date())
        let parentId = comment["parentId"] as? String ?? ""
        
        return CommentItem(id, userId, recipeId, commentText, userName, timestamp, parentId)
    }
    
    /*
     
        Increments or decrements the recipe's times liked count by 1.
     
     */
    func modifyCommentCount(_ incrementFactor: Int) {
        let query = PFQuery(className: "Recipes")
        query.getObjectInBackground(withId: recipeId) { (recipeObject, error) in
            if let error = error {
                print("Error fetching recipe id: \(recipeId) \(error.localizedDescription)\n")
                
            } else if let recipeObject = recipeObject {
                
                if incrementFactor == 1 {
                    recipeObject.incrementKey("numberOfComments", byAmount: 1)
                    numComments += 1
                } else {
                    recipeObject.incrementKey("numberOfComments", byAmount: -1)
                    numComments -= 1
                }
                
                recipeObject.saveInBackground { (success, error) in
                    if success {
                        print("Successfully updated the numberOfComments value for recipe \(recipeId).\n")
                    } else if let error = error {
                        print("Unsuccessfully updated the numberOfComments value for recipe \(recipeId). \(error.localizedDescription)\n")
                    }
                }
            }
        }
    }
    
    /*
     
        Initialize the array of CommentItem objects.
     
     */
    func initializeComments() {
        let query = PFQuery(className: "Comments")
        parentComments = []
        childComments = []
        
        query.whereKey("recipeId", equalTo: recipeId)
        query.findObjectsInBackground { (commentObjects, error) in
            if let error = error {
                print("Error fetching comments: \(error.localizedDescription)")
                
            } else if let commentObjects = commentObjects {
                for comment in commentObjects {
                    let parentId = comment["parentId"] as? String ?? ""
                    
                    //No parent id means comment is parent comment, append to appropriate array
                    if parentId.isEmpty {
                        parentComments.append(createCommentItem(comment))
                    } else {
                        childComments.append(createCommentItem(comment))
                    }
                }
                //Sort arrays chronologically
                parentComments = sortCommentsByTime(parentComments)
            }
        }
    }
}
