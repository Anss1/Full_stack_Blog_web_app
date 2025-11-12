// HELPER METHODS

export function getUsernameFromToken(authToken) {
    if(!authToken) return null;
    try{
        // the token is in three parts separated by dots the middle one is the payload
        const payload = JSON.parse(atob(authToken.split('.')[1]));
        return payload.sub; // claim for subject (username)
    } catch(e) {
        return null;
    }
}

export function showNotification(message, type = 'success') {
    const container = $('#notification-container');

    const notification = $('<div></div>')
        .addClass('notification')
        .addClass(type)
        .text(message);
    
    container.append(notification);

    setTimeout(() => {
        notification.fadeOut(500, function() {
            $(this).remove();
        });
    }, 3000); // disappear after 3 sec
}

export function showConfirmationModal(message, onConfirmCallback) {
    const overlay = $('#modal-overlay');

    $('#modal-message').text(message);

    overlay.removeClass('hidden'); // show the modal

    $('#modal-btn-confirm').one('click', function() {
        overlay.addClass('hidden');
        onConfirmCallback(); // then execute the action after confirm
    });
    $('#modal-btn-cancel').one('click', function() {
        overlay.addClass('hidden');
    });
}

export function showSpinner() {
    $('#spinner-overlay').removeClass('hidden');
}

export function hideSpinner() {
    $('#spinner-overlay').addClass('hidden');
}
