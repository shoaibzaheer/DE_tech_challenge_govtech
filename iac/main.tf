provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "image_processing_rg" {
  name     = "image-processing-rg"
  location = "Southeast Asia"
}

resource "azurerm_storage_account" "image_storage_account" {
  name                     = "imagestorageaccount"
  resource_group_name      = azurerm_resource_group.image_processing_rg.name
  location                 = azurerm_resource_group.image_processing_rg.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
}

resource "azurerm_storage_container" "image_container" {
  name                  = "imagecontainer"
  storage_account_name  = azurerm_storage_account.image_storage_account.name
  container_access_type = "private"
}

resource "azurerm_storage_blob" "image_blob" {
  name                   = "imageblob"
  storage_account_name   = azurerm_storage_account.image_storage_account.name
  storage_container_name = azurerm_storage_container.image_container.name
  type                   = "Block"
}

resource "azurerm_kubernetes_cluster" "aks" {
  name                = "my-aks-cluster"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
  dns_prefix          = "myakscluster"

  default_node_pool {
    name            = "default"
    node_count      = 3
    vm_size         = "Standard_DS2_v2"
    vnet_subnet_id  = azurerm_subnet.subnet.id
    os_disk_size_gb = 30
  }

  service_principal {
    client_id     = var.client_id
    client_secret = var.client_secret
  }

  tags = {
    Environment = "dev"
  }
}

resource "azurerm_container_registry" "acr" {
  name                     = "mycontainerregistry"
  location                 = azurerm_resource_group.rg.location
  resource_group_name      = azurerm_resource_group.rg.name
  sku                      = "Basic"
  admin_enabled            = true
  georeplication_locations = ["eastus2", "westus"]
}

resource "azurerm_kubernetes_service" "processing-api" {
  depends_on = [
    azurerm_kubernetes_cluster.aks,
  ]

  name                = "processing-api"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
  depends_on          = [azurerm_kubernetes_cluster.aks]

  docker_image_reference = "${azurerm_container_registry.acr.login_server}/myimage:v1"

  port {
    name       = "http"
    port       = 80
    target_port = 80
  }

  tag {
    name       = "v1"
    properties = {
      transform_zip = base64encode(file("${path.module}/transform.zip"))
    }
  }
}
resource "azurerm_databricks_workspace" "databricks" {
  name                = "image-processing-databricks"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
  sku_version         = "premium"
}

resource "azurerm_databricks_cluster" "cluster" {
  name                      = "image-processing-cluster"
  location                  = azurerm_resource_group.rg.location
  resource_group_name       = azurerm_resource_group.rg.name
  databricks_workspace_name = azurerm_databricks_workspace.databricks.name
  node_type_id              = "Standard_DS3_v2"
  num_workers               = 3

  # other cluster configuration options
}
