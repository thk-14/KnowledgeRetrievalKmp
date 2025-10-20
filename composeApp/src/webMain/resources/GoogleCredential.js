var googleSignInFinishCallback = (googleCredentialPayload) => {
    console.error("googleSignInFinishCallback not set!");
};

var googleSignOutFinishCallback = () => {
    console.error("googleSignOutFinishCallback not set!");
};

window.setGoogleSignInFinishCallback = (callback) => {
    googleSignInFinishCallback = callback;
    console.log("Set googleSignInFinishCallback");
};

window.setGoogleSignOutFinishCallback = (callback) => {
    googleSignOutFinishCallback = callback;
    console.log("Set googleSignOutFinishCallback");
};

function initializeGoogleSignIn(clientId) {
    try {
        if (typeof google === 'undefined' || !google.accounts || !google.accounts.id) {
            console.error("Google Identity Services library not loaded yet");
            // retry after a delay
            setTimeout(() => initializeGoogleSignIn(clientId), 1000);
            return;
        }

        google.accounts.id.initialize({
            client_id: clientId,
            callback: handleCredentialResponse,
            auto_select: false,
            use_fedcm_for_prompt: true
        });
        console.log("Google Identity Services Initialized");
    } catch (error) {
        console.error("Error initializing Google Sign In", error);
    }
}

function handleCredentialResponse(response) {
    console.log(`Received credential response: ${JSON.stringify(response, null, 2)}`);
    let googleCredentialPayload = parseResponseCredential(response.credential)
    if (googleCredentialPayload == null) return
    if (googleSignInFinishCallback && typeof googleSignInFinishCallback === 'function') {
        googleSignInFinishCallback(googleCredentialPayload);
    } else {
        console.error("googleSignInFinishCallback is not a function or not set!");
    }
}

function parseResponseCredential(responseCredential) {
    try {
        // 1. Split the token into parts (header, payload, signature)
        const parts = responseCredential.split('.');
        if (parts.length !== 3) {
            throw new Error("Invalid JWT: Does not contain 3 parts.");
        }
        const payloadBase64Url = parts[1];
        // Convert Base64Url to Base64
        // Replace '-' with '+' and '_' with '/'
        const payloadBase64 = payloadBase64Url.replace(/-/g, '+').replace(/_/g, '/');
        // Decode Base64 string
        const decodedPayloadJson = atob(payloadBase64);
        // Parse the JSON string
        const payload = JSON.parse(decodedPayloadJson);
        return {
            email: payload.email,
            emailVerified: payload.email_verified,
            familyName: payload.family_name,
            givenName: payload.given_name,
            name: payload.name,
            picture: payload.picture,
            token: responseCredential
        }
    } catch (error) {
        console.error("Error decoding JWT payload:", error);
        return null
    }
}

function triggerGoogleSignIn() {
    try {
        if (typeof google === 'undefined' || !google.accounts || !google.accounts.id) {
            console.error("Google library not ready to trigger prompt.");
            return;
        }
        console.log("Triggering Google Sign-In prompt ...");
        google.accounts.id.prompt((notification) => {
            console.log("Google prompt notification:", notification);
            if (notification.isNotDisplayed() || notification.isSkippedMoment()) {
                console.warn("Google prompt was not displayed or was skipped. Reason:",
                    notification.getNotDisplayedReason(), notification.getSkippedReason());
            } else if (notification.isDismissedMoment()) {
                console.log("Google prompt was dismissed by the user. Reason:", notification.getDismissedReason());
            }
            if (notification.getSkippedReason() === 'tap_outside') {
                google.accounts.id.prompt();
            }
        });
    } catch (error) {
        console.error("Error triggering Google prompt:", error);
    }
}

function triggerGoogleSignOut() {
    try {
        if (typeof google === 'undefined' || !google.accounts || !google.accounts.id) {
            console.error("Google library not ready for sign out.");
            return;
        }
        google.accounts.id.disableAutoSelect();
        console.log("Google auto sign-in disabled.");
        if (googleSignOutFinishCallback && typeof googleSignOutFinishCallback === 'function') {
            googleSignOutFinishCallback();
        } else {
            console.error("googleSignOutFinishCallback is not a function or not set!");
        }
    } catch (error) {
        console.error("Error during Google sign out:", error);
        if (googleSignOutFinishCallback && typeof googleSignOutFinishCallback === 'function') {
            googleSignOutFinishCallback();
        }
    }
}