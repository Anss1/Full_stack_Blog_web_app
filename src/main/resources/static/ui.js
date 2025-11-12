import { apiService } from './api.js';
import { showNotification, showSpinner, hideSpinner } from './utils.js';

let easyMDEInstance = null;

export const postsContainer = $('#posts-container');

export function loadComments(postId) {
    const commentsList = $('#comments-list');
    commentsList.html('<p>Loading comments...</p>');

    apiService.getComments(postId)
        .done(function(comments) {
            commentsList.empty();

            if (comments.length === 0) {
                commentsList.html('<p>No comments yet. Be the first to comment!</p>');
                return;
            }

            comments.forEach(comment => {
                const commentHtml = `
                    <div class="comment">
                        <p class="comment-author">${comment.authorName} </p>
                        <p class="comment-text">${comment.text}</p>
                        ${
                            comment.authorName === localStorage.getItem('username') ? `
                            <button class="delete-comment-btn" data-comment-id="${comment.id}">Delete</button>
                            <button class="edit-comment-btn" data-comment-id="${comment.id}">Edit</button>
                        ` : ''
                        }
                    </div>
                `;
                commentsList.append(commentHtml);
            });
        })
        .fail(function() {
            commentsList.html('<p>Failed to load comments.</p>');
        });
}

// -------------------------- RENDER FUNCTIONS ---------------------------------

export function createPostCardHtml(post) {
    let tagsHtml = '';
    if (post.tags.length > 0) {
        const tagsList = post.tags.map(tag => `<span class="post-tag">${tag}</span>`).join('');
        tagsHtml = `<div class="post-tags-container">${tagsList}</div>`;
    }

    return `
        <div class="post-card">
            <h2 class="post-card-title">${post.title}</h2>
            <p class="post-meta">By ${post.authorName} on ${new Date(post.createdAt).toLocaleDateString()}</p>
            ${tagsHtml}
            <p class="post-card-content">${post.content.substring(0, 200)}...</p>
            <a href="#post/${post.id}" class="read-more-link">Read More</a>
        </div>`;
}

export function renderHomePage(currentPage) {
    showSpinner();
    postsContainer.empty();
    $('#load-more-container').remove();

    apiService.getPosts(currentPage - 1)
        .done(function(response) {
            const posts = response.content;
        
            if(posts.length === 0 && currentPage === 1){
                postsContainer.html('<p>No posts found.</p>');
                return;
            }
            posts.forEach(function(post){
                const postHtml = createPostCardHtml(post);
                postsContainer.append(postHtml);
            });
            // if this not the last page show the load more btn
            if (currentPage < response.page.totalPages) {
                const loadMoreHtml = `
                <div id="load-more-container">
                    <button id="load-more-btn">Load More</button>
                </div>`;
                $('main').append(loadMoreHtml);
            }
        })
        .fail(function(error) {
            console.error('Error fetching posts:',error);
            postsContainer.html('<p>Error loading posts. Is the backend server running?</p>');
        })
        .always(function() {
            hideSpinner();
        });
}

export function renderSinglePostPage(postId) {
    showSpinner();
    let tagsHtml = '';
    
    apiService.getPost(postId)
        .done(function(post) {
            postsContainer.empty(); // clear the main page

            if (post.tags.length > 0) {
                const tagsList = post.tags.map(tag => `<span class="post-tag">${tag}</span>`).join('');
                tagsHtml = `<div class="post-tags-container">${tagsList}</div>`;
            }
            const singlePostHtml = `
                <div class="single-post">
                    <a href="#" id="back-to-posts"> &larr;Back</a>
                    <h1>${post.title}</h1>
                    <p class="post-meta"> By ${post.authorName} on ${new Date(post.createdAt).toLocaleDateString()}</p>
                    ${tagsHtml}
                    <div class="post-full-content">${post.content}</div>

                    <!-- Comments Section -->
                    <div id="comments-section">
                        <h3>Comments</h3>
                        <div id="comments-list">Loading comments...</div>
                        ${localStorage.getItem('accessToken') ? `
                            <form id="comment-form" novalidate>
                                <textarea id="comment-content" placeholder="Write a comment..." rows="3" required></textarea>
                                <div class="form-error" id="comment-content-error"></div>
                                <button type="submit">Post Comment</button>
                            </form>
                        ` : ``}
                    </div>
                </div>
            `;
            postsContainer.html(singlePostHtml);
            // Convert markdown to HTML for display
            $('.post-full-content').html(marked.parse(post.content));
            loadComments(postId);
        })
        .fail(function(error) {
            console.error('Error fetching single post:',error);
            postsContainer.html('<p>Error loading post.</p>');
        })
        .always(function() {
            hideSpinner();
        });
}

