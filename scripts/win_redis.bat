docker stop redis && docker rm redis
docker run --restart=always -d -p 6379:6379 -e TZ=Asia/Bishkek --name=redis redis:latest
docker logs -f redis