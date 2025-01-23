docker stop postgres && docker rm postgres
docker build -t postgres .
docker run --restart=always -d -p 5432:5432 -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=prodigy --name postgres postgres:latest
docker logs -f postgres
pause