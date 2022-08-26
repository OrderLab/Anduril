UNAME := $(shell uname -s)
CC=gcc
CFLAGS=-c -Ofast -Wall -fPIC -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux
ifeq ($(UNAME),Darwin)
    CFLAGS=-c -Ofast -Wall -fPIC -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin
endif
LDFLAGS=-fPIC -shared

SOURCES_DIR=src/main/native
OBJECTS_DIR=target/native
EXECUTABLE=target/classes/libfeedback.so

SOURCES=$(shell find '$(SOURCES_DIR)' -type f -name '*.cpp')
OBJECTS=$(SOURCES:$(SOURCES_DIR)/%.cpp=$(OBJECTS_DIR)/%.o)

all: $(EXECUTABLE)

$(EXECUTABLE): $(OBJECTS)
	$(CC) $(LDFLAGS) $(OBJECTS) -o $@

$(OBJECTS): $(SOURCES)
	mkdir -p $(OBJECTS_DIR)
	$(CC) $(CFLAGS) $< -o $@

clean:
	rm -rf $(OBJECTS_DIR) $(EXECUTABLE)
