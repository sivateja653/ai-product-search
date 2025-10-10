region            = "us-east-1"
vpc_id            = "vpc-0b917d2215cad0ca1"
public_subnet_ids = ["subnet-0abb18e450fc8b1ed", "subnet-0eff3c2d9e2111b04"]
sg_alb_id         = "sg-006e9507b4095f871"

tg_name           = "search-service-tg"
alb_name          = "search-service-alb"
health_path       = "/actuator/health"
health_port       = 8080
listener_port     = 80