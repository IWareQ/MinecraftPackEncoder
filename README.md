# MinecraftPackEncoder

![license](https://img.shields.io/badge/License-Apache_2.0-blue.svg)

Minecraft Resource Pack encryptor and decryptor.

Download jar file from releases.

Required Java 8+

## Usage

### For decrypting use:

```Cmd
java -jar MinecraftPackEncoder.jar decrypt "path/to/resource"
```

### For encrypting use:

```Cmd
java -jar MinecraftPackEncoder.jar encrypt "path/to/resource" "exludeFile.json, excludeFile2.json"
```

If you don't want to exclude files, then don't pass this argument