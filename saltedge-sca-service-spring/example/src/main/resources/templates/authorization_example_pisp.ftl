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
          display: table-row;
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
          right: 0;
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
<font class="first">Payee:</font><font class="last">${payee_name}</font>
</div>
<div class="row"> </div>
<div class="row">
<font class="first">Amount:</font><font class="last">${amount}</font>
</div>
<div class="row"> </div>
<div class="row">
<font class="first">Your Account:</font><font class="last">${from_account}</font>
</div>
<div class="row"> </div>
<div class="row">
<font class="first">Payee Account:</font><font class="last">${to_account}</font>
</div>
<div class="row"> </div>
<div class="row">
<font class="first">Reference:</font><font class="last">${payment_description}​ AT ${date}</font>
</div>
</div></body>
</html>