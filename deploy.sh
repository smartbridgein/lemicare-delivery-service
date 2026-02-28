#!/bin/bash
set -euo pipefail

PROJECT_ID="lemicareprod"
REGION="asia-south1"
SERVICE_NAME="lemicare-delivery-service"
IMAGE="gcr.io/${PROJECT_ID}/${SERVICE_NAME}:latest"
COMMON_LIB_PATH="../lemicare-common"

echo "=== Build common library ==="
cd "$COMMON_LIB_PATH"
mvn clean install -DskipTests
cd -

echo "=== Build & push image ==="
mvn jib:build \
  -Djib.to.image="${IMAGE}" \
  -Djib.to.credHelper=gcloud

echo "=== Deploy to Cloud Run ==="
gcloud run deploy "${SERVICE_NAME}" \
  --image="${IMAGE}" \
  --region="${REGION}" \
  --platform=managed \
  --allow-unauthenticated \
  --memory=512Mi \
  --cpu=1 \
  --set-env-vars SPRING_PROFILES_ACTIVE=cloud \
  --set-env-vars JWT_ISSUER=https://smartbridgein.com \
  --set-env-vars JWT_AUDIENCE=MS \
  --set-env-vars JWT_SECRET_KEY=YourSuperStrongAndLongSecretKeyForHmacShaAlgorithmsAtLeast256Bits \
  --set-env-vars EMAIL=moyyappa@gmail.com \
  --set-env-vars PASS='xLB7sOzV#%Vd$b9q' \
  --set-env-vars SEC=123456

echo "✅ Deployment completed"