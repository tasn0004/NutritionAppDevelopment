package com.nutritionapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
    private String recipeID;

    public interface CommentActionListener {
        void onReplySelected(String parentId);
        void onEditSelected(String commentId, String commentText);
    }

    private CommentActionListener listener;

    private List<Comment> commentsList;

    public CommentsAdapter(List<Comment> commentsList, CommentActionListener listener, String recipeID) {
        this.commentsList = commentsList;
        this.listener = listener;
        this.recipeID = recipeID;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = commentsList.get(position);
        holder.userNameTextView.setText(comment.getUserName());
        holder.commentTextView.setText(comment.getCommentText());

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        holder.commentDateTextView.setText(sdf.format(comment.getDate()));
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();

        if (comment.getParentId() != null && !comment.getParentId().isEmpty()) {
            // This is a reply comment
            layoutParams.leftMargin = holder.itemView.getContext().getResources().getDimensionPixelSize(R.dimen.reply_indentation_margin);
            setReplyVisibility(holder.itemView, comment.isReplyVisible());
            // For reply comments, always show the divider
            holder.divider.setVisibility(View.VISIBLE);
        } else {
            // This is a top-level comment
            layoutParams.leftMargin = 0;
            setTopLevelCommentLayout(holder.itemView);

            // Check if there are replies to this comment
            boolean hasReplies = false;
            int lastVisibleReplyIndex = -1;
            for (int i = position + 1; i < commentsList.size(); i++) {
                Comment reply = commentsList.get(i);
                if (reply.getParentId() != null && reply.getParentId().equals(comment.getObjectId())) {
                    hasReplies = true;
                    if (reply.isReplyVisible()) {
                        lastVisibleReplyIndex = i;
                    }
                } else {
                    break; // No more replies
                }
            }


            // Set the divider visibility based on whether there are visible replies
            holder.divider.setVisibility((!hasReplies || lastVisibleReplyIndex == position) ? View.VISIBLE : View.GONE);
        }

        holder.replyCommentButton.setOnClickListener(v -> {
            Log.d("CommentsAdapter", "Reply button clicked for comment: " + comment.getObjectId());
            listener.onReplySelected(comment.getObjectId());
        });
        holder.editCommentButton.setOnClickListener(v -> {
            listener.onEditSelected(comment.getObjectId(), comment.getCommentText());

        });
        holder.deleteCommentButton.setOnClickListener(v -> {
            deleteComment(comment.getObjectId(), position);

        });
        if (comment.getProfilePictureUrl() != null && !comment.getProfilePictureUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(comment.getProfilePictureUrl())
                    .placeholder(R.drawable.default_profile_pic) // default profile picture
                    .into(holder.profileImageView);
        } else {
            holder.profileImageView.setImageResource(R.drawable.default_profile_pic); // default profile pic
        }
        // Count the number of replies for this comment
        int replyCount = 0;
        for (Comment c : commentsList) {
            if (c.getParentId() != null && c.getParentId().equals(comment.getObjectId())) {
                replyCount++;
            }
        }

        holder.viewMoreRepliesButton.setVisibility(replyCount > 0 ? View.VISIBLE : View.GONE);
        boolean isAnyReplyVisible = false;
        for (Comment c : commentsList) {
            if (c.getParentId() != null && c.getParentId().equals(comment.getObjectId()) && c.isReplyVisible()) {
                isAnyReplyVisible = true;
                break;
            }
        }

        holder.viewMoreRepliesButton.setText(isAnyReplyVisible ? "Hide replies" : "View more replies");
        holder.viewMoreRepliesButton.setOnClickListener(v -> {
            for (Comment c : commentsList) {
                if (c.getParentId() != null && c.getParentId().equals(comment.getObjectId())) {
                    c.setReplyVisible(!c.isReplyVisible());
                }
            }
            notifyDataSetChanged();
        });



        boolean hasReplies = false;
        for (Comment c : commentsList) {
            if (c.getParentId() != null && c.getParentId().equals(comment.getObjectId())) {
                hasReplies = true;
                break;
            }

        }

        if (hasReplies) {
            holder.viewMoreRepliesButton.setVisibility(View.VISIBLE);
            // Set the text depending on whether any reply is visible
            isAnyReplyVisible = false;
            for (Comment c : commentsList) {
                if (c.getParentId() != null && c.getParentId().equals(comment.getObjectId()) && c.isReplyVisible()) {
                    isAnyReplyVisible = true;
                    break;
                }
            }
            holder.viewMoreRepliesButton.setText(isAnyReplyVisible ? "Hide replies" : "View more replies");
        } else {
            holder.viewMoreRepliesButton.setVisibility(View.GONE);
        }

        if (comment.getUserId().equals(ParseUser.getCurrentUser().getObjectId())) {
            // Show delete and edit buttons for the user's own comments
            holder.deleteCommentButton.setVisibility(View.VISIBLE);
            holder.editCommentButton.setVisibility(View.VISIBLE);
            holder.replyCommentButton.setVisibility(View.GONE);

            // Set click listeners for buttons
            holder.deleteCommentButton.setOnClickListener(v -> {
                deleteComment(comment.getObjectId(), position);
            });

            holder.editCommentButton.setOnClickListener(v -> {
                listener.onEditSelected(comment.getObjectId(), comment.getCommentText());
            });
        } else {
            // Hide buttons for comments from other users
            holder.deleteCommentButton.setVisibility(View.GONE);
            holder.editCommentButton.setVisibility(View.GONE);
            holder.replyCommentButton.setVisibility(View.VISIBLE);

        }

    }

    private void setReplyVisibility(View itemView, boolean isVisible) {
        itemView.setVisibility(isVisible ? View.VISIBLE : View.VISIBLE);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        if (isVisible) {
            params.height = RecyclerView.LayoutParams.WRAP_CONTENT;
            params.width = RecyclerView.LayoutParams.MATCH_PARENT;
        } else {
            params.height = 0;
            params.width = 0;
        }
        itemView.setLayoutParams(params);
    }
    public void addReply(Comment replyComment) {
        // Ensure replyComment has a valid parentId
        if (replyComment.getParentId() == null || replyComment.getParentId().isEmpty()) {
            Log.e("CommentsAdapter", "Reply does not have a valid parent ID");
            return;
        }

        int insertIndex = -1;
        for (int i = 0; i < commentsList.size(); i++) {
            Comment currentComment = commentsList.get(i);
            // Check if currentComment is the parent of the reply
            if (currentComment.getObjectId().equals(replyComment.getParentId())) {
                insertIndex = i + 1;
                while (insertIndex < commentsList.size() &&
                        commentsList.get(insertIndex).getParentId() != null &&
                        commentsList.get(insertIndex).getParentId().equals(replyComment.getParentId())) {
                    insertIndex++;
                }
                break;
            }
        }

        if (insertIndex != -1) {
            commentsList.add(insertIndex, replyComment);
            notifyItemInserted(insertIndex);
        } else {
            Log.e("CommentsAdapter", "Parent comment not found for the reply");
        }
    }






    private void deleteComment(String commentId, int position) {
        final int[] numberOfCommentsToDelete = {1}; // Start with 1 for the top-level comment, use array to mutate the count

        // First, delete all replies to this comment
        for (int i = commentsList.size() - 1; i >= 0; i--) {
            Comment c = commentsList.get(i);
            if (commentId.equals(c.getParentId())) {
                numberOfCommentsToDelete[0]++;
                ParseObject replyCommentObj = ParseObject.createWithoutData("Comments", c.getObjectId());
                replyCommentObj.deleteInBackground(e -> {
                    if (e == null) {
                        // Reply Comment deleted successfully
                        Log.d("Delete Reply Comment", "Reply comment deleted successfully");
                    } else {
                        // Handle error
                    }
                });
                commentsList.remove(i);
                notifyItemRemoved(i);
            }
        }

        // Then, delete the actual comment
        ParseObject commentObj = ParseObject.createWithoutData("Comments", commentId);
        commentObj.deleteInBackground(e -> {
            if (e == null) {
                // Comment deleted successfully
                commentsList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, commentsList.size());
                decrementRecipeCommentCount(recipeID, numberOfCommentsToDelete[0]);
            } else {
                Log.e("Delete Comment Error", e.getMessage(), e);
            }
        });
    }

    private void decrementRecipeCommentCount(String recipeId, int count) {
        Log.d("Decrement", "y: " + count);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Recipes");
        query.getInBackground(recipeId, (recipe, e) -> {
            if (e == null) {
                int currentComments = recipe.getInt("numberOfComments");
                int newCommentsCount = currentComments - count;
                Log.d("Decrement", "Current count: " + currentComments + ", new count: " + newCommentsCount);
                recipe.put("numberOfComments", newCommentsCount >= 0 ? newCommentsCount : 0);
                recipe.saveInBackground(e2 -> {
                    if (e2 == null) {
                        Log.d("Decrement", "Decrement successful.");
                    } else {
                        Log.e("Decrement Error", "Save error: " + e2.getMessage());
                    }
                });
            } else {
                Log.e("Decrement Error", "Fetch error: " + e.getMessage());
            }
        });
    }




    private void setTopLevelCommentLayout(View itemView) {
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        params.height = RecyclerView.LayoutParams.WRAP_CONTENT;
        params.width = RecyclerView.LayoutParams.MATCH_PARENT;
        itemView.setLayoutParams(params);
    }


    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView;
        TextView commentTextView;
        ImageView replyCommentButton;
        TextView commentDateTextView;
        TextView viewMoreRepliesButton;
        ImageView deleteCommentButton;
        ImageView editCommentButton;
        ImageView profileImageView;

        View divider;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            deleteCommentButton = itemView.findViewById(R.id.deleteCommentButton);
            commentDateTextView = itemView.findViewById(R.id.commentDateTextView);
            replyCommentButton = itemView.findViewById(R.id.replyCommentButton);
            viewMoreRepliesButton = itemView.findViewById(R.id.viewMoreRepliesButton);
            editCommentButton = itemView.findViewById(R.id.editCommentButton);
            profileImageView = itemView.findViewById(R.id.profileImageView);

            divider = itemView.findViewById(R.id.divider);
        }
    }

}