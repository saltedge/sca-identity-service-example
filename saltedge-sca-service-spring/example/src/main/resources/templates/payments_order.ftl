<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Salt Edge SCA Service Example</title>

    <script src="https://code.jquery.com/jquery-3.4.1.min.js" integrity="sha256-CSXorXvZcTkaix6Yvo6HppcZGetbYMGWSFlBw8HfCJo=" crossorigin="anonymous"></script>
    <#include "users_sign_in_qr.css">

    <script language="javascript">
        function refreshStatus() {
            var payment_uuid = "${payment.uuid}"
            var token = $('input[name="csrfToken"]').attr('value');

            $.ajax({
                url: "/sca/payment/status?payment_uuid=" + payment_uuid,
                method: "get",
                dataType: "json",
                headers: {
                    'X-CSRF-Token': token
                },
                success: function(data) {
                    var status = data["status"]
                    var userName = data["user_name"]
                    var showAuth = data["show_auth"]

                    document.getElementById("payment-status").textContent = status
                    document.getElementById("payment-user").textContent = userName
                    if (showAuth) {
                        document.getElementById("auth_block").style.visibility = "visible"
                    } else {
                        document.getElementById("auth_block").style.visibility = "hidden"
                    }
                    polling()
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

        polling()
    </script>
</head>
<body>
    <h1 class="top-header">Payment Order Example</h1>
    <div class="container">
        <div>
            <a href="/payments/order?create_new=true"><h3 style="display: inline;">[Create new]</h3></a>

            <a href="/"><h3 style="display: inline;">[Index]</h3></a>

            <a href="/users/sign_in"><h3 style="display: inline;">[Dashboard]</h3></a>
        </div>
        <div style="border:1px solid gray;padding: 10px;">
            <p>Payee: ${payment.payeeName} ${payment.payeeAddress}
            <p>Amount: ${payment.amount} ${payment.currency}
            <p>User: <span id="payment-user">${userName}</span>
            <p>Status: <span id="payment-status">${payment.status}</span>
        </div>

        <div class="wrapper" id="auth_block">

            <div class="form-wrapper">
                <h3>Authorize with credentials</h3>
                <form method="post" action="/payments/sign_in">
                    <div class="input-wrapper">
                        <label for="username">Username</label>
                        <input type="text" id="username" name="username" class="form-input">
                    </div>

                    <div class="input-wrapper">
                        <label for="password">Password</label>
                        <input type="password" id="password" name="password" class="form-input">
                    </div>

                    <div class="with-margin centered">
                        <input type="hidden" name="payment_uuid" value="${payment.uuid}">
                        <input type="submit" value="Authorize" style="font-size:20px">
                    </div>
                </form>

                <#if error??>
                    <h3 style="color:red;">${error}</h3>
                <#else>
                    <p></p>
                </#if>
            </div>


            <div>
                <h2>OR</h2>
            </div>

            <div id="sca_options" class="centered">
                <div id="login_action" class="qr-wrapper">
                    <h3>Authorize with Authenticator</h3>
                    <img class="qr_img" src="${qr_img_src}" width="256" height="256" align="middle">
                    <a href="${authenticator_link}"><h3 style="text-align:center;">Click to Authorize</h3></a>
                </div>
            </div>

        </div>
    </div>
</body>
</html>