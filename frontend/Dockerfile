FROM node:carbon-alpine as frontendbase
RUN mkdir /opt/app
WORKDIR /opt/app
COPY . ./
RUN npm install
RUN npm run build

FROM nginx
# COPY ./.docker/nginx-strip-poker.conf /etc/nginx/conf.d/default.conf
COPY --from=frontendbase /opt/app/dist /usr/share/nginx/html
