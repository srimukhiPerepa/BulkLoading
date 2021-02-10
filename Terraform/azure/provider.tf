variable "tenancy_ocid" {}
variable "user_ocid" {}
variable "api_fingerprint" {}
variable "private_key_path" {}
variable "region" {default="us-ashburn-1"}

provider "oci" {
  version              = ">= 3.14"
  region               = "${var.region}"
}