export function renderLoginPage() {
    postsContainer.empty();
    const loginHtml = `
    <div id="login-form-container">
        <form id="login-form" novalidate>
            <h3>Login</h3>
            <input type="text" id="username" placeholder="Username" required>
            <div class="form-error" id="login-username-error"></div>

            <input type="password" id="password" placeholder="Password" required>
            <div class="form-error" id="login-password-error"></div>

            <button type="submit">Login</button>
        </form>
    </div>`;
    postsContainer.html(loginHtml);
}

export function renderSignupPage() {
    postsContainer.empty();
    const signupHtml = `
    <div id="signup-form-container">
        <form id="signup-form" novalidate>
            <h3>Signup</h3>
            <input type="text" id="signup-username" placeholder="Username" required>
            <div class="form-error" id="signup-username-error"></div>

            <input type="email" id="signup-email" placeholder="Email" required>
            <div class="form-error" id="signup-email-error"></div>

            <input type="password" id="signup-password" placeholder="Password" required>
            <div class="form-error" id="signup-password-error"></div>

            <input type="password" id="signup-confirm-password" placeholder="Confirm Password" required>
            <div class="form-error" id="signup-confirm-password-error"></div>

            <button type="submit">Register</button>
        </form>
    </div>`;
    postsContainer.html(signupHtml);
}

export function renderProfilePage() {
    if (!localStorage.getItem('accessToken')) {
        window.location.hash = '#login';
        return;
    }

    showSpinner();
    postsContainer.empty();

    apiService.getProfile(localStorage.getItem('username'))
        .done(function(userProfile) {
            const profileHtml = `
            <div class="profile-card"> 
                <h2>User profile</h2>
                <a href="#profile/edit" class="edit-profile-btn">Edit Profile</a>
                <p><strong>Username:</strong> ${userProfile.username}</p>
                <p><strong>Email:</strong> ${userProfile.email}</p>
                <p><strong>Bio:</strong> ${userProfile.bio || 'Not set'}</p>
                <p><strong>Social:</strong> ${userProfile.website || 'Not set'}
            </div>`;
            postsContainer.html(profileHtml);
        })
        .fail(function() {
            postsContainer.html('<p>Could not load user profile. Your session may have expired.</p>');
        })
        .always(function() {
            hideSpinner();
        });
}

export function renderMyPostsPage() {
    if (!localStorage.getItem('accessToken')) {
        window.location.hash = '#login';
        return;
    }
    
    showSpinner();
    postsContainer.empty().html('<p>Loading your posts...</p>');

    apiService.getMyPosts()
        .done(function(response) {
            postsContainer.empty();
            const posts = response.content;

            if(posts.length === 0) {
                postsContainer.html('<p>You have not created any posts yet.</p>');
                return;
            }
            posts.forEach(function(post) {
                const editDeleteButtons = `
                    <button class="edit-post-btn" data-post-id="${post.id}" title="Edit post">
                        <i class="fa-solid fa-pen"></i>
                    </button>
                    <button class="delete-post-btn" data-post-id="${post.id}" title="Delete post">
                        <i class="fa-solid fa-trash"></i>
                    </button>`;

                const postHtml = `
                    <div class="my-posts-post-card">
                        <a href="#post/${post.id}" class="my-posts-title-link">${post.title}</a>
                        <div class="my-posts-post-actions">${editDeleteButtons}</div>
                    </div>`;

                postsContainer.append(postHtml);
            });
        })
        .fail(function() {
            postsContainer.html('<p>Could not load your posts.</p>');
        })
        .always(function() {
            hideSpinner();
        });
}

export function renderSearchResultsPage(query) {
    showSpinner();
    postsContainer.empty();
    $('#load-more-container').remove();

    const titleHtml = `<h2>Search results for: <span style="color: var(--creamy-red);">${query}</span></h2><hr class="header-hr">`;
    postsContainer.html(titleHtml);

    apiService.searchPosts(query)
        .done(function(response) {
            const posts = response.content;
            if (posts.length === 0) {
                postsContainer.append('<p>No posts found matching your search.</p>');
                return;
            }
            posts.forEach(function(post) {
                const postHtml = createPostCardHtml(post);
                postsContainer.append(postHtml);
            });
        })
        .fail(function() {
            postsContainer.append('<p>There was an error performing the search.</p>');
        })
        .always(function() {
            hideSpinner();
        });
}


