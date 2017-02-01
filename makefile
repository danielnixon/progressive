all:
	sbt "such test"

dist:
	sbt "so clean" "such test" "very publishSigned"

update:
	ncu --upgradeAll

.PHONY: all dist update
