<html>
  <style type="text/css">
    html, body, .content {
      overflow-x: hidden;
      padding: 0;
      margin: 0;
    }
    :root {
      color-scheme: light dark;
      --description-color: black;
    }
    @media (prefers-color-scheme: dark) {
      :root {
        --description-color: white;
      }
    }
    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Roboto', 'Helvetica Neue', 'Helvetica', Arial, sans-serif;
      font-size: 15px;
      line-height: 20px;
      max-width: 100% !important;
      width: 100%;
      position: fixed;
    }
    h2 {
      font-size: 20px;
      font-weight: 400;
      line-height: 30px;
      text-align: center;
      margin-bottom: 13px;
    }
    .content {
      width: 100%;
      display: block;
      padding: 20px 5px 30px 5px;
      margin-left: auto;
      margin-right: auto;
    }
    .row {
      margin: 15px 0;
      width: 100% !important;
      display: flex;
      font-family: -apple-system, BlinkMacSystemFont, 'Roboto', 'Helvetica Neue', 'Helvetica', Arial, sans-serif;
      font-size: 15px;
      line-height: 20px;
      text-align: left !important;
      position: relative;
      height: 15px;
    }
    .first {
      display: table-cell;
      position: absolute;
      left: 0;
      color: #9d9d9d;
      width: 35%;
      -webkit-box-sizing: border-box ;
      -moz-box-sizing: border-box ;
      box-sizing: border-box ;
    }
    .last {
      color: var(--description-color);
      display: table-cell;
      position: absolute;
      right: 10px;
      text-align: right;
      width: 65%;
      -webkit-box-sizing: border-box ;
      -moz-box-sizing: border-box ;
      box-sizing: border-box ;
    }
  </style>
<head><meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no"></head>
<body><div class="content">
<h2>Pay ${amount}</h2>
<div class="row">
<div class="first">Payee:</div><div class="last">${payee_name}</div>
</div>
<div class="row"> </div>
<div class="row">
<div class="first">Amount:</div><div class="last">${amount}</div>
</div>
<div class="row"> </div>
<div class="row">
<div class="first">Your Account:</div><div class="last">${from_account}</div>
</div>
<div class="row"> </div>
<div class="row">
<div class="first">Payee Account:</div><div class="last">${to_account}</div>
</div>
<div class="row"> </div>
<div class="row">
<div class="first">Reference:</div><div class="last">${payment_description}</div>
</div>
</div>
</body>
</html>
