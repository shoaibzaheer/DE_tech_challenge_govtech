# Infrastructure As a Code

To deploy  Terraform script, follow these steps:

1. Install Terraform: https://www.terraform.io/downloads.html
2. copy paste main.tf at desired directory.
3. Modify the values of the variables client_id and client_secret with your own Azure AD application credentials, or create environment variables ARM_CLIENT_ID and ARM_CLIENT_SECRET.
Run the following commands in your terminal:
````
   % terraform init
   % terraform plan
   % terraform apply
````

