import { getUsernameFromToken, showNotification, showConfirmationModal } from './utils.js';
import { apiService } from './api.js';
import { 
    postsContainer,
    renderHomePage,
    renderSinglePostPage,
    renderLoginPage,
    renderSignupPage,
    renderProfilePage,
    renderMyPostsPage,
    renderPostFormPage,
    renderEditProfilePage,
    renderSearchResultsPage,
    initPostFormListener,
    loadComments
 } from './ui.js';
import { checkLoginState, initAuthEventListeners } from './auth.js';


// --------------------------- START --------------------------------

$(document).ready(function(){
    // STATE
    let currentPage = 1;

    // INITIALIZATION
    checkLoginState();
    initAuthEventListeners();
    initPostFormListener();

    // ------------- Router ----------------
    function router() {
        $('#load-more-container').remove();
        const path = window.location.hash;

        if (path.startsWith('#post/')) {
            const parts = path.split('/');
            const postId = parts[1];
            renderSinglePostPage(postId);
        } else if (path === '#login') {
            renderLoginPage();
        } else if (path === '#signup') {
            renderSignupPage();
        } else if (path === '#profile') {
            renderProfilePage();
        } else if (path === '#profile/edit') {
            renderEditProfilePage();
        } else if (path === '#create-post') {
            renderPostFormPage(null);
        } else if (path === '#my-posts') {
            renderMyPostsPage();
        } else if (path.startsWith('#edit-post/')) {
            const parts = path.split('/');
            const postId = parts[1];
            renderPostFormPage(postId);
        } else if (path.startsWith('#/search/')) {
            const query = decodeURIComponent(path.split('/')[2]); // handle the white spaces and special char
            renderSearchResultsPage(query);
        } 
        else {
            currentPage = 1; // Reset to first page
            renderHomePage(currentPage);
        }
        checkLoginState();
    }

    $(window).on('hashchange', router);
    router();

    // ------------------ Event Listeners ---------------------

    $('header').on('submit', '#nav-search-form', function(event) {
        event.preventDefault();
        const searchInput = $(this).find('.nav-search-input');
        const query = searchInput.val().trim();

        if (query) {
            window.location.hash = `#/search/${query}`;
            searchInput.val('');
        }
    })


    postsContainer.on('click','#back-to-posts',function(event) {
        event.preventDefault();
        history.back();
    });

    postsContainer.on('click', '.edit-post-btn', function() {
        const postId = $(this).data('post-id');
        window.location.hash = `#edit-post/${postId}`;
    });

    postsContainer.on('click', '.delete-post-btn', function() {
        const postId = $(this).data('post-id');
        const postCard = $(this).closest('.my-posts-post-card');

        showConfirmationModal('Delete this post?', function() {
            apiService.deletePost(postId)
                .done(function() {
                    postCard.fadeOut(400, function() {
                        $(this).remove();
                    });
                    showNotification('Post deleted.', 'success');
                    router();
                })
                .fail(function() {
                    showNotification('Failed to delete post.', 'error');
                });
        });
    });

    $('main').on('click', '#load-more-btn', function() {
        const btn = $(this);
        currentPage++;
        btn.prop('disabled', true).text('Loading...');

        apiService.getPosts(currentPage - 1)
            .done(function(response) {
                const posts = response.content;
                posts.forEach(post => {
                    const postHtml = createPostCardHtml(post);
                    postsContainer.append(postHtml);
                });

                if (currentPage >= response.page.totalPages) {
                    $('#load-more-container').remove();
                }
            })
            .fail(function() {
                alert('Failed to load more posts. Please try again.');
                currentPage--; // Rollback page number on failure
            })
            .always(function() {
                if ($('#load-more-btn').length) { // Check if button still exists
                    btn.prop('disabled', false).text('Load More');
                }
            });
    });

    postsContainer.on('submit', '#comment-form', function(event) {
        event.preventDefault();

        $('#comment-content-error').text('');
        $('#comment-content').removeClass('input-error');

        const text = $('#comment-content').val().trim();
        if (!text) {
            $('#comment-content-error').text('Comment cannot be empty.');
            $('#comment-content').addClass('input-error');
            return;
        }
        
        const postId = window.location.hash.split('/')[1];
        
        apiService.addComment(postId, {text})
            .done(function() {
                $('#comment-content').val('');
                loadComments(postId);
                showNotification('Comment added successfully!', 'success');
            })
            .fail(function() {
                showNotification('Failed to add comment.', 'error');
            });
    });

    // ----------- Profile Edit Form -------------------
    
    postsContainer.on('submit', '#edit-profile-form', function(event) {
        event.preventDefault();

        // Clear previous errors
        $('.form-error').text('');
        $('#edit-profile-form input, #edit-profile-form textarea').removeClass('input-error');

        const email = $('#profile-email').val().trim();
        const bio = $('#profile-bio').val().trim();
        const website = $('#profile-website').val().trim();
        const username = localStorage.getItem('username');

        let isValid = true;
        if (email === '') {
            isValid = false;
            $('#profile-email-error').text('Email is required.');
            $('#profile-email').addClass('input-error');
        } else if (!/^\S+@\S+\.\S+$/.test(email)) {
            isValid = false;
            $('#profile-email-error').text('Please enter a valid email address.');
            $('#profile-email').addClass('input-error');
        }

        if (website && !/^https?:\/\/.+/.test(website)) {
            isValid = false;
            $('#profile-website-error').text('Please enter a valid URL (e.g., http://example.com).');
            $('#profile-website').addClass('input-error');
        }

        if (!isValid) {
            return;
        }

        const profileData = { email, bio, website };

        apiService.updateProfile(username, profileData)
            .done(function() {
                showNotification('Profile updated successfully!', 'success');
                window.location.hash = '#profile';
            })
            .fail(function() {
                showNotification('Failed to update profile. Please try again.', 'error');
            });
    });

    // ------------------ Comments ----------------------------
    postsContainer.on('click', '.edit-comment-btn', function() {
        const editButton = $(this);
        const commentDiv = editButton.closest('.comment');
        const commentTextP = commentDiv.find('.comment-text');
        const originalText = commentTextP.text();

        if (commentDiv.find('.edit-comment-container').length > 0) return;

        const editInputHtml = `
            <div class="edit-comment-container">
                <textarea class="edit-comment-textarea" rows="3">${originalText}</textarea>
                <div class="edit-comment-actions">
                    <button class="save-comment-btn" data-comment-id="${editButton.data('comment-id')}">Save</button>
                    <button class="cancel-edit-comment-btn">Cancel</button>
                </div>
            </div>
        `;

        commentTextP.hide();
        editButton.hide();
        editButton.siblings('.delete-comment-btn').hide();
        commentDiv.append(editInputHtml);
        commentDiv.find('.edit-comment-textarea').focus();
    });

    postsContainer.on('click', '.cancel-edit-comment-btn', function() {
        const commentDiv = $(this).closest('.comment');
        commentDiv.find('.edit-comment-container').remove();
        commentDiv.find('.comment-text').show();
        commentDiv.find('.delete-comment-btn').show();
        commentDiv.find('.edit-comment-btn').show();
    });

    postsContainer.on('click', '.save-comment-btn', function() {
        const saveButton = $(this);
        const commentDiv = saveButton.closest('.comment');
        const newText = commentDiv.find('.edit-comment-textarea').val().trim();
        const commentId = saveButton.data('comment-id');
        const postId = window.location.hash.split('/')[1];

        if (!newText) {
            showNotification('Comment cannot be empty.', 'error');
            return;
        }

        saveButton.prop('disabled', true).text('Saving...');

        apiService.editComment(commentId, { text: newText })
            .done(function() {
                showNotification('Comment updated successfully!', 'success');
                loadComments(postId);
            })
            .fail(function() {
                showNotification('Failed to update comment.', 'error');
                commentDiv.find('.edit-comment-container').remove();
                commentDiv.find('.comment-text').show();
                commentDiv.find('.delete-comment-btn').show();
                commentDiv.find('.edit-comment-btn').show();
            });
    });

    postsContainer.on('click', '.delete-comment-btn', function() {
        const commentId = $(this).data('comment-id');
        const postId = window.location.hash.split('/')[1];

        showConfirmationModal('Delete this comment?', function() {
            apiService.deleteComment(commentId)
                .done(function() {
                    showNotification('Comment deleted.', 'success');
                    loadComments(postId);
                })
                .fail(function() {
                    showNotification('Failed to delete comment.', 'error');
                });
        });
    });
});