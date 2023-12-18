package com.nutritionapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class is responsible for displaying and managing user comments on a recipe.
 */
public class CommentsPage extends BottomSheetDialogFragment implements CommentsAdapter.CommentActionListener {

    private RecyclerView commentsRecyclerView;
    private EditText commentEditText;
    private Button postCommentButton;
    private String replyToCommentId = null;
    private String recipeID;
    // Data
    private List<Comment> commentList = new ArrayList<>();
    private CommentsAdapter commentsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.comments_page, container, false);

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            recipeID = getArguments().getString("RECIPE_ID");
        }

        // Initialize UI elements
        commentsRecyclerView = view.findViewById(R.id.commentsRecyclerView);
        commentEditText = view.findViewById(R.id.commentEditText);
        postCommentButton = view.findViewById(R.id.postCommentButton);

        // Setup RecyclerView
        commentsAdapter = new CommentsAdapter(commentList, this, recipeID);
        commentsRecyclerView.setAdapter(commentsAdapter);


        Log.d("CommentsPage", "Received recipe ID: " + recipeID);
        loadCommentsFromDatabase(recipeID);

        postCommentButton.setOnClickListener(v -> {
            String commentText = commentEditText.getText().toString().trim();
            if (!commentText.isEmpty()) {
                ParseObject commentObj = new ParseObject("Comments");
                commentObj.put("commentText", commentText);
                commentObj.put("userId", ParseUser.getCurrentUser().getObjectId());
                commentObj.put("userName", ParseUser.getCurrentUser().getString("firstName") + " " + ParseUser.getCurrentUser().getString("lastName"));
                commentObj.put("recipeId", recipeID);
                commentObj.put("date", new Date());
                commentEditText.setHint("Write a comment");
                commentEditText.clearFocus();
                // Check if replying to a comment
                if (replyToCommentId != null && !replyToCommentId.isEmpty()) {

                    commentObj.put("parentId", replyToCommentId); // Set parentId for the reply
                    replyToCommentId = null; // Reset the replyToCommentId
                }

                commentObj.saveInBackground(e -> {
                    if (e == null) {
                        // Comment saved successfully
                        loadCommentsFromDatabase(recipeID);
                        commentEditText.setText(""); // Clear the EditText
                        hideKeyboard();
                        incrementRecipeCommentCount(recipeID);
                    } else {
                        Log.e("Save Comment Error", e.getMessage(), e);
                    }
                });
            }
        });

        return view;

    }

    /**
     * this method expand BottomSheetDialogFragment to be full page by default
     */
    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }
    private void handleReplyAddition(String replyText, String commentIdToReplyTo) {
        // Fetch the profile picture URL for the current user
        ParseFile profilePic = fetchUserProfilePic(ParseUser.getCurrentUser().getObjectId());
        String profilePicUrl = profilePic != null ? profilePic.getUrl() : null;

        // Determine the top-level parentId
        String topLevelParentId = null;
        for (Comment comment : commentList) {
            if (comment.getObjectId().equals(commentIdToReplyTo)) {
                // If the comment being replied to is a top-level comment, use its objectId as the parentId
                // Otherwise, use its parentId (which is the ID of the top-level comment it's replying to)
                topLevelParentId = (comment.getParentId() == null || comment.getParentId().isEmpty()) ? comment.getObjectId() : comment.getParentId();
                break;
            }
        }

        if (topLevelParentId != null) {
            // Create a new Comment object for the reply
            String userName = ParseUser.getCurrentUser().getString("firstName") + " " + ParseUser.getCurrentUser().getString("lastName");
            String uniqueID = System.currentTimeMillis() + "_" + ParseUser.getCurrentUser().getObjectId();
            Comment replyComment = new Comment(userName, replyText, uniqueID, ParseUser.getCurrentUser().getObjectId(), recipeID, new Date(), topLevelParentId, profilePicUrl);

            // Add the reply to the comments list and notify the adapter
            commentsAdapter.addReply(replyComment);
            commentsAdapter.notifyDataSetChanged();

            // Save the reply comment to the database
            ParseObject commentObj = new ParseObject("Comments");
            commentObj.put("commentText", replyText);
            commentObj.put("userId", ParseUser.getCurrentUser().getObjectId());
            commentObj.put("userName", userName);
            commentObj.put("recipeId", recipeID);
            commentObj.put("date", new Date());
            commentObj.put("parentId", topLevelParentId); // Set the parentId to the ID of the top-level comment being replied to
            commentObj.saveInBackground(e -> {
                if (e == null) {
                    // Reply saved successfully
                    loadCommentsFromDatabase(recipeID);
                    commentEditText.setText(""); // Clear the EditText
                    hideKeyboard();
                    incrementRecipeCommentCount(recipeID);
                } else {
                    Toast.makeText(getActivity(), "Error saving reply: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getActivity(), "Top-level comment not found.", Toast.LENGTH_SHORT).show();
        }
    }



    private void loadCommentsFromDatabase(String recipeId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Comments");
        query.whereEqualTo("recipeId", recipeId);
        query.findInBackground((comments, e) -> {
            if (e == null) {
                List<Comment> sortedComments = new ArrayList<>();
                // First, add all parent comments (comments without a parentId)
                for (ParseObject commentObj : comments) {
                    String parentId = commentObj.getString("parentId");
                    if (parentId == null || parentId.isEmpty()) {
                        sortedComments.add(convertParseObjectToComment(commentObj));
                    }
                }
                // Next, for each parent comment, add its replies immediately after it
                for (Comment parentComment : new ArrayList<>(sortedComments)) {
                    for (ParseObject commentObj : comments) {
                        String parentId = commentObj.getString("parentId");
                        if (parentId != null && !parentId.isEmpty() && parentId.equals(parentComment.getObjectId())) {
                            sortedComments.add(sortedComments.indexOf(parentComment) + 1, convertParseObjectToComment(commentObj));
                        }
                    }
                }
                commentList.clear();
                commentList.addAll(sortedComments);
                commentsAdapter.notifyDataSetChanged();
            } else {
            }
        });
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    private Comment convertParseObjectToComment(ParseObject commentObj) {
        String userName = commentObj.getString("userName");
        String commentText = commentObj.getString("commentText");
        String objectId = commentObj.getObjectId();
        String userId = commentObj.getString("userId");
        Date date = commentObj.getDate("date");
        String parentId = commentObj.getString("parentId");
        String recipeId = commentObj.getString("recipeId");

        ParseFile profilePic = fetchUserProfilePic(userId);
        String profilePicUrl = profilePic != null ? profilePic.getUrl() : null;

        return new Comment(userName, commentText, objectId, userId, recipeId, date, parentId, profilePicUrl);
    }

    private ParseFile fetchUserProfilePic(String userId) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", userId);
        try {
            ParseUser user = query.getFirst();
            return user.getParseFile("userPictureProfile");
        } catch (ParseException e) {
            Log.e("Error", "Error fetching user profile picture: " + e.getMessage());
            return null;
        }
    }


    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getView() != null) {
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }

    @Override
    public void onReplySelected(String parentId) {
        String replyToUsername = "";
        for (Comment comment : commentList) {
            if (comment.getObjectId().equals(parentId)) {
                replyToUsername = comment.getUserName();
                break;
            }
        }
        commentEditText.setHint("Replying to " + replyToUsername);
        commentEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(commentEditText, InputMethodManager.SHOW_IMPLICIT);
        }
        postCommentButton.setOnClickListener(v -> {
            String replyText = commentEditText.getText().toString().trim();
            if (!replyText.isEmpty()) {
                handleReplyAddition(replyText, parentId);
            } else {
                Toast.makeText(getActivity(), "Reply cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditSelected(String commentId, String commentText) {
        commentEditText.setText(commentText);
        commentEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(commentEditText, InputMethodManager.SHOW_IMPLICIT);
        }

        postCommentButton.setOnClickListener(v -> {
            String editedText = commentEditText.getText().toString().trim();
            if (!editedText.isEmpty()) {
                updateCommentInDatabase(commentId, editedText);
            } else {
                Toast.makeText(getActivity(), "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateCommentInDatabase(String commentId, String newText) {
        ParseObject commentObj = ParseObject.createWithoutData("Comments", commentId);
        commentObj.put("commentText", newText);
        commentObj.saveInBackground(e -> {
            if (e == null) {
                // Comment updated successfully, reload comments
                loadCommentsFromDatabase(recipeID);
                commentEditText.setText(""); // Clear the EditText
                hideKeyboard();
            } else {
                // Handle the update error
                Log.e("Update Comment Error", e.getMessage(), e);
            }
        });
    }
    private void incrementRecipeCommentCount(String recipeId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Recipes");
        query.getInBackground(recipeId, (recipe, e) -> {
            if (e == null) {
                // Get the current number of comments and increment
                int currentComments = recipe.getInt("numberOfComments");
                recipe.put("numberOfComments", currentComments + 1);
                recipe.saveInBackground();
            } else {
                Log.e("Increment Error", "Error: " + e.getMessage());
            }
        });
    }

}
