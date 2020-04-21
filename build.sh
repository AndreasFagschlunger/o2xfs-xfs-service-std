#!/bin/bash
set -e

BRANCH=$(if [ "$TRAVIS_PULL_REQUEST" == "false" ]; then echo $TRAVIS_BRANCH; else echo $TRAVIS_PULL_REQUEST_BRANCH; fi)

generate_VERSION() {
  VERSION=$(./gradlew -s properties | grep 'version: ' | sed -e 's/version: //')
}

increment_patch_level() {
  local baseVersion=${VERSION/-SNAPSHOT/}
  local patchVersion=$(git tag | egrep ^${baseVersion}\.[0-9]+$ | sed -e s/${baseVersion}.// | sort -n | tail -n 1)
  if [ -z "$patchVersion" ]; then
    patchVersion=0
  fi
  NEW_VERSION="$baseVersion.$(($patchVersion+1))"

  sed -i -E s/^[[:blank:]]*version[[:blank:]]*=[[:blank:]]*\"[^\"]+\"[[:blank:]]*/version\ =\ \"$NEW_VERSION\"/ build.gradle.kts
}

git_config() {
  git config --global push.default simple
  git config --global user.name "Travis CI"
  git config --global user.email "travis@travis-ci.com"
  git remote add origin "https://${GITHUB_TOKEN}@github.com/${TRAVIS_REPO_SLUG}.git"
}

git_add_commit() {
  git add build.gradle.kts
  git commit -m "Release ${NEW_VERSION}"
}

git_tag_and_push() {
  if git push origin master >git_push_master.log 2>&1; then \
    failed=false; else failed=true; fi

  sed -i -- "s/${GITHUB_TOKEN}/\[SECRET\]/g" git_push_master.log || true
  if [ ${failed} == "true" ]; then
    echoerr "Failed to git push to master!"
    cat git_push_master.log 1>&2
    exit 1
  else
    cat git_push_master.log
  fi

  git tag -f "${NEW_VERSION}"
  git tag -f latest

  if git push --tags -f >git_push_tags.log 2>&1; then \
    failed=false; else failed=true; fi
  sed -i -- "s/${GITHUB_TOKEN}/\[SECRET\]/g" git_push_tags.log || true
  if [ ${failed} == "true" ]; then
    echoerr "Failed to push git tags!"
    cat git_push_master.log 1>&2
    exit 2
  else
    cat git_push_master.log
  fi
}

if [ "$BRANCH" == "master" ]; then
  generate_VERSION
  increment_patch_level
fi

./gradlew build

if [ "$BRANCH" == "master" ]; then
  git_config
  git_add_commit
  git_tag_and_push
fi

if [ "$BRANCH" == "master" ] || [ "$BRANCH" == "develop" ]; then
  ./gradlew publish
fi
