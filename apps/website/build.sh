#!/bin/bash

if [ "$VERCEL_ENV" != "production" ]; then
    echo "Skipping build for non-production deployments"
    exit 0
fi

echo "Detected production deployment, proceeding"

npx turbo-ignore apps/website