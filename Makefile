IJ_VERSION := 1.54g
IJ_JAR := lib/ij-$(IJ_VERSION).jar
IJ_URL := https://repo1.maven.org/maven2/net/imagej/ij/$(IJ_VERSION)/ij-$(IJ_VERSION).jar

BUILD_DIR := build/classes
DIST_DIR := build/dist
JAVA_SOURCES := $(filter-out TestRunner.java,$(wildcard *.java))
TEST_SOURCES := TestRunner.java

ifeq ($(OS),Windows_NT)
  CP_SEP := ;
else
  CP_SEP := :
endif

CLASSPATH := $(IJ_JAR)
CLASSPATH_WITH_CLASSES := $(CLASSPATH)$(CP_SEP)$(BUILD_DIR)

.PHONY: all build clean test download-deps

all: build

$(IJ_JAR):
	@mkdir -p $(dir $@)
	@echo "Downloading ImageJ runtime to $(IJ_JAR)"
	@curl -L -o $@ $(IJ_URL)

$(BUILD_DIR)/.compiled: $(IJ_JAR) $(JAVA_SOURCES)
	@mkdir -p $(BUILD_DIR)
	@javac -cp "$(CLASSPATH)" -d $(BUILD_DIR) $(JAVA_SOURCES)
	@touch $@

$(DIST_DIR)/OpenComet_.jar: $(BUILD_DIR)/.compiled
	@mkdir -p $(DIST_DIR)
	@jar cf $@ -C $(BUILD_DIR) .

build: $(DIST_DIR)/OpenComet_.jar

$(BUILD_DIR)/TestRunner.class: $(TEST_SOURCES) $(BUILD_DIR)/.compiled
	@javac -cp "$(CLASSPATH_WITH_CLASSES)" -d $(BUILD_DIR) $(TEST_SOURCES)

test: $(BUILD_DIR)/.compiled $(BUILD_DIR)/TestRunner.class
	@java -cp "$(CLASSPATH_WITH_CLASSES)" TestRunner

clean:
	@rm -rf build

download-deps: $(IJ_JAR)
	@true
