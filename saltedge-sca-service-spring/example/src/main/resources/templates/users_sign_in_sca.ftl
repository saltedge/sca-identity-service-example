<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.w3.org/1999/xhtml">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Sign In User</title>

    <#include "users_sign_in_qr.css">
</head>
<body>
    <h1 class="top-header">Sign In User</h1>

    <div class="container">
        <div class="centered">
            <form action="/users/sign_in_sca?secret=${secret}" method="post">
                <p>Username: <input type="text" name="username">
                <p>Password: <input type="password" name="password">
                <p><input type="submit" value="Sign In" style="font-size:20px">
            </form>

            <#if error??>
                <h3 style="color:red;">${error}</h3>
            </#if>
        </div>
    </div>
</body>
</html>