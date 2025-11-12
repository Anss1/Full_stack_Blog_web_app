// auth.js

import { apiService } from './api.js';
import { getUsernameFromToken, showNotification } from './utils.js';
import { postsContainer } from './ui.js';

export function checkLoginState() {
    const accessToken = localStorage.getItem('accessToken');
    const username = localStorage.getItem('username');

    // if user loggedIn
    if(accessToken && username) {
        $('#login-button').addClass('hidden');
        $('#signup-button').addClass('hidden');

        $('#profile-button').removeClass('hidden');
        $('#my-post-link').removeClass('hidden');
        $('#logout-button').removeClass('hidden');
        
        $('#create-post-button').removeClass('hidden');
    } else {
        $('#login-button').removeClass('hidden');
        $('#signup-button').removeClass('hidden');

        $('#profile-button').addClass('hidden');
        $('#my-post-link').addClass('hidden');
        $('#logout-button').addClass('hidden');
        
        $('#create-post-button').addClass('hidden');
    }
}

export function initAuthEventListeners() {
    // Login form
    postsContainer.on('submit', '#login-form', function(event) {
        event.preventDefault();

        // Clear previous errors
        $('.form-error').text('');
        $('#login-form input').removeClass('input-error');

        const username = $('#username').val().trim();
        const password = $('#password').val();

        let isValid = true;
        if (username === '') {
            isValid = false;
            $('#login-username-error').text('Username is required.');
            $('#username').addClass('input-error');
        }
        if (password === '') {
            isValid = false;
            $('#login-password-error').text('Password is required.');
            $('#password').addClass('input-error');
        }

        if (!isValid) {
            return;
        }

        apiService.login(username, password)
            .done(function(response) {
                localStorage.setItem('accessToken', response.accessToken);
                localStorage.setItem('refreshToken', response.refreshToken);
                localStorage.setItem('username', getUsernameFromToken(response.accessToken));

                showNotification(`Welcome, ${localStorage.getItem('username')} :)`, 'success');
                window.location.hash = '';
            })
            .fail(function() {
                showNotification('Please check your username or password.', 'error');
                
                $('#username').addClass('input-error');
                $('#password').addClass('input-error');
            });
    });

    // Signup form
    postsContainer.on('submit', '#signup-form', function(event) {
        event.preventDefault();

        // Clear previous errors
        $('.form-error').text('');
        $('#signup-form input').removeClass('input-error');

        // Get values
        const username = $('#signup-username').val().trim();
        const email = $('#signup-email').val().trim();
        const password = $('#signup-password').val();
        const confirmPassword = $('#signup-confirm-password').val();

        let isValid = true;

        // --------- Validation Checks --------
        if (username === '') {
            isValid = false;
            $('#signup-username-error').text('Username is required.');
            $('#signup-username').addClass('input-error');
        }

        if (email === '') {
            isValid = false;
            $('#signup-email-error').text('Email is required.');
            $('#signup-email').addClass('input-error');
        } else if (!/^\S+@\S+\.\S+$/.test(email)) { 
            isValid = false;
            $('#signup-email-error').text('Please enter a valid email address.');
            $('#signup-email').addClass('input-error');
        }

        if (password.length < 6) {
            isValid = false;
            $('#signup-password-error').text('Password must be at least 8 characters long.');
            $('#signup-password').addClass('input-error');
        }

        if (password !== confirmPassword) {
            isValid = false;
            $('#signup-confirm-password-error').text('Passwords do not match.');
            $('#signup-confirm-password').addClass('input-error');
        }

        if (!isValid) {
            return;
        }

        // If validation passes, proceed with API call
        apiService.signup(username, email, password, confirmPassword)
            .done(function() {
                showNotification('Our family just got a little bigger! Please log in.', 'success');
                window.location.hash = '#login';
            })
            .fail(function(error) {
                const errorMessage = error.responseJSON ? error.responseJSON.message : 'Unknown error occurred.';
                showNotification(errorMessage, 'error');
            });
    });

    // Logout button
    $('#logout-button').on('click', function() {
        const refreshToken = localStorage.getItem('refreshToken');
        apiService.logout(refreshToken)
            .always(function() {
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
                localStorage.removeItem('username');

                checkLoginState();
                window.location.hash = '';
                showNotification('We will miss you. ):', 'success');
            });
    });

    // Global AJAX error handler for token refresh
    let isRefreshing = false;
    $(document).ajaxError(function(event, jqXHR, ajaxSettings, thrownError) {
        if (jqXHR.status === 401 || jqXHR.status === 403) {
            const refreshToken = localStorage.getItem('refreshToken');

            if (isRefreshing || ajaxSettings.url.includes('/auth/refresh') || !refreshToken) {
                return;
            }

            isRefreshing = true;
            console.log('Access token may be expired. Attempting to refresh...');

            apiService.refresh(refreshToken)
                .done(function(response) {
                    console.log('Token refreshed successfully');
                    localStorage.setItem('accessToken', response.accessToken);
                    localStorage.setItem('refreshToken', response.refreshToken);
                    // Retry the failed request
                    $.ajax({
                        ...ajaxSetting,
                        headers: {
                            ...ajaxSetting.headers,
                            'Authorization': 'Bearer ' + response.accessToken
                        }
                    });
                })
                .fail(function() {
                    console.log('Refresh token is invalid or expired. Logging out.');
                    apiService.logout(refreshToken).always(() => {
                        localStorage.removeItem('accessToken');
                        localStorage.removeItem('refreshToken');
                        localStorage.removeItem('username');
                        checkLoginState();
                        window.location.hash = '#login';
                    });
                })
                .always(function() {
                    isRefreshing = false;
                });
        }
    });
}