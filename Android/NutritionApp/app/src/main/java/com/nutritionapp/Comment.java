package com.nutritionapp;
import java.util.Date;

public class Comment {
    private String userName;
    private String commentText;
    private String objectId;
    private String userId;
    private String recipeId;
    private Date date;
    private String parentId;
    private boolean isReplyVisible;
    private String profilePictureUrl;

    public Comment(String userName, String commentText, String objectId, String userId, String recipeId, Date date, String parentId, String profilePictureUrl) {
        this.userName = userName;
        this.commentText = commentText;
        this.objectId = objectId;
        this.userId = userId;
        this.recipeId = recipeId;
        this.date = date;
        this.parentId = parentId;
        this.isReplyVisible = false;
        this.profilePictureUrl = profilePictureUrl;

    }


    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }
    public String getUserName() {
        return userName;
    }

    public String getCommentText() {
        return commentText;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getUserId() {
        return userId;
    }

    public String getRecipeId() {
        return recipeId;
    }
    public Date getDate() {
        return date;
    }

    public String getParentId() {
        return parentId;
    }
    public boolean isReplyVisible() {
        return isReplyVisible;
    }

    public void setReplyVisible(boolean replyVisible) {
        isReplyVisible = replyVisible;
    }
}
