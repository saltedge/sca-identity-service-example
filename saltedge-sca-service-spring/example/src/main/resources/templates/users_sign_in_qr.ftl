<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Salt Edge SCA Service Example</title>

    <script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo=" crossorigin="anonymous"></script>
    <#include "users_sign_in_qr.css">

    <script language="javascript">
        function refreshStatus() {
            var action_uuid = "${action_uuid}"
            var token = $('input[name="csrfToken"]').attr('value');

            $.ajax({
                url: "/sca/login/status?action_uuid=" + action_uuid,
                method: "get",
                dataType: "json",
                headers: {
                    'X-CSRF-Token': token
                },
                success: function(data) {
                    var status = data["status"]
                    var redirect = data["redirect"]

                    switch(status) {
                        case "expired":
                            document.getElementById("login_action").style.visibility = "hidden"
                            break
                        case "authenticated":
                            var link  = document.createElement('a');
                            link.href = redirect;
                            document.body.appendChild(link);
                            link.click();
                            break
                        default:
                            polling()
                            break
                    }
                },
                fail: function(data) {
                    console.log(data);  debugger;
                }
            })
        }

        function polling() {
            clearTimeout(poll)
            var poll = setTimeout(refreshStatus, 3000)
        }

        <#if action_uuid??>
            polling()
        </#if>
    </script>
</head>
<body>
    <h1 class="top-header">Salt Edge SCA Example Login</h1>
    <div class="container">
        <div class="wrapper">

            <div class="form-wrapper">
                <h3>Input credentials</h3>
                <form method="post" action="/users/sign_in">
                    <div class="input-wrapper">
                        <label for="username">Username</label>
                        <input type="text" id="username" name="username" class="form-input">
                    </div>

                    <div class="input-wrapper">
                        <label for="password">Password</label>
                        <input type="password" id="password" name="password" class="form-input">
                    </div>

                    <div class="with-margin centered">
                        <input type="submit" value="Login" style="font-size:20px">
                    </div>
                </form>

                <#if error??>
                    <h3 style="color:red;">${error}</h3>
                <#else>
                    <p></p>
                </#if>

                <div class="bottom-box">
                    <p>New user?</p>
                    <a href="/users/register"><h3>Register</h3></a>
                </div>
            </div>

            <#if show_sca_options?? && show_sca_options>
            <div>
                <h2>OR</h2>
            </div>

            <div id="sca_options" class="centered">
                <div id="login_action" class="qr-wrapper">
                    <h3>Instant Login with<br>Authenticator</h3>
                    <img class="qr_img" src="${qr_img_src}" width="256" height="256" align="middle">
                    <a href="${authenticator_link}"><h3 style="text-align:center;">Click to Instant Login</h3></a>
                </div>
                <div class="bottom-box">
                    <p>New Authenticator?</p>
                    <a href="/users/connect_sca"><h3>Connect</h3></a>
                </div>
            </div>
            </#if>

        </div>
    </div>
</body>
</html>