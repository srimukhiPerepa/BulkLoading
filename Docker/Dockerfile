# build environment
FROM node:alpine 	as builder

WORKDIR /app

COPY . .

RUN echo $PATH

RUN which npm
#Installing the dependencies listed in our package.json file.
RUN npm -v
