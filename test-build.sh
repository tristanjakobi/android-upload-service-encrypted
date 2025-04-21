#!/bin/bash

# Set environment variables like JitPack
export ANDROID_HOME=/usr/local/android-sdk
export ANDROID_SDK_ROOT=/usr/local/android-sdk
export ANDROID_NDK_HOME=/usr/local/android-ndk
export GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.debug=true"

# Print environment
echo "Environment:"
env | grep -E "ANDROID|GRADLE|JAVA"

# Clean and build
./gradlew clean
./gradlew :uploadservice:assembleRelease --info --stacktrace
./gradlew :uploadservice:generatePomFileForReleasePublication --info --stacktrace
./gradlew :uploadservice:publishToMavenLocal --info --stacktrace

# Verify artifacts
ls -la uploadservice/build/outputs/aar/
ls -la ~/.m2/repository/com/github/tristanjakobi/ 