# Makefile for Java Wechaty
#
# 	GitHb: https://github.com/wechaty/java-wechaty
# 	Author: Huan LI <zixia@zixia.net> github.com/huan
#

.PHONY: all
all: install bot

.PHONY: install
install:
	echo "maven install ?"

.PHONY: test
test:
	echo "maven test ?"

.PHONY: bot
bot:
	java examples/ding-dong-bot.java

.PHONY: version
version:
	@newVersion=$$(awk -F. '{print $$1"."$$2"."$$3+1}' < VERSION) \
		&& echo $${newVersion} > VERSION \
		&& git add VERSION \
		&& git commit -m "$${newVersion}" > /dev/null \
		&& git tag "v$${newVersion}" \
		&& echo "Bumped version to $${newVersion}"
