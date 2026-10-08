const { add } = require('./app');

console.log("Running Unit Tests...");

const result = add(2, 3);
if (result === 5) {
    console.log("✅ TEST PASSED: 2 + 3 equals 5");
    process.exit(0); // Exit code 0 means success
} else {
    console.log("❌ TEST FAILED: Expected 5, got " + result);
    process.exit(1); // Exit code 1 tells Jenkins the build failed
}

