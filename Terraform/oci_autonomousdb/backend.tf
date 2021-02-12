terraform {
  backend "consul" {
    path    = "terraform/state/flex/oci/db/autonomous/demo1"
	address = "teplt01.flexagon:8500"
    scheme  = "http"
    lock = true
  }
}