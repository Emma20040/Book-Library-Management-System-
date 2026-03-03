<!DOCTYPE html>
<html lang="en">

<head>
    <title>Bank Statement</title>
    <style>
        body {
            margin: 20px 12rem;
        }

        .header {
            text-align: center;
            font-size: 24px;
            font-weight: bold;
        }

        .customer-info,
        .account-details,
        .transactions {
            margin-top: 20px;
            border-collapse: collapse;
            width: 100%;
        }

        .customer-info td,
        .account-details td,
        .transactions th,
        .transactions td {
            border: 1px solid #ddd;
            padding: 8px;
        }

        .transactions th {
            background-color: #f2f2f2;
        }
    </style>
</head>

<body>

<div class="header">Oval Ray Bank</div>

<!-- Customer Information -->
<table class="customer-info">
    <tr>
        <td><strong>Customer Name:</strong></td>
        <td>${customerName}</td>
    </tr>
    <tr>
        <td><strong>Account Number:</strong></td>
        <td>${accountNumber}</td>
    </tr>
    <tr>
        <td><strong>Email:</strong></td>
        <td>${email}</td>
    </tr>
    <tr>
        <td><strong>Phone Number:</strong></td>
        <td>${phoneNumber}</td>
    </tr>
    <tr>
        <td><strong>Address:</strong></td>
        <td>${address}</td>
    </tr>
</table>

<!-- Account Details -->
<h3>Account Summary</h3>
<table class="account-details">
    <tr>
        <td><strong>Account Type:</strong></td>
        <td>${accountType}</td>
    </tr>
    <tr>
        <td><strong>Balance:</strong></td>
        <td>${balance}</td>
    </tr>
    <tr>
        <td><strong>Branch:</strong></td>
        <td>${branch}</td>
    </tr>
</table>

<!-- Transaction History -->
<h3>Recent Transactions</h3>
<table class="transactions">
    <tr>
        <th>Date</th>
        <th>Description</th>
        <th>Amount</th>
        <th>Type</th>
        <th>Balance After Transaction</th>
    </tr>
    <#list transactions as transaction>
        <tr>
            <td>${transaction.date}</td>
            <td>${transaction.description}</td>
            <td>${transaction.amount}</td>
            <td>${transaction.type}</td>
            <td>${transaction.balanceAfter}</td>
        </tr>
    </#list>
</table>

<p style="text-align:center; margin-top:30px;">Thank you for banking with us!</p>

</body>

</html>
