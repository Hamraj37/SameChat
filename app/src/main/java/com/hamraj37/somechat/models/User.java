package com.hamraj37.somechat.models;

public class User {
    private String uid;
    private String username;
    private String displayName;
    private String email;
    private String photoUrl;
    private String searchName;
    private boolean online;
    private long lastSeen;
    private String publicKey;
    private String bio;
    private String coverUrl;
    private String fcmToken;
    private java.util.Map<String, Boolean> groups;
    private java.util.List<Object> websites;

    public static class Website {
        private String title;
        private String url;
        private String faviconUrl;

        public Website() {}

        public Website(String title, String url) {
            this.title = title;
            this.url = url;
        }

        public Website(String title, String url, String faviconUrl) {
            this.title = title;
            this.url = url;
            this.faviconUrl = faviconUrl;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getFaviconUrl() { return faviconUrl; }
        public void setFaviconUrl(String faviconUrl) { this.faviconUrl = faviconUrl; }
    }

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String username, String displayName, String email, String photoUrl) {
        this.uid = uid;
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.photoUrl = photoUrl;
        this.searchName = displayName != null ? displayName.toLowerCase() : "";
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getSearchName() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public java.util.Map<String, Boolean> getGroups() {
        return groups;
    }

    public void setGroups(java.util.Map<String, Boolean> groups) {
        this.groups = groups;
    }

    public java.util.List<Website> getWebsites() {
        if (websites == null) return null;
        java.util.List<Website> result = new java.util.ArrayList<>();
        for (Object item : websites) {
            if (item instanceof String) {
                String url = (String) item;
                result.add(new Website(url, url));
            } else if (item instanceof java.util.Map) {
                java.util.Map<String, String> map = (java.util.Map<String, String>) item;
                result.add(new Website(map.get("title"), map.get("url"), map.get("faviconUrl")));
            } else if (item instanceof Website) {
                result.add((Website) item);
            }
        }
        return result;
    }

    public void setWebsites(java.util.List<Website> websites) {
        if (websites == null) {
            this.websites = null;
        } else {
            this.websites = new java.util.ArrayList<>(websites);
        }
    }
}
