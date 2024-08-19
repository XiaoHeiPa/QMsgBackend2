# Auto update QMessenger && build
# Make sure you have git and jdk 17 installed
echo "Fetching..."
git pull
echo "Building from source..."
bash ./gradlew bootJar
echo "Done."