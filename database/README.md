# DE_tech_challenge_govtech

1. Which are the top 10 members based on their total spending?
````
SELECT Members.name, SUM(Transactions.total_price) AS total_spending
FROM Members
JOIN Transactions ON Members.member_id = Transactions.member_id
GROUP BY Members.name
ORDER BY total_spending DESC
LIMIT 10;
````

2. Which are the top 3 items that are frequently bought by members?

````
SELECT Items.name, SUM(TransactionItems.quantity) AS total_quantity
FROM Items
JOIN TransactionItems ON Items.item_id = TransactionItems.item_id
GROUP BY Items.name
ORDER BY total_quantity DESC
LIMIT 3;


````