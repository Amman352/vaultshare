output "account_id" {
  value = data.aws_caller_identity.current.account_id
}

output "vpc_id" {
  value = data.aws_vpc.shared.id
}

output "instance_id" {
  value = aws_instance.tanay.id
}

output "private_ip" {
  value = aws_instance.tanay.private_ip
}

output "public_ip" {
  value = aws_instance.tanay.public_ip
}

output "security_group_id" {
  value = aws_security_group.tanay.id
}
