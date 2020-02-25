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

        <div>
            <a href="/users/sign_in"><h3>Back to Sign In</h3></a>
        </div>
    </div>
</body>
</html>