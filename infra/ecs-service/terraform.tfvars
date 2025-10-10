region              = "us-east-1"
cluster_name        = "search-cluster"
service_name        = "search-service"

task_definition_arn = "arn:aws:ecs:us-east-1:068571370361:task-definition/search-service:2"
private_subnet_ids  = ["subnet-0bfce11308393ca1c", "subnet-011ae5da115769447"]
sg_app_id           = "sg-025ae5070ad09185e"
tg_arn              = "arn:aws:elasticloadbalancing:us-east-1:068571370361:targetgroup/search-service-tg/040417c06b446f3b"

desired_count       = 1