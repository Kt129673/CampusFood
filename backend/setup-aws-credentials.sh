#!/bin/bash

# Setup script for AWS credentials
# Run this before starting the backend server

echo "=========================================="
echo "CampusFood Backend - AWS Credentials Setup"
echo "=========================================="
echo ""

# Check if credentials are already set
if [ -n "$AWS_ACCESS_KEY" ] && [ -n "$AWS_SECRET_KEY" ]; then
    echo "✅ AWS credentials are already set!"
    echo "   AWS_ACCESS_KEY: ${AWS_ACCESS_KEY:0:10}..."
    echo "   AWS_SECRET_KEY: ${AWS_SECRET_KEY:0:10}..."
    echo ""
    echo "To update credentials, unset them first:"
    echo "   unset AWS_ACCESS_KEY AWS_SECRET_KEY"
    exit 0
fi

# Prompt for credentials
echo "Please enter your AWS credentials:"
echo ""
read -p "AWS Access Key: " access_key
read -sp "AWS Secret Key: " secret_key
echo ""
echo ""

# Validate input
if [ -z "$access_key" ] || [ -z "$secret_key" ]; then
    echo "❌ Error: Both Access Key and Secret Key are required!"
    exit 1
fi

# Export credentials
export AWS_ACCESS_KEY="$access_key"
export AWS_SECRET_KEY="$secret_key"

echo "✅ AWS credentials have been set!"
echo ""
echo "⚠️  Note: These credentials are only set for this terminal session."
echo ""
echo "To make them permanent, add to your ~/.bashrc or ~/.zshrc:"
echo "   export AWS_ACCESS_KEY=\"$access_key\""
echo "   export AWS_SECRET_KEY=\"***hidden***\""
echo ""
echo "Now you can start the backend server:"
echo "   ./mvnw spring-boot:run"
echo ""
