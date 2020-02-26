<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Add SCA</title>

    <#include "users_sign_in_qr.css">
</head>
<body>
    <h1 class="top-header">Add Salt Edge Authenticator</h1>
    <div class="container">
        <div class="centered">
            <h3 style="text-align:center;">Scan QR with Authenticator App</h3>

            <img class="qr_img" src="${qr_img_src}" width="256" height="256" align="middle">

            <a href="${authenticator_link}"><h3 style="text-align:center;">or OPEN with Authenticator App</h3></a>

            <div>
                <a href='https://play.google.com/store/apps/details?id=com.saltedge.authenticator'>
                    <img src='https://cdn.worldvectorlogo.com/logos/google-play-badge.svg' alt='Get it on Google Play' height="44px"/>
                </a>
                <a href='https://apps.apple.com/md/app/priora-authenticator/id1277625653'>
                    <img src='https://cdn.worldvectorlogo.com/logos/download-on-the-app-store-apple.svg' alt='Get it on App Store' height="44px"/>
                </a>
            </div>
        </div>

        <div>
            <a href="/users/sign_in"><h3>Back to Sign In</h3></a>
        </div>
    </div>
</body>
</html>