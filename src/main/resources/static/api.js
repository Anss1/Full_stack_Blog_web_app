const globalApiUrl = 'http://localhost:8080/api/v1';

export const apiService = {
    baseUrl: globalApiUrl,
    
    _makeRequest: function(url, type, data) {
        return $.ajax({
            url: this.baseUrl + url,
            type: type,
            contentType: 'application/json',
            data: data ? JSON.stringify(data) : null,
            beforeSend: function(xhr) {
                const token = localStorage.getItem('accessToken');
                if (token) {
                    xhr.setRequestHeader('Authorization', `Bearer ${token}`);
                }
            }
        });
    },

    // ========= Auth ==========
    login: function(username, password) {
        return this._makeRequest('/auth/login', 'POST', { username, password });
    },
    signup: function(username, email, password, confirmPassword) {
        return this._makeRequest('/auth/signup', 'POST', { username, email, password, confirmPassword });
    },
    logout: function(refreshToken) {
        return this._makeRequest('/auth/logout', 'POST', { refreshToken });
    },
    refresh: function(refreshToken) {
        return this._makeRequest('/auth/refresh', 'POST', { refreshToken });
    },

    // ======== Manage Posts ========
    getPosts: function(page = 0, size = 5) {
        return this._makeRequest(`/posts?page=${page}&size=${size}`, 'GET');
    },
    searchPosts: function(keyword) {
        return this._makeRequest(`/posts/search?keyword=${keyword}`, 'GET');
    },
    getPost: function(postId) {
        return this._makeRequest(`/posts/${postId}`, 'GET');
    },
    getMyPosts: function() {
        return this._makeRequest('/posts/my-posts', 'GET');
    },
    createPost: function(postData) {
        return this._makeRequest('/posts', 'POST', postData);
    },
    updatePost: function(postId, postData) {
        return this._makeRequest(`/posts/${postId}`, 'PUT', postData);
    },
    deletePost: function(postId) {
        return this._makeRequest(`/posts/${postId}`, 'DELETE');
    },

    // ======== Comments ========
    getComments: function(postId) {
        return this._makeRequest(`/posts/${postId}/comments`, 'GET');
    },
    addComment: function(postId, commentData) {
        return this._makeRequest(`/posts/${postId}/comments`, 'POST', commentData);
    },
    editComment: function(commentId, commentData) {
        return this._makeRequest(`/comments/${commentId}`, 'PUT', commentData);
    },
    deleteComment: function(commentId) {
        return this._makeRequest(`/comments/${commentId}`, 'DELETE');
    },

    //========= Profile ==========
    getProfile: function(username) {
        return this._makeRequest(`/profiles/${username}`, 'GET');
    },
    updateProfile: function(username, profileData) {
        return this._makeRequest(`/profiles/${username}`, 'PUT', profileData);
    }
};