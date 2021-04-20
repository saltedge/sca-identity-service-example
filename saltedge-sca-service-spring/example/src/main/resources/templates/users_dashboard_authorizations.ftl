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
            <a href="/users/dashboard/connections?user_id=${user.id}">Connections</a>
            <a href="/users/dashboard/consents?user_id=${user.id}">Consents</a>
            <a href="/users/sign_in">Sign Out</a>
        </div>

        <div class="main">
            <h2>Authorizations (Transactions)</h2>

            <form method="post" action="/users/dashboard/authorizations/create">
                <input type="hidden" name="user_id" value="${user.id}">
                <input type="submit" value="Create new">
            </form>

            <br>

            <#list authorizations>
                <table border="1">
                    <tr>
                       <th>ID</th>
                       <th>Title</th>
                       <th>CreatedAt -> ExpiresAt</th>
                       <th>Status</th>
                       <th>Confirmed with</th>
                       <th>Location</th>
                    </tr>
                <#items as item>
                    <tr>
                        <td><a href="/authorizations/${item.id}">${item.id}</a></td>
                        <td>${item.getTitle()}</td>
                        <td>${item.getCreatedAt().toString()}<br>${item.getExpiresAt().toString()}</td>
                        <td>${item.getStatus()}</td>
                        <td>${item.getConfirmAuthorizationType()}</td>
                        <td>${item.getConfirmLocation()}</td>
                    </tr>
                </#items>
            </table>
            <#else>
                <p>Not registered</p>
            </#list>
        </div>
    </div>
</body>
</html>