# Compile and run

Just run
```
./gradlew jmh
```

# Troubleshooting

## Got an error :compileJmhJava "Could not target platform: 'Java SE 11' using tool chain: 'JDK 8 (1.8)'"

Ensure gradlew daemon is started with JDK 11 `./gradlew --version`