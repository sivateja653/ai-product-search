region = "us-east-1"
ecr_repo_url = "068571370361.dkr.ecr.us-east-1.amazonaws.com/search-service"
image_tag = "latest"
secret_arn = "arn:aws:secretsmanager:us-east-1:068571370361:secret:search-service-prod-W4wqgN"

# Optional overrides
log_group_name = "/ecs/search-service"
cpu = 512
memory = 1024
task_family = "search-service"
container_name = "search-service"