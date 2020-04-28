<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.w3.org/1999/xhtml">
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Dashboard</title>
    <#include "users_dashboard.css">
</head>
<body>
    <h1 class="top-header">User Dashboard (${user.name})</h1>
    <div class="container">

        <!-- Side navigation -->
        <div class="sidenav">
            <a href="/users/dashboard/authorizations?user_id=${user.id}">Authorizations</a>
            <a href="/users/dashboard/consents?user_id=${user.id}">Consents</a>
            <a href="/users/sign_in">Sign Out</a>
        </div>

        <div class="main">
            <h2>Connections (Authenticators)</h2>

            <button id="connectQrButton">Create new</button>
            <br><br>

            <#list connections>
                <table border="1">
                    <tr>
                       <th>ID</th>
                       <th>platform</th>
                       <th>access_token</th>
                       <th>[X]</th>
                    </tr>
                <#items as item>
                    <tr>
                        <td>${item.id}</td>
                        <td>${item.platform}</td>
                        <td>${item.accessToken}</td>
                        <td>
                            <#if item.revoked>
                                <b>R</b>
                            <#else>
                                <form method="post" action="/users/dashboard/connections/revoke">
                                    <input type="hidden" name="user_id" value="${user.id}">
                                    <input type="hidden" name="connection_id" value="${item.id}">
                                    <input class='btn btn-primary' type='submit' value='X'>
                                </form>
                            </#if>
                        </td>
                    </tr>
                </#items>
                </table>
            <#else>
                <p>Not registered</p>
            </#list>

            <!-- The Modal -->
            <div id="connectQrModal" class="modal">
                <!-- Modal content -->
                <div class="modal-content">
                    <span class="close">&times;</span>

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
            </div>
        </div>
    </div>
    <script>
        // Get the modal
        var modal = document.getElementById("connectQrModal");

        // Get the button that opens the modal
        var btn = document.getElementById("connectQrButton");

        // Get the <span> element that closes the modal
        var span = document.getElementsByClassName("close")[0];

        // When the user clicks the button, open the modal
        btn.onclick = function() {
          modal.style.display = "block";
        }

        // When the user clicks on <span> (x), close the modal
        span.onclick = function() {
          modal.style.display = "none";
        }

        // When the user clicks anywhere outside of the modal, close it
        window.onclick = function(event) {
          if (event.target == modal) {
            modal.style.display = "none";
          }
        }
    </script>
</body>
</html>