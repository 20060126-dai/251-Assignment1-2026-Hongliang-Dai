# Docker image for the 159.251 text editor.
#
# Build with:   docker build -t 251-text-editor .
# Run tests:    docker run --rm 251-text-editor
# The default command compiles the project and runs the unit tests,
# so a marker can verify the Maven project works in the container
# without installing anything.
#
# To actually open the editor window you need a display, so on a
# desktop machine run the jar directly (see README.md).

FROM maven:3.9-eclipse-temurin-17

WORKDIR /app

# copy the project files (target and reports are excluded by .dockerignore)
COPY pom.xml .
COPY src ./src
COPY editor.yaml .

# build the runnable jar
RUN mvn -B clean package

# default command: verify the build by running the tests
CMD ["mvn", "-B", "test"]
