variable "tenancy_ocid" {}
variable "user_ocid" {}
variable "api_fingerprint" {}
variable "private_key_path" {}
variable "compartment_ocid" {default="ocid1.compartment.oc1..aaaaaaaanqjkbw76pcrpg24tcoroxgdi2ivtwcidkce4hjv2eksg2rgpo6da"}
variable "region" {default="us-ashburn-1"}

provider "oci" {
  version              = ">= 3.14"
  region               = "${var.region}"
}
