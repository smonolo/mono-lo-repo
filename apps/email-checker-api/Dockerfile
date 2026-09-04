FROM node:20-alpine AS builder

WORKDIR /app

# Copy root and app files needed for install
COPY package*.json ./
COPY apps/email-checker-api/package*.json ./apps/email-checker-api/

RUN npm ci --workspace=apps/email-checker-api

COPY apps/email-checker-api ./apps/email-checker-api

WORKDIR /app/apps/email-checker-api
RUN npm run build

FROM node:20-alpine AS runner

WORKDIR /app/apps/email-checker-api

ENV NODE_ENV=production
ENV PORT=4000

COPY --from=builder /app/apps/email-checker-api/package*.json ./
COPY --from=builder /app/apps/email-checker-api/dist ./dist
COPY --from=builder /app/node_modules /app/node_modules

EXPOSE 4000

CMD ["node", "dist/index.js"]
