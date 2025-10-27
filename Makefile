IJ_VERSION := 1.54p
IJ_JAR := lib/ij-$(IJ_VERSION).jar
IJ_URL := https://repo1.maven.org/maven2/net/imagej/ij/$(IJ_VERSION)/ij-$(IJ_VERSION).jar

BUILD_DIR := build/classes
DIST_DIR := build/dist
JAVA_SOURCES := $(wildcard *.java)
TEST_SOURCES := $(wildcard tests/*.java)
TEST_MARKER := $(BUILD_DIR)/.tests-compiled
TEST_CLASSES := $(basename $(notdir $(TEST_SOURCES)))

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

ifeq ($(strip $(TEST_SOURCES)),)
$(TEST_MARKER): $(BUILD_DIR)/.compiled
	@touch $@
else
$(TEST_MARKER): $(TEST_SOURCES) $(BUILD_DIR)/.compiled
	@javac -cp "$(CLASSPATH_WITH_CLASSES)" -d $(BUILD_DIR) $(TEST_SOURCES)
	@touch $@
endif

test: $(BUILD_DIR)/.compiled $(TEST_MARKER)
	@if [ -z "$(strip $(TEST_CLASSES))" ]; then \
		echo "No tests to run."; \
	else \
		for cls in $(TEST_CLASSES); do \
			echo "Running $$cls"; \
			java -cp "$(CLASSPATH_WITH_CLASSES)" $$cls || exit $$?; \
		done; \
	fi

clean:
	@rm -rf build

download-deps: $(IJ_JAR)
	@true