export function renderPostFormPage(postId) {
    if (!localStorage.getItem('accessToken')) {
        window.location.hash = '#login';
        return;
    }

    // Cleanup previous EasyMDE instance if it exists
    if (easyMDEInstance) {
        easyMDEInstance.toTextArea();
        easyMDEInstance = null;
    }

    postsContainer.empty();

    const isEditing = postId !== null;
    const formTitle = isEditing ? 'Edit Post' : 'Create a New Post';
    const buttonText = isEditing ? 'Update Post' : 'Publish Post';

    const postFormHtml = 
    `<div class="create-post-container">
        <h2>${formTitle}</h2>

        <form id="create-post-form" novalidate>
            <input type="hidden" id="post-id">

            <label for="post-title">Title</label>
            <input type="text" id="post-title" placeholder="Enter post title..." required>
            <div class="form-error" id="post-title-error"></div>

            <label for="post-content">Content</label>
            <textarea id="post-content"></textarea>
            <div class="form-error" id="post-content-error"></div>

            <input type="text" id="post-tags" placeholder="Tags with comma-separated">

            <button type="submit" class="btn-submit-post">${buttonText}</button>
        </form>
    </div>
    `;
    postsContainer.html(postFormHtml);

    // Initialize EasyMDE on the new textarea
    easyMDEInstance = new EasyMDE({
        element: document.getElementById("post-content"),
        spellChecker: false,
        placeholder: "Write your post content here...",
        status: false,
        toolbar: [
            "bold", "italic", "heading", "|",
            "quote", "unordered-list", "ordered-list", "|",
            "link", "image", "|",
            "preview", "side-by-side", "fullscreen"
        ]
    });

    // If we are editing, fetch the post and populate the form
    if (isEditing) {
        showSpinner();
        apiService.getPost(postId)
            .done(function(post) {
                $('#post-id').val(post.id);
                $('#post-title').val(post.title);
                // Use the EasyMDE API to set the content
                easyMDEInstance.value(post.content); 
                const tags = Array.isArray(post.tags) ? post.tags.join(', ') : '';
                $('#post-tags').val(tags);
            })
            .fail(function() { 
                showNotification('Could not fetch post details.', 'error');
                postsContainer.html('<p>Failed to load post for editing.</p>');
            })
            .always(function() {
                hideSpinner();
            });
    }
}

export function renderEditProfilePage() {
    if (!localStorage.getItem('accessToken')) {
        window.location.hash = '#login';
        return;
    }

    showSpinner();
    const username = localStorage.getItem('username');

    apiService.getProfile(username)
        .done(function(userProfile) {
            const editProfileHtml = `
            <div id="edit-profile-form-container">
                <form id="edit-profile-form" novalidate>
                    <h3>Edit Your Profile</h3>

                    <label for="profile-email">Email</label>
                    <input type="email" id="profile-email" value="${userProfile.email || ''}" required>
                    <div class="form-error" id="profile-email-error"></div>

                    <label for="profile-bio">Bio</label>
                    <textarea id="profile-bio" rows="4">${userProfile.bio || ''}</textarea>
                    <div class="form-error" id="profile-bio-error"></div>

                    <label for="profile-website">Website</label>
                    <input type="url" id="profile-website" value="${userProfile.website || ''}">
                    <div class="form-error" id="profile-website-error"></div>

                    <button type="submit">Save Changes</button>
                    <a href="#profile" class="cancel-link">Cancel</a>
                </form>
            </div>
            `;
            postsContainer.html(editProfileHtml);
        })
        .fail(function() {
            postsContainer.html('<p>Could not load your profile to edit.</p>');
        })
        .always(function() {
            hideSpinner();
        });
}

// --- Event Listeners for UI-related forms ---

export function initPostFormListener() {
    postsContainer.on('submit','#create-post-form', function(event) {
        event.preventDefault();

        // Clear previous errors
        $('.form-error').text('');
        $('#create-post-form input, #create-post-form textarea').removeClass('input-error');

        const title = $('#post-title').val().trim();
        const content = easyMDEInstance.value().trim(); // Get content from EasyMDE

        let isValid = true;
        if (title === '') {
            isValid = false;
            $('#post-title-error').text('Title is required.');
            $('#post-title').addClass('input-error');
        }
        if (content === '') {
            isValid = false;
            $('#post-content-error').text('Content is required.');
            // We don't add error class to the MDE editor itself
        }

        if (!isValid) {
            return;
        }

        const btn = $(this).find('button[type="submit"]');
        btn.prop('disabled', true).text('Publishing...');

        const postId = $('#post-id').val();
        const tags = $('#post-tags').val().split(',').map(tag => tag.trim()).filter(tag => tag);
        
        const requestPayload = { title, content, tags };
        const isEditing = !!postId;

        const apiCall = isEditing ? apiService.updatePost(postId, requestPayload) : apiService.createPost(requestPayload);

        apiCall.done(function() {
            showNotification(`Post ${isEditing ? 'Updated' : 'Created'} successfully.`, 'success');
            window.location.hash = '';
        }).fail(function(error) {
            showNotification(`Error: Could not ${isEditing ? 'update' : 'create'} post.`, 'error');
            console.error(`Error post:`, error);
            btn.prop('disabled', false).text(isEditing ? 'Update Post' : 'Publish Post');
        });
    });
}
