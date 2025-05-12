# vm-manager-java
vm-manager-javaは、仮想マシンをブラウザから管理できるWebアプリ(仮)のサーバーサイドのプロジェクトです。

##  Build
```bash
$ ./gradlew shadowJar
```
上記コマンドで、`./app/build/libs`に`app-all.jar`が作成されます。

## Run
実行するには、Java 21が必要です。
```bash
$ java -jar app-all.jar [options]
```
## Options
現時点でのコマンドラインオプションです。
- `--uri [URI]`
libvirtのバックエンドURIを指定します。
  - 使用例: `--uri qemu:///system`
- `--disk-images-path [Path]`
仮想マシンのディスクイメージを保存するディレクトリを指定します。
  - 使用例: `--disk-images-path /path/to/disk-dir`
- `--iso-images-path [Path]`
仮想マシンのOSインストールに使用するISOイメージが保存されているディレクトリを指定します。
  - 使用例: `--iso-images-path /path/to/iso-dir`
- `--allow-origin [Origin]`
Web APIでアクセスを許可するOriginを指定します。
  - 使用例: `--allow-origin "*"`
  - 注意: Originをクォーテーションで囲まないと、シェルによって置き換えられる場合があります。

## API
[API.md](API.md) を参照してください。