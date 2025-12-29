const fs = require("fs");
const path = require("path");

const [, , nextVersion] = process.argv;

if (!nextVersion) {
  console.error("Usage: node scripts/update-gradle-version.js <version>");
  process.exit(1);
}

const gradleFile = path.join(__dirname, "..", "build.gradle.kts");
const contents = fs.readFileSync(gradleFile, "utf8");
const updated = contents.replace(
  /(^\s*version\s*=\s*")[^"]*(")/m,
  `$1${nextVersion}$2`
);

if (updated === contents) {
  console.error("Could not find Gradle version declaration to update.");
  process.exit(1);
}

fs.writeFileSync(gradleFile, updated);
