data "aws_caller_identity" "current" {}

data "aws_subnet" "shared" {
  id = var.subnet_id
}

data "aws_vpc" "shared" {
  id = var.vpc_id
}

data "aws_ssm_parameter" "al2023_ami" {
  name = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64"
}

resource "aws_security_group" "tanay" {
  name_prefix = "vaultshare-tanay-"
  description = "Tanay app node in shared VPC - no public inbound access"
  vpc_id      = var.vpc_id

  egress {
    description = "Outbound for K3s installation, image pulls, SSM, and AWS APIs"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name    = "VaultShare-Tanay"
    Project = "VaultShare"
    Owner   = "Tanay"
  }
}

resource "aws_iam_role" "instance" {
  name = "vaultshare-tanay-ec2-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
  tags = { Project = "VaultShare", Owner = "Tanay" }
}

resource "aws_iam_role_policy_attachment" "ssm" {
  role       = aws_iam_role.instance.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "instance" {
  name = "vaultshare-tanay-ec2-profile"
  role = aws_iam_role.instance.name
}

resource "aws_instance" "tanay" {
  ami                         = data.aws_ssm_parameter.al2023_ami.value
  instance_type               = var.instance_type
  subnet_id                   = var.subnet_id
  associate_public_ip_address = true
  vpc_security_group_ids      = [aws_security_group.tanay.id]
  iam_instance_profile        = aws_iam_instance_profile.instance.name
  user_data                   = file("${path.module}/bootstrap-k3s.sh")
  user_data_replace_on_change = true
  monitoring                  = false

  credit_specification {
    cpu_credits = "standard"
  }

  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 2
  }

  root_block_device {
    encrypted   = true
    volume_type = "gp3"
    volume_size = 24
  }

  tags = {
    Name    = "Tanay"
    Project = "VaultShare"
    Module  = "kubernetes-app"
    Owner   = "Tanay"
  }

  lifecycle {
    ignore_changes = [user_data]
    precondition {
      condition     = data.aws_subnet.shared.vpc_id == var.vpc_id
      error_message = "The selected subnet does not belong to the selected shared VPC."
    }
    precondition {
      condition     = data.aws_subnet.shared.owner_id == var.expected_vpc_owner_id
      error_message = "The selected subnet is not owned by the expected VPC owner."
    }
    precondition {
      condition     = data.aws_caller_identity.current.account_id != var.expected_vpc_owner_id
      error_message = "Run this module in Tanay's participant account, not Amman's VPC-owner account."
    }
  }
}
