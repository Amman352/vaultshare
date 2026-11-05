variable "region" {
  type    = string
  default = "us-east-1"
}

variable "vpc_id" {
  type = string
}

variable "subnet_id" {
  type = string
}

variable "expected_vpc_owner_id" {
  type = string
}

variable "instance_type" {
  type    = string
  default = "t3a.medium"
}
