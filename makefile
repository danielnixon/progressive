all:
	sbt "so clean" "such test"

dist:
	sbt "so clean" "such test" "very publishSigned"

update:
	ncu --upgradeAll && yarn upgrade

.PHONY: all dist update
