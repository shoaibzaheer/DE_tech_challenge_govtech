# Charts & API

## Data Collection:
   1. Use Databricks notebook to run a Python/Scala that makes a GET request to the COVID-19 API for Singapore. 
   2. Extract the relevant data from the JSON using spark. 
   3. Store the data in a Delta  table using Databricks spark. 
   4. Schedule the script to run daily using Databricks Workflow.
## Data Transformation:
   1. Use Databricks SQL to transform the data into a format that can be easily visualized on the dashboard.
   2. Create a view or table that aggregates the data by date.
## Data Visualization:
   1. Use Databricks dashboard to create the COVID-19 cases graph.
   2. Connect to the Delta table using Databricks SQL endpoint.
   3. Create a visualization that updates automatically with new data from the table.
## Dashboard Hosting:
   1. Host the dashboard on Databricks Workspace.
   2. Use Databricks access control to ensure that the dashboard is accessible to the public.
## Dashboard Deployment:
   1. Configure the Databricks cluster to support the dashboard and its dependencies.
   2. Deploy the dashboard using Databricks dashboard API or UI.
## Dashboard Monitoring:
   1. Use Databricks monitoring tool to monitor the performance of the dashboard and its dependencies.
   2. Configure alerts and notifications for any issues or downtime.

With this pipeline, we can ensure low latency and high availability of the dashboard, while also allowing for future modifications and addition of new graphs and features.