<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Register New User</title>

    <#include "users_sign_in_qr.css">
</head>
<body>
    <h1 class="top-header">Register New User</h1>

    <div class="container">
        <div class="centered">
            <form action="/users/register" method="post">
                <p>Username: <input type="text" name="username">
                <p>Password: <input type="password" name="password">
                <p><input type="submit" value="Register" style="font-size:20px">
            </form>

            <#if error??>
                <h3 style="color:red;">${error}</h3>
            </#if>
        </div>

        <a href="/"><h2 class="top-header">Back to the main page</h2></a>
        <a href="/users/sign_in"><h2 class="top-header">Go to Sign In</h2></a>
    </div>
</body>
</html>