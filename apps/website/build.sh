#!/bin/bash

if [ "$VERCEL_ENV" != "production" ]; then
    echo "Skipping build for non-production deployments"
    exit 0
fi

npx turbo-ignore